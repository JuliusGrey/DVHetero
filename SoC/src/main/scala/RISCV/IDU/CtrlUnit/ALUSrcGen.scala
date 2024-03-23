package RISCV.IDU.CtrlUnit

import chisel3.util._
import ALUSrc._
import chisel3._
import RISCV.common.Param._
import RISCV.common.instList._

import scala.collection.mutable.ArrayBuffer
//涉及到npc的相加，不会使用ALU
object ALUSrc {
  def ALUSrcRd = 0.U
  def ALUSrcImm = 1.U
  def ALUSrcPC = 2.U
  def ALUSrcXX = 3.U
}
//目前看来，U形不确定，I形就是src1 和2，s是1+dst，J没用，B1和2，R1和2，需要手动添加U
class ALUSrcGen extends RawModule {
  val io = IO(new Bundle {
    val inst = Input(UInt(instW.W))
    val AluSrc1 = Output(UInt(2.W))
    val AluSrc2 = Output(UInt(2.W))
  })
//  val AluSrc1List : Array[Tuple2[Bool,UInt]]  = Array(
//    (io.inst ===ADDI) -> ALUSrcRd,
//    (io.inst ===AUIPC) -> ALUSrcImm,
//    (io.inst ===LUI) -> ALUSrcImm,
//    (io.inst ===JAL) -> ALUSrcXX,
//    (io.inst ===JALR) -> ALUSrcXX,
//    (io.inst ===SD) -> ALUSrcRd,
//    (io.inst ===LD) -> ALUSrcRd,
//    (io.inst ===LBU) -> ALUSrcRd,
//    (io.inst ===BNE) -> ALUSrcRd,
//    (io.inst ===SLTIU) -> ALUSrcRd,
//  )
//  val AluSrc2List : Array[Tuple2[Bool,UInt]]  = Array(
//    (io.inst ===ADDI) -> ALUSrcImm,
//    (io.inst ===AUIPC) -> ALUSrcPC,
//    (io.inst ===LUI) -> ALUSrcXX,
//    (io.inst ===JAL) -> ALUSrcXX,
//    (io.inst ===JALR) -> ALUSrcXX,
//    (io.inst ===SD) -> ALUSrcImm,
//    (io.inst ===LD) -> ALUSrcImm,
//    (io.inst ===LBU) -> ALUSrcImm,
//    (io.inst ===BNE) -> ALUSrcRd,
//    (io.inst ===SLTIU) -> ALUSrcImm,
//  )
//
////  val AluSrcBuf: ArrayBuffer[Tuple2[Bool, UInt]] = new ArrayBuffer()
////  map_type.map {
////    case (k, v) => {
////      v match {
////        case "I" => AluSrcBuf.append((io.inst === k) -> ALUSrcImm)
////        case _ =>
////      }
////    }
////  }


  val AluSrc1List: ArrayBuffer[Tuple2[Bool, UInt]] = new ArrayBuffer()
  map_type.map {
    case (k, v) => {
      v match {
        case "I" => AluSrc1List.append((io.inst === k) -> ALUSrcRd)
        case "S" => AluSrc1List.append((io.inst === k) -> ALUSrcRd)
        case "B" => AluSrc1List.append((io.inst === k) -> ALUSrcRd)
        case "R" => AluSrc1List.append((io.inst === k) -> ALUSrcRd)
        case _ =>
      }
    }
  }
  AluSrc1List.append((io.inst ===AUIPC) -> ALUSrcImm)
  AluSrc1List.append((io.inst === LUI) -> ALUSrcImm)

  val AluSrc2List: ArrayBuffer[Tuple2[Bool, UInt]] = new ArrayBuffer()
  map_type.map {
    case (k, v) => {
      v match {
        case "I" => AluSrc2List.append((io.inst === k) -> ALUSrcImm)
        case "S" => AluSrc2List.append((io.inst === k) -> ALUSrcImm)
        case "B" => AluSrc2List.append((io.inst === k) -> ALUSrcRd)
        case "R" => AluSrc2List.append((io.inst === k) -> ALUSrcRd)
        case _ =>
      }
    }
  }
  AluSrc2List.append((io.inst ===AUIPC) -> ALUSrcPC)
  AluSrc2List.append((io.inst ===LUI) -> ALUSrcXX)


  io.AluSrc1 := MuxCase(0.U, AluSrc1List)
  io.AluSrc2 := MuxCase(0.U, AluSrc2List)


}
