package TOP.CGRA256
import chisel3._
import chisel3.util._
class CGRA2FIFOSingle extends Module{
  val fifoNum = 16
  val io = IO(new Bundle {
    val validList = Vec(fifoNum ,Input (Bool()))
    val start = Input(Bool())//强制归0，用于dmast指令
    val clear = Input(Bool())//处理完fifo中的数据后归0，用于batch结束
//    val waitcClear = Output(Bool())
    val fifoIn = Vec(fifoNum , new fifoPort)
    val fifoOut = Flipped(Vec(8 , new fifoPort))
  })
  val inValid = Mux1H(
    io.validList,
    io.fifoIn.map(_.fifoValid)
  )
  val inVal = Mux1H(
    io.validList,
    io.fifoIn.map(_.fifoData)
  )

  val cnt = Wire(UInt(3.W))
  val cntReg = RegEnable(
    Mux(
      inValid && !io.start,
      cnt + 1.U,
      0.U
    ),
    0.U,
    inValid || io.clear||io.start
  )
  cnt := cntReg

  for( i <- 0 until 8){
    io.fifoOut(i).fifoValid := (cnt === i.U)&& inValid
    io.fifoOut(i).fifoData := inVal
  }
  dontTouch(io)
  dontTouch(cnt)
  dontTouch(inVal)
  dontTouch(inValid)

//  io.waitcClear :=  inValid







}
object CGRA2FIFOSinglegen extends App {
  chisel3.Driver.execute(args,() => new CGRA2FIFOSingle )


}