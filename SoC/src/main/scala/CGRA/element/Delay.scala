package CGRA.element

import chisel3._
import chisel3.util._

class Delay(name : String ,  maxDelay: Int, width: Int) extends Module{
  override val desiredName = name
  val io = IO(new Bundle {
    val cfg = Input(UInt(log2Up(maxDelay).W))
    val inputs = Input(Vec(1, UInt((width + 1).W)))
    val outputs = Output(Vec(1, UInt((width + 1).W)))
  })
  val idleSt:: buzySt :: Nil = Enum(2)
  val state = RegInit(0.U(1.W))
  val isIdle = state===idleSt
  val cntWire = Wire(UInt(log2Up(maxDelay).W))
  val buzyEnd = cntWire === io.cfg - 1.U
  val idleMux = Mux(io.cfg =/= 0.U & io.inputs(0)(width),buzySt,idleSt )
  val buzyMux = Mux(buzyEnd,idleSt,buzySt)

  cntWire := RegNext(
    Mux(
      isIdle,
      0.U,
      cntWire + 1.U
    ),
    0.U
  )
  state := Mux(isIdle,idleMux,buzyMux)

  val dataReg = RegEnable(io.inputs(0)(width - 1, 0),0.U,io.inputs(0)(width).asBool() && (io.cfg =/=0.U))

  io.outputs(0) := Mux(
    io.cfg === 0.U,
    io.inputs(0),
    Cat(!isIdle && buzyEnd, dataReg)
  )




}
