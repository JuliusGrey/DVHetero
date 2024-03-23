package RISCV.EXU.ALUOp

import chisel3._
import chisel3.util._
import RISCV.common.Param.dataW

//当前来说我是要求输入保持不变的，如果要求输入只在valid起作用的话需要稍微改一下，就在idle状态遇到vali的时候采集数据到 寄存器中，进入idle的时候再清0，其他时候保持不变就可以了
class divR2 extends Module{
  val io = IO (new Bundle() {
    val dividend = Input(UInt(dataW.W))
    val divisor = Input(UInt(dataW.W))
    val div_valid = Input(Bool())
    val divw  = Input(Bool())
    val div_signed = Input(Bool())
    val out_valid = Output(Bool())
    val quotient = Output(UInt(dataW.W))
    val remainder = Output(UInt(dataW.W))

    val block = Input(Bool())
  })
//这里注意符号位1是正数，0是负数
  val dividend64Real = Mux(io.dividend(dataW - 1) && io.div_signed ,(~io.dividend).asUInt + 1.U,io.dividend)
  val divisor64Real = Mux(io.divisor(dataW - 1) && io.div_signed ,(~io.divisor).asUInt + 1.U,io.divisor)
  val quoSgn64 = (io.dividend(dataW - 1) ^ io.divisor(dataW - 1)) &&io.div_signed
  val remSgn64 = io.dividend(dataW - 1) &&io.div_signed

  val dividend32Real = Mux(io.dividend(32 - 1) && io.div_signed, (~io.dividend(31,0)).asUInt + 1.U, io.dividend(31,0))
  val divisor32Real = Mux(io.divisor(32 - 1) && io.div_signed, (~io.divisor(31,0)).asUInt + 1.U, io.divisor(31,0))
  val quoSgn32 = (io.dividend(32 - 1) ^ io.divisor(32 - 1)) && io.div_signed
  val remSgn32 = io.dividend(32 - 1) && io.div_signed



//先写好状态机，共四个状态
  val idleState :: div32State :: div64State :: resultState ::Nil = Enum(4)
  val stateReg = RegInit(idleState)
  val isIdle = stateReg===idleState
  val isDiv32 = stateReg===div32State
  val isDiv64 = stateReg === div64State
  val isResult = stateReg === resultState
  val cnt = RegInit(0.U(6.W))
  val idleMux = MuxLookup(
    Cat(io.div_valid,io.divw),
    idleState,
    Array(
      "b11".U -> div32State,
      "b10".U -> div64State
    )
  )
  val div32Mux = Mux(cnt === 31.U ,resultState,div32State )
  val div64Mux = Mux(cnt === 63.U ,resultState,div64State )
  val resultMux = Mux(io.block ,resultState,idleState )
  stateReg := MuxLookup(
    stateReg,
    idleState,
    Array(
      idleState->idleMux,
      div32State -> div32Mux,
      div64State -> div64Mux,
      resultState -> resultMux
    )
  )

  cnt := Mux(isDiv32|| isDiv64 , cnt + 1.U,0.U)

  //运算过程
  val dividendReg = RegInit(0.U(128.W))
  val resReg = RegInit(0.U(64.W))
  val rem32M = Wire(UInt(32.W))
  val rem64M = Wire(UInt(64.W))

  val idleDividendMux = MuxLookup(
    Cat(io.div_valid,io.divw),
    0.U,
    Array(
      "b11".U -> Cat(0.U(64.W),0.U(32.W),dividend32Real(31,0)),
      "b10".U -> Cat(0.U(64.W),dividend64Real))
  )
  val div32DividendMux = Cat(0.U(64.W), rem32M,dividendReg(30,0),false.B)
  val div64DividendMux = Cat(rem64M,dividendReg(62,0),false.B)

  dividendReg := MuxLookup(
    stateReg,
    0.U,
    Array(
      idleState ->idleDividendMux,
      div32State -> div32DividendMux,
      div64State -> div64DividendMux,
      resultState -> dividendReg
    )
  )
  val subed64 =  dividendReg(127,63)
  val subRes64 = subed64 - divisor64Real
  rem64M := Mux(subRes64(64),subed64(63,0),subRes64(63,0))

  val subed32= dividendReg(63,31)
  val subRes32 = subed32 - divisor32Real
  rem32M := Mux(subRes32(32),subed32(31,0),subRes32(31,0))

  resReg := MuxLookup(
    stateReg,
    0.U,
    Array(
      idleState -> 0.U,
      div32State -> Cat(resReg(62,0),!subRes32(32)),
      div64State -> Cat(resReg(62,0),!subRes64(64)),
      resultState -> resReg,
    )
  )

  val res64Out = Mux(quoSgn64 , ( ~resReg).asUInt() + 1.U , resReg)
  val rem64Out =Mux(remSgn64, (~dividendReg(127,64)).asUInt() + 1.U,dividendReg(127,64))

  val res32out = Mux(quoSgn32 , Cat(Fill(32,true.B) ,( ~resReg(31,0)).asUInt()+ 1.U)  ,resReg )
//  printf("quoSgn32 is %x\n", quoSgn32 )
  val rem32Out = Mux(remSgn32,  Cat(Fill(32,true.B) ,( ~dividendReg(63,32)).asUInt()+ 1.U) , Cat(Fill(32,false.B) ,dividendReg(63,32) ))
//  printf("dividendReg is %x\n",dividendReg)
//  printf("resReg is %x\n",resReg)
//  printf("resReg is %x\n",resReg)
  io.quotient := Mux(io.divw ,res32out,res64Out )
  io.remainder := Mux(io.divw,rem32Out,rem64Out )
//  printf("io.quotient is %x\n", io.quotient )

  io.out_valid := isResult
//  printf("divst is %x\n",stateReg)
//  printf("div cnt is %x\n",cnt)



}

object divgen extends App {
  chisel3.Driver.execute(args,() => new divR2 )


}
