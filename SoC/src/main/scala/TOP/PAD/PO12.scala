package TOP.PAD
import chisel3._
class PO12 extends BlackBox {
  val io = IO(new Bundle {
    val I = Input(UInt(1.W))
    val PAD = Output(Bool())
  })
}
