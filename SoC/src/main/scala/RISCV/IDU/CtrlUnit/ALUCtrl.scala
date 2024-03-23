package RISCV.IDU.CtrlUnit

import chisel3.util._
import ALUOpSel._
import chisel3._
import RISCV.common.Param._
import RISCV.common.instList._

import scala.collection.mutable.ArrayBuffer

object ALUOpSel {
  val ALU_Ctrl_W = 5
  val ALU_ADD = 0.U(ALU_Ctrl_W.W)
  val ALU_SUB = 1.U(ALU_Ctrl_W.W)
  val ALU_AND = 2.U(ALU_Ctrl_W.W)
  val ALU_OR = 3.U(ALU_Ctrl_W.W)
  val ALU_XOR = 4.U(ALU_Ctrl_W.W)
  val ALU_LT = 5.U(ALU_Ctrl_W.W)
  val ALU_SLL = 6.U(ALU_Ctrl_W.W)
  val ALU_LTU = 7.U(ALU_Ctrl_W.W)
  val ALU_SRL = 8.U(ALU_Ctrl_W.W)
  val ALU_SRA = 9.U(ALU_Ctrl_W.W)
  val ALU_PASS = 10.U(ALU_Ctrl_W.W)
  val ALU_NE = 11.U(ALU_Ctrl_W.W)
  //  val ALU_LTU = 12.U(ALU_Ctrl_W.W)
  val ALU_SRLW = 12.U(ALU_Ctrl_W.W)
  val ALU_ADDW = 13.U(ALU_Ctrl_W.W)
  val ALU_SLLW = 14.U(ALU_Ctrl_W.W)
  val ALU_SRAW = 15.U(ALU_Ctrl_W.W)
  val ALU_SUBW = 16.U(ALU_Ctrl_W.W)
  val ALU_DIVUW = 17.U(ALU_Ctrl_W.W)
  val ALU_REMUW = 18.U(ALU_Ctrl_W.W)
  val ALU_MULW = 19.U(ALU_Ctrl_W.W)
  val ALU_REMW = 20.U(ALU_Ctrl_W.W)
  val ALU_DIVW = 21.U(ALU_Ctrl_W.W)
  val ALU_MUL = 22.U(ALU_Ctrl_W.W)
  val ALU_DIVU = 23.U(ALU_Ctrl_W.W)
  val ALU_REM = 24.U(ALU_Ctrl_W.W)
  val ALU_DIV = 25.U(ALU_Ctrl_W.W)
  val ALU_EQ = 26.U(ALU_Ctrl_W.W)
  val ALU_GEU = 27.U(ALU_Ctrl_W.W)
//  val ALU_LT = 28.U(ALU_Ctrl_W.W)
  val ALU_GE = 28.U(ALU_Ctrl_W.W)
  val ALU_REMU = 29.U(ALU_Ctrl_W.W)
  val ALU_XXX = 31.U(ALU_Ctrl_W.W)



}

//iurb不确定，j没有，s一直是加，需要手动添加irub
class ALUCtrl extends RawModule{
  val io = IO(new Bundle {
    val inst = Input(UInt(instW.W))
    val ALUCtrl  = Output(UInt(ALU_Ctrl_W.W))
  })

//  val ALUCtrlList : Array[Tuple2[Bool,UInt]]  = Array(
//    (io.inst ===ADDI) -> ALU_ADD,
//    (io.inst ===AUIPC) -> ALU_ADD,
//    (io.inst ===LUI) -> ALU_PASS,
//    (io.inst ===JAL) -> ALU_XXX,
//    (io.inst ===JALR) -> ALU_XXX,
//    (io.inst ===SD) -> ALU_ADD,
//    (io.inst ===LD) -> ALU_ADD,
//    (io.inst ===LBU) -> ALU_ADD,
//    (io.inst ===BNE) -> ALU_NE,
//    (io.inst ===SLTIU) -> ALU_LTU
//  )

  val ALUCtrlList: ArrayBuffer[Tuple2[Bool, UInt]] = new ArrayBuffer()
  map_type.map {
    case (k, v) => {
      v match {
        case "S" => ALUCtrlList.append((io.inst === k) -> ALU_ADD)
        case _ =>
      }
    }
  }
  ALUCtrlList.append((io.inst ===AUIPC) -> ALU_ADD)
  ALUCtrlList.append((io.inst ===LUI) -> ALU_PASS)
  ALUCtrlList.append((io.inst ===ADDI) -> ALU_ADD)
  ALUCtrlList.append((io.inst ===JALR) -> ALU_XXX)
  ALUCtrlList.append((io.inst ===LD) -> ALU_ADD)
  ALUCtrlList.append((io.inst ===LBU) -> ALU_ADD)
  ALUCtrlList.append((io.inst ===SLTIU) -> ALU_LTU)
  ALUCtrlList.append((io.inst ===SRLIW) -> ALU_SRLW)
  ALUCtrlList.append((io.inst ===SLLI) -> ALU_SLL)
  ALUCtrlList.append((io.inst ===ANDI) -> ALU_AND)
  ALUCtrlList.append((io.inst ===XORI) -> ALU_XOR)
  ALUCtrlList.append((io.inst ===ADDIW) -> ALU_ADDW)
  ALUCtrlList.append((io.inst ===SRLI) -> ALU_SRL)
  ALUCtrlList.append((io.inst ===SLLIW) -> ALU_SLLW)
  ALUCtrlList.append((io.inst ===SRAIW) -> ALU_SRAW)
  ALUCtrlList.append((io.inst ===SRAI) -> ALU_SRA)
  ALUCtrlList.append((io.inst ===LH) -> ALU_ADD)
  ALUCtrlList.append((io.inst ===LW) -> ALU_ADD)
  ALUCtrlList.append((io.inst ===LD) -> ALU_ADD)
  ALUCtrlList.append((io.inst ===LHU) -> ALU_ADD)
  ALUCtrlList.append((io.inst ===LWU) -> ALU_ADD)
  ALUCtrlList.append((io.inst ===LB) -> ALU_ADD)
  ALUCtrlList.append((io.inst ===ORI) -> ALU_OR)
  ALUCtrlList.append((io.inst ===SLTU) -> ALU_LTU)
  ALUCtrlList.append((io.inst ===ADDW) -> ALU_ADDW)
  ALUCtrlList.append((io.inst ===SUBW) -> ALU_SUBW)
  ALUCtrlList.append((io.inst ===AND) -> ALU_AND)
  ALUCtrlList.append((io.inst ===ADD) -> ALU_ADD)
  ALUCtrlList.append((io.inst ===DIVUW) -> ALU_DIVUW)
  ALUCtrlList.append((io.inst ===REMUW) -> ALU_REMUW)
  ALUCtrlList.append((io.inst ===SUB) -> ALU_SUB)
  ALUCtrlList.append((io.inst ===MULW) -> ALU_MULW)
  ALUCtrlList.append((io.inst ===REMW) -> ALU_REMW)
  ALUCtrlList.append((io.inst ===DIVW) -> ALU_DIVW)
  ALUCtrlList.append((io.inst ===MUL) -> ALU_MUL)
  ALUCtrlList.append((io.inst ===OR) -> ALU_OR)
  ALUCtrlList.append((io.inst ===SLLW) -> ALU_SLLW)
  ALUCtrlList.append((io.inst ===SRAW) -> ALU_SRAW)
  ALUCtrlList.append((io.inst ===SRLW) -> ALU_SRLW)
  ALUCtrlList.append((io.inst ===SLT) -> ALU_LT)
  ALUCtrlList.append((io.inst ===DIVU) -> ALU_DIVU)
  ALUCtrlList.append((io.inst ===XOR) -> ALU_XOR)
  ALUCtrlList.append((io.inst ===REM) -> ALU_REM)
  ALUCtrlList.append((io.inst ===DIV) -> ALU_DIV)
  ALUCtrlList.append((io.inst ===SLL) -> ALU_SLL)
  ALUCtrlList.append((io.inst ===BEQ) -> ALU_EQ)
  ALUCtrlList.append((io.inst ===BLTU) -> ALU_LTU)
  ALUCtrlList.append((io.inst ===BGEU) -> ALU_GEU)
  ALUCtrlList.append((io.inst ===BLT) -> ALU_LT)
  ALUCtrlList.append((io.inst ===BGE) -> ALU_GE)
  ALUCtrlList.append((io.inst ===BNE) -> ALU_NE)
  ALUCtrlList.append((io.inst ===REMU) -> ALU_REMU)
  ALUCtrlList.append((io.inst ===SRL) -> ALU_SRL)


  io.ALUCtrl := MuxCase( ALU_XXX,ALUCtrlList)

}


object ALUCtrlGen extends App{
  chisel3.Driver.execute(args,() => new ALUCtrl )
}
