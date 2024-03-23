package CGRA.element

import chisel3._
import chisel3.util._


class IB (name :String , w : Int) extends Module {
  override val desiredName = name
  val io = IO(
    new Bundle {
      val inputs = Input(Vec(1, UInt((w + 1).W)))
      val outputs = Output(Vec(1, UInt((w + 1).W)))
    }
  )
  io.outputs(0) := io.inputs(0)
}

class OB ( name : String, inNum : Int , w :Int) extends Module{
  override val desiredName = name
  val io = IO(
    new Bundle() {
      val cfg = Input (UInt(log2Up(inNum).W))
      val inputs = Input( Vec( inNum, UInt((w + 1).W)))
      val outputs = Output(Vec(1,UInt((w + 1).W)))
    }
  )
  if(w == 1){
    io.outputs(0) := io.inputs(0)
  }else {
    val inbuffer = (0 until inNum).map(i => i.U -> io.inputs(i))
    io.outputs(0) := MuxLookup(io.cfg, 0.U(w.W), inbuffer)
  }
}
