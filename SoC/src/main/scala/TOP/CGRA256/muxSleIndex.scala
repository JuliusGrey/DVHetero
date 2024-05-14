package TOP.CGRA256
import chisel3._
import chisel3.util._
class muxSleIndex extends Module{
  val ioNum = 16
  val indexWidth = log2Ceil(ioNum) + 1
  val io = IO(new Bundle() {
    val inValid = Input(Vec(ioNum, Bool()))
    val selIndex = Output(Vec(ioNum,UInt(indexWidth.W)))
    val en = Input(Bool())
    val over = Output(Bool())
  })

  val sums = Wire((Vec(ioNum,UInt(indexWidth.W))))
  sums(0) := io.inValid(0).asUInt()
  for(i <- 1 until ioNum){
    sums(i) := sums(i - 1) + io.inValid(i).asUInt()
  }

//  val sum = sums.asTypeOf(UInt((indexWidth*ioNum).W))

  val sumReg = RegEnable(sums.asTypeOf(UInt((indexWidth*ioNum).W)),0.U,io.en)
  val selIndexReg = sumReg.asTypeOf(Vec(ioNum,UInt(indexWidth.W)))
  (0 until ioNum).map{ i => io.selIndex(i) := Mux(io.inValid(i) ,selIndexReg(i),0.U)}

  io.over := selIndexReg.last > 8.U

}

object muxSleIndexGen extends App {

  chisel3.Driver.execute(args,() => new muxSleIndex )


}
