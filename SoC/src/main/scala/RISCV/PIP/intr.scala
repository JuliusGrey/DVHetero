package RISCV.PIP

import chisel3._
class intr extends BlackBox{
  val io = IO(new Bundle() {
    val intr = Input(Bool())
  })

}