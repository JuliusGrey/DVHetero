package RISCV.EXU

import RISCV.IDU.CtrlUnit.dnpcSrcList._
import chisel3._
import RISCV.common.Param._

class dnpcGen extends Module{
  val io = IO(new Bundle {
    val npcSrc = Input(Bool())
    val pc = Input(UInt(addrW.W))
    val imme = Input(UInt(addrW.W))
    val rd = Input(UInt(addrW.W))
    val dnpc = Output(UInt(addrW.W))
  })
  val src1 = Mux(io.npcSrc === rd , io.rd,io.pc)
  io.dnpc := io.imme + src1
}
