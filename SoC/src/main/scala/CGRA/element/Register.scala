package CGRA.element

import chisel3._
import chisel3.util._

class Register(name: String, w: Int) extends Module {
  override val desiredName = name
  val io = IO(new Bundle {
    //    val en = Input(Bool())
    val inputs = Input(Vec(1, UInt((w + 1).W)))
    val outputs = Output(Vec(1, UInt((w + 1).W)))
    //    val valid = Output(Bool())
  })
  //  val Reg = RegInit(0.U(w.W))
  //  when(io.en) {
  //    Reg := io.inputs(0)
  //  }
  //  val outWire = Wire(UInt((w+1).W))
  //  outWire :=RegNext(Cat(io.en,io.inputs(0)),0.U)
  //  io.outputs(0) := outWire(w -1 , 0 )
  //  io.valid := outWire(w)
  io.outputs(0) := RegNext(io.inputs(0),0.U)
}
