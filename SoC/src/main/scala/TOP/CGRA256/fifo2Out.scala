package TOP.CGRA256
import CGRA.parameter.Param.{cfgDataW, dataW}
import chisel3._
import chisel3.util._

class fifo2Out extends Module{
  val fifoNum = 16
  val indexWidth =  log2Ceil(fifoNum) + 1
  val io = IO(new Bundle {
    val en = Input(Bool())// regster the inValid
    val read = Input(Bool())//read once ; "en" must valid before "read" at last one cycle
    val dataFIFO = Input(Vec(fifoNum, UInt(dataW.W)))
    val readyFIFO = Output(Vec(fifoNum, Bool()))
    val dataOut = Output((UInt(256.W)))

    val inValid = Input(Vec(fifoNum, Bool()))

    val single = Output(Bool())
    val over = Output(Bool())
  })





  val sums = Wire((Vec(fifoNum, UInt(indexWidth.W))))
  sums(0) := io.inValid(0).asUInt()
  for (i <- 1 until fifoNum) {
    sums(i) := sums(i - 1) + io.inValid(i).asUInt()
  }

  val over = RegEnable(
    Mux(
      sums.last >= 9.U,
      true.B,
      false.B
    ),
    false.B,
    io.en
  )
  io.over := over


  io.single := RegEnable(
    Mux(
      sums.last === 1.U,
      true.B,
      false.B
    ),
    false.B,
    io.en
  )

  val selHL = Wire(Bool())
  val selHLReg = RegEnable(
    Mux(
      over,
      ~selHL,
      false.B),
    false.B,
    io.read,
  )
  selHL := selHLReg





  val index = Wire((Vec(fifoNum, UInt(indexWidth.W))))
  for( i <- 0 until fifoNum){
    index(i) := RegEnable(
      Mux(
        io.inValid(i),
        sums(i),
        0.U),
      0.U,
      io.en
    )
  }

  val map = (0 until fifoNum).map{
    i => index(i) -> io.dataFIFO(i)
  }

  val outNum = 256/dataW
  val outList = Wire(Vec(outNum,UInt(dataW.W)))
  for(i <- 0 until outNum){
    outList(i) := MuxLookup(
      (i + 1).U + Mux(selHL, 8.U,0.U),
      0.U,
      map
    )
  }
  io.dataOut := outList.asTypeOf(UInt(256.W))


//  val fifoReadValidLow = (0 until fifoNum).map{
//    i =>{
//      index(i) < 9.U
//    }
//  }.toList
//
//  val fifoReadValidHigh = (0 until fifoNum).map {
//    i => {
//      !fifoReadValidLow(i)
//    }
//  }.toList
//
//  val fifoReadValidOver = (0 until fifoNum).map {
//    i => {
//      Mux(selHL,!fifoReadValidLow(i) ,fifoReadValidLow(i))
//    }
//  }.toList

  val fifoReadValidOver = (0 until fifoNum).map {
    i => {
      selHL ^ (index(i) < 9.U)
    }
  }.toList
  (0 until fifoNum).map{
    i => {
      io.readyFIFO(i) := io.inValid(i) &&
        Mux(
          over,
          fifoReadValidOver(i),
          true.B
        )
    }
  }

}



