package CGRA.element.ALU

import chisel3._
import chisel3.util._

class Div(val dataW:Int)  extends Module{
  val io = IO (new Bundle() {
    val dividend = Input(UInt(dataW.W))
    val divisor = Input(UInt(dataW.W))
    val divValid = Input(Bool())
    val divSigned = Input(Bool())
    val outValid = Output(Bool())
    val quotient = Output(UInt(dataW.W))
    val remainder = Output(UInt(dataW.W))

  })
  //这里注意符号位1是正数，0是负数
  val dividendReal = Mux(io.dividend(dataW - 1) && io.divSigned ,(~io.dividend).asUInt + 1.U,io.dividend)
  val divisorReal = Mux(io.divisor(dataW - 1) && io.divSigned ,(~io.divisor).asUInt + 1.U,io.divisor)
  val quoSgn = (io.dividend(dataW - 1) ^ io.divisor(dataW - 1)) &&io.divSigned
  val remSgn = io.dividend(dataW - 1) &&io.divSigned

  //先写好状态机，共三个状态
  val idleState :: divState :: resultState ::Nil = Enum(3)
  val stateReg = RegInit(idleState)
  val isIdle = stateReg===idleState
  val isDiv = stateReg === divState
  val isResult = stateReg === resultState
  val cnt = RegInit(0.U(6.W))
  val idleMux = Mux(
    io.divValid,
    divState,
    idleState
  )
  val divMux = Mux(cnt ===(dataW - 1).U ,resultState,divState )
  stateReg := MuxLookup(
    stateReg,
    idleState,
    Array(
      idleState->idleMux,
      divState -> divMux,
      resultState -> idleState
    )
  )

  cnt := Mux( isDiv , cnt + 1.U,0.U)

  //运算过程
  val dividendReg = RegInit(0.U((dataW*2).W))
  val resReg = RegInit(0.U(dataW.W))
  val remM = Wire(UInt(dataW.W))

  val idleDividendMux = Mux(
    io.divValid,
    Cat(0.U(dataW.W),dividendReal),
    0.U
  )
  val divDividendMux = Cat(remM,dividendReg(dataW - 2,0),false.B)

  dividendReg := MuxLookup(
    stateReg,
    0.U,
    Array(
      idleState ->idleDividendMux,
      divState -> divDividendMux,
      resultState -> dividendReg
    )
  )
  val subed =  dividendReg(dataW*2 - 1,dataW - 1)
  val subRes = subed - divisorReal
  remM := Mux(subRes(dataW),subed(dataW - 1 ,0),subRes(dataW - 1,0))

  resReg := MuxLookup(
    stateReg,
    0.U,
    Array(
      idleState -> 0.U,
      divState -> Cat(resReg(dataW - 2,0),!subRes(dataW)),
      resultState -> resReg,
    )
  )

  val resOut = Mux(quoSgn , ( ~resReg).asUInt() + 1.U , resReg)
  val remOut =Mux(remSgn, (~dividendReg(dataW*2 - 1,dataW)).asUInt() + 1.U,dividendReg(dataW*2-1,dataW))

  io.quotient := resOut
  io.remainder := remOut

  io.outValid := isResult



}
