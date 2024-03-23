package RISCV.IDU.CtrlUnit

import ResultSrcList._
import chisel3._
import chisel3.util.MuxCase
import RISCV.common.Param._
import RISCV.common.instList._

import scala.collection.mutable.ArrayBuffer

//写到寄存器的值的由来
//jrU确定，I不确定，sb没有，需要手动添加I
object ResultSrcList {
  val regFrom_ALU = 0.U(2.W)
  val regFrom_NPC = 1.U(2.W)
  val regFrom_DM = 2.U(2.W)
  val regFrom_XX = 0.U(2.W)
}
class ResultSrcGen extends RawModule{
  val io = IO(new Bundle {
    val inst = Input(UInt(instW.W))
    val ResultSrc = Output(UInt(2.W))
  })
//  val RSList : Array[Tuple2[Bool,UInt]]  = Array(
//    (io.inst ===ADDI) -> regFrom_ALU,
//    (io.inst ===AUIPC) -> regFrom_ALU,
//    (io.inst ===LUI) -> regFrom_ALU,
//    (io.inst ===JAL) -> regFrom_NPC,
//    (io.inst ===JALR) -> regFrom_NPC,
//    (io.inst ===SD) -> regFrom_XX,
//    (io.inst ===LD) -> regFrom_DM,
//    (io.inst ===LBU) -> regFrom_DM
//  )
  val RSList: ArrayBuffer[Tuple2[Bool, UInt]] = new ArrayBuffer()
  map_type.map {
    case (k, v) => {
      v match {
        case "U" => RSList.append((io.inst === k) -> regFrom_ALU)
        case "J" => RSList.append((io.inst === k) -> regFrom_NPC)
        case "R" => RSList.append((io.inst === k) -> regFrom_ALU)
        case _ =>
      }
    }
  }
  RSList.append((io.inst ===ADDI) -> regFrom_ALU)
  RSList.append((io.inst ===JALR) -> regFrom_NPC)
  RSList.append((io.inst ===JALR) -> regFrom_NPC)
  RSList.append((io.inst ===LD) -> regFrom_DM)
  RSList.append((io.inst ===LBU) -> regFrom_DM)
  RSList.append((io.inst ===SLTIU) -> regFrom_ALU)
  RSList.append((io.inst ===SRLIW) -> regFrom_ALU)
  RSList.append((io.inst ===SLLI) -> regFrom_ALU)
  RSList.append((io.inst ===ANDI) -> regFrom_ALU)
  RSList.append((io.inst ===XORI) -> regFrom_ALU)
  RSList.append((io.inst ===ADDIW) -> regFrom_ALU)
  RSList.append((io.inst ===SRLI) -> regFrom_ALU)
  RSList.append((io.inst ===SLLIW) -> regFrom_ALU)
  RSList.append((io.inst ===SRAIW) -> regFrom_ALU)
  RSList.append((io.inst ===SRAI) -> regFrom_ALU)
  RSList.append((io.inst ===ORI) -> regFrom_ALU)
  RSList.append((io.inst ===LH) -> regFrom_DM)
  RSList.append((io.inst ===LW) -> regFrom_DM)
  RSList.append((io.inst ===LHU) -> regFrom_DM)
  RSList.append((io.inst ===LWU) -> regFrom_DM)
  RSList.append((io.inst ===LB) -> regFrom_DM)
  RSList.append((io.inst ===CSRRS) -> regFrom_ALU)
  RSList.append((io.inst ===CSRRW) -> regFrom_ALU)

  io.ResultSrc := MuxCase( false.B,RSList)
}
