package RISCV.EXU

import RISCV.IDU.immeList.{SignExt,ZeroExt}
import chisel3._
import chisel3.util._
import RISCV.common.Param.{addrW, dataW}
class memData extends  Module{
  val io = IO(new Bundle {
    val rdata =  Input(UInt(dataW.W))
    val rdata_ext = Output(UInt(dataW.W))
    val memReadNum = Input(UInt(3.W))
  })

  io.rdata_ext := MuxLookup(
    io.memReadNum,
    0.U,
    Array(
      0.U(3.W) -> SignExt(io.rdata(7,0),dataW),
      1.U(3.W) -> SignExt(io.rdata(15,0),dataW),
      2.U(3.W) -> SignExt(io.rdata(31,0),dataW),
      3.U(3.W) -> io.rdata,
      4.U(3.W) -> ZeroExt(io.rdata(7,0),dataW),
      5.U(3.W) -> ZeroExt(io.rdata(15,0),dataW),
      6.U(3.W) -> ZeroExt(io.rdata(31,0),dataW)
    )
  )
}
