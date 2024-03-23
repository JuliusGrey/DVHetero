package CGRA.module
import chisel3._
//class dpicCGRA(inNum : Int)  extends BlackBox {
//  val io = IO(new Bundle {
//    val ins = Input(Vec(inNum,Output(UInt(33.W))))
//    val outs = Input(Vec(2,Output(UInt(33.W))))
//  })
//
//}

object debug{
  class dpicCGRA(inNum: Int) extends BlackBox {
    val io = IO(new Bundle {
      val ins = Input(Vec(inNum, Output(UInt(33.W))))
//      val outs = Input(Vec(2, Output(UInt(33.W))))
    })

  }

  class dpicDebug extends BlackBox {
    val io = IO(new Bundle {
      val in = Input(UInt(33.W))
//      val outs = Input(Vec(2, Output(UInt(33.W))))
    })

  }

  val dbList = List("PE8")
}
