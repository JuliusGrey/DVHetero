package CGRA.element

import chisel3._
import chisel3.util._

class ConstUnit (name :String , w : Int) extends Module {
  override val desiredName = name
  val io = IO (
    new Bundle{
      val cfg = Input(UInt(w.W))
      val outputs = Output(Vec(1, UInt((w + 1).W)))
    }
  )
  io.outputs(0) := Cat(true.B, io.cfg)
}
