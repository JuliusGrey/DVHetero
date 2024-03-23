package TOP.PAD
import chisel3._
import chisel3.util._
class PIS extends BlackBox {
  val io = IO(new Bundle {
    val IE = Input(UInt(1.W))
    val PAD = Input(Bool())
    val C = Output(Bool())
  })
}
