package CGRA.element

import chisel3._
import chisel3.util._

class Multiplexer ( name : String, muxSize : Int , w :Int) extends Module{
  override val desiredName = name
  val io = IO(
    new Bundle() {
      val cfg = Input (UInt(log2Up(muxSize).W))
      val inputs = Input( Vec( muxSize, UInt((w + 1).W)))
      val outputs = Output(Vec(1,UInt((w + 1).W)))
    }
  )
  val inbuffer = (0 until muxSize ).map(i => i.U -> io.inputs(i))
  io.outputs(0) := MuxLookup(io.cfg, 0.U(w.W), inbuffer)
}
