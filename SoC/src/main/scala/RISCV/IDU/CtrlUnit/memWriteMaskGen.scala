package RISCV.IDU.CtrlUnit

import chisel3.util._
import chisel3._
import RISCV.common.Param._
import RISCV.common.instDe.getF3
import RISCV.common.instList._
//只有s指令不确定，其他都没有，需要手动添加s
class memWriteMaskGen extends RawModule{
  val io = IO(new Bundle {
    val inst = Input(UInt(instW.W))
    val mask = Output(UInt(8.W))
  })
//  io.mask := MuxCase(
//    0.U,
//    Array(
//      (SD === io.inst) -> "b11111111".U(8.W)
//    )
//  )
  io.mask := MuxLookup(
    getF3(io.inst),
    0.U,
    Array(
      0.U ->"b00000001".U(8.W),
      1.U ->"b00000011".U(8.W),
      2.U ->"b00001111".U(8.W),
      3.U ->"b11111111".U(8.W)
    )
  )
}
