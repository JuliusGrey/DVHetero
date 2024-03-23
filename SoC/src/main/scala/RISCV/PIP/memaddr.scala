package RISCV.PIP

import chisel3._
import RISCV.common.Param.addrW
class memaddr extends BlackBox{
  val io = IO(new Bundle() {
    val addr = Input(UInt(addrW.W))
  })

}