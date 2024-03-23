package RISCV.EXU.ALUOp

import RISCV.IDU.CtrlUnit.ALUOpSel._
import RISCV.IDU.immeList.{SignExt, ZeroExt}
import chisel3._
import chisel3.util._
import chisel3.util.experimental.BoringUtils
import RISCV.common.Param._

class ALU extends Module{
  //  val opMap: Map[UInt ,(UInt,UInt) => UInt] = Map(
  //    ALUOp.OR -> { (srcA: UInt, srcB: UInt) => srcA | srcB }
  //  )
  val io = IO(new Bundle {
    val srcA = Input(UInt(dataW.W))
    val srcB = Input(UInt(dataW.W))
    val ALUCtrl = Input(UInt(ALU_Ctrl_W.W))
    val ALUResult = Output(UInt(dataW.W))
  }
  )
  //例化加法器及其相关输入端口
  val addIns = Module(new add(dataW))
  val subList: List[UInt] = List(ALU_SUB,ALU_LT,ALU_SUBW,ALU_LT,ALU_LTU,ALU_GEU,ALU_GE)
  val sub = subList.foldLeft(false.B)((res , i) => res || io.ALUCtrl===i)
  val srcBInv = (~io.srcB).asUInt()
  addIns.io.cin := sub
  addIns.io.a := io.srcA
  if(hasBug) addIns.io.b :=  io.srcB else addIns.io.b :=Mux(sub,srcBInv,io.srcB)

  //例化一个除法器
  val divR2Ins= Module(new divR2)
  val  divValidList: List[UInt] =List(ALU_DIVUW,ALU_REMUW,ALU_REMW,ALU_DIVW,ALU_DIVU,ALU_REM,ALU_DIV,ALU_REMU)
  val divValid = divValidList.foldLeft(false.B)((res , i) => res || io.ALUCtrl===i)
  val divWList: List[UInt] =List(ALU_DIVUW,ALU_REMUW,ALU_REMW,ALU_DIVW)
  val divW = divWList.foldLeft(false.B)((res , i) => res || io.ALUCtrl===i)
  val divSignedList: List[UInt] =List(ALU_REMW,ALU_DIVW,ALU_REM,ALU_DIV)
  val divSigned = divSignedList.foldLeft(false.B)((res , i) => res || io.ALUCtrl===i)
  divR2Ins.io.dividend := io.srcA
  divR2Ins.io.divisor := io.srcB
  divR2Ins.io.div_valid := divValid
  divR2Ins.io.divw := divW
  divR2Ins.io.div_signed := divSigned
//  divR2Ins.io.block := false.B

  //例化一个乘法器
  val mulIns = Module(new mul(dataW))
  val srcAUSignW = ZeroExt(io.srcA(31,0),dataW)
  val srcBUSignW = ZeroExt(io.srcB(31,0),dataW)
  val mulValidList: List[UInt] =List(ALU_MULW,ALU_MUL)
  val mulValid = mulValidList.foldLeft(false.B)((res , i) => res || io.ALUCtrl===i)
  val inUSignWList: List[UInt] =List(ALU_MULW)
  val inUSignW = inUSignWList.foldLeft(false.B)((res , i) => res || io.ALUCtrl===i)

  mulIns.io.mul_signed := false.B
  mulIns.io.mul_valid := mulValid
  mulIns.io.multiplicand := Mux(inUSignW,srcAUSignW,io.srcA)
  mulIns.io.multiplier := Mux(inUSignW,srcBUSignW,io.srcB)
//  mulIns.io.block := false.B


  val shamt = io.srcB(5, 0).asUInt
  val ALUBuffer = Array(
    ALU_ADD -> addIns.io.sum,
    ALU_SUB -> addIns.io.sum,
    ALU_SRA -> (io.srcA.asSInt >> shamt).asUInt,
    ALU_SRL -> (io.srcA >> shamt),
    ALU_SLL -> (io.srcA << shamt),
    ALU_LT ->  addIns.io.sum(dataW -1).asUInt(),
    ALU_LTU -> !addIns.io.cout,
    ALU_AND -> (io.srcA & io.srcB),
    ALU_OR -> (io.srcA | io.srcB),
    ALU_XOR -> (io.srcA ^ io.srcB),
    ALU_NE -> ZeroExt((io.srcA =/= io.srcB),dataW),
    ALU_SRLW ->  SignExt((io.srcA(31,0).asUInt >> shamt ).asUInt, dataW),
    ALU_ADDW -> SignExt((addIns.io.sum)(31,0).asUInt,dataW),
    ALU_SLLW ->  SignExt((io.srcA(31,0).asUInt << shamt )(31,0).asUInt, dataW),
    ALU_SRAW ->  SignExt((io.srcA(31,0).asSInt >> shamt ).asUInt(), dataW),
    ALU_SUBW -> SignExt(addIns.io.sum(31,0).asUInt,dataW),
    ALU_DIVUW -> SignExt(divR2Ins.io.quotient(31,0).asUInt,dataW),
    ALU_REMUW -> SignExt(divR2Ins.io.remainder(31,0).asUInt,dataW),
    ALU_MULW -> SignExt(mulIns.io.result_low(31,0).asUInt,dataW),
    ALU_REMW -> SignExt(divR2Ins.io.remainder(31,0).asUInt,dataW),
    ALU_DIVW -> SignExt(divR2Ins.io.quotient(31,0).asUInt,dataW),
    ALU_MUL ->  mulIns.io.result_low,
    ALU_DIVU -> divR2Ins.io.quotient,
    ALU_REM -> divR2Ins.io.remainder,
    ALU_DIV -> divR2Ins.io.quotient,
    ALU_EQ -> (io.srcA === io.srcB) ,
    ALU_GEU -> addIns.io.cout ,//
//    ALU_LT -> (io.srcA.asSInt() < io.srcB.asSInt()) ,
    ALU_GE ->  !addIns.io.sum(dataW -1) ,
    ALU_REMU -> divR2Ins.io.remainder  ,//
    ALU_PASS -> io.srcA
  )
  io.ALUResult := MuxLookup(io.ALUCtrl,0.U,ALUBuffer)

  val block1 = Wire(Bool())
  block1 := divR2Ins.io.div_valid && !divR2Ins.io.out_valid || mulIns.io.mul_valid && !mulIns.io.out_valid
  BoringUtils.addSource(block1, "block1")

//  val block2  = Wire(Bool())
//  block2 := false.B
//  BoringUtils.addSink(block1, "block2")

  val block23 = Wire(Bool())
  block23 := true.B
  BoringUtils.addSink(block23, "block23")


  mulIns.io.block := block23
  divR2Ins.io.block := block23
  //  io.ALUResult := io.srcA + io.srcB
  //  val opMap: Map[UInt ,UInt] = Map(
  //    ALUOp.OR -> {  io.srcA + io.srcB }
  //  )
  //  io.ALUResult := MuxLookup(io.ALUC , 0.U,opMap.toList)
  //  printf("ctrl is %d\n" , io.ALUCtrl)
  //  printf("srcb is %d \n",io.srcB )
  //  printf("rs is %d \n",io.ALUResult )


}
