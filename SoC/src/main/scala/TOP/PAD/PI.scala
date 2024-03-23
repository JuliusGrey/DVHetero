package TOP.PAD
import chisel3._
class PI extends BlackBox {
  val io = IO(new Bundle {
    val IE = Input(UInt(1.W))
    val PAD = Input(Bool())
    val C = Output(Bool())
  })
}
