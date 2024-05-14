
import chisel3._
import chisel3.util._

class MyArbiter extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Vec(3, Decoupled(UInt(8.W))))
    val out = Decoupled(UInt(8.W))
    val chosen = Output(UInt())
  })
//  withReset(reset) {
    val arbiter = Module(new RRArbiter(UInt(8.W), 3)) // 2 to 1 Priority Arbiter
//    }
  arbiter.reset<> reset
  arbiter.io.in <> io.in
  io.out <> arbiter.io.out
  io.chosen := arbiter.io.chosen
}

object riscvgen extends App {
  chisel3.Driver.execute(args,() => new MyArbiter )


}