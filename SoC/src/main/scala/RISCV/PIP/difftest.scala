package RISCV.PIP

import chisel3._
class difftest extends BlackBox{
  val io = IO(new Bundle() {
    val v = Input(Bool())
  })

}
