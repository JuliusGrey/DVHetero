package TOP.CGRA256
import RISCV.common.Param.dataW
import chisel3._
import chisel3.util._
import chisel3.util.experimental.BoringUtils
class inOrganize extends Module{
  val io = IO(new Bundle {
    val valid = Input(Bool())
    val ready = Output(Bool())
    val dataIn = Input(UInt(256.W))
    val dataOut = Output(Vec(16,UInt((32 + 1).W)))
    val delayen = Input(Bool())
    val delayCycle = Input(UInt(64.W))
    val over = Output(Bool())
  })


  val dmaCtrl = Wire(UInt((dataW * 3).W))
  dmaCtrl := 0.U
  BoringUtils.addSink(dmaCtrl, "dmaCtrl")
  val judgeType = dmaCtrl( dataW-1,  dataW/2).asUInt()



  val dmaEnWR = Wire(Bool())
  dmaEnWR := false.B
  BoringUtils.addSink(dmaEnWR, "dmaEnWR")
  val dmaEnWRL = RegNext(dmaEnWR)
  val dmaWRStart = dmaEnWR && !dmaEnWRL


  val dmaEn = Wire(Bool())
  dmaEn := false.B
  BoringUtils.addSink(dmaEn, "dmaEn")
  val dmaEnL = RegNext(dmaEn)
  val dmaStart = dmaEn && !dmaEnL

  val isMem2CGRA = judgeType === 1.U && dmaEn






  val dmaCGRAInMask = dmaCtrl( 3*dataW/2- 1,dataW ).asTypeOf(Vec(16, Bool()))



  val muxSleIns = Module(new muxSleIndex)
  muxSleIns.io.en:= dmaEnWR|| isMem2CGRA
  muxSleIns.io.inValid := dmaCGRAInMask






  val inList = io.dataIn.asTypeOf(Vec(8,UInt(32.W)))
  val inMap = (0 until 8).map{
    i => {
      (i + 1).U -> inList(i)
    }
  }

  val selHL = Wire(Bool())
  val selHLReg = RegEnable(
    Mux(
      dmaWRStart|| (dmaStart && isMem2CGRA),
      false.B,
      ~selHL),
    false.B,
    (io.valid && io.ready) || dmaWRStart || (dmaStart && isMem2CGRA)
  )
  selHL := selHLReg

  for( i <- 0 until 16){

//    val selH = muxSleIns.io.selIndex(i) - 9.U
//    val selHValid = selH(muxSleIns.io.selIndex(i).getWidth - 1).asBool()
//    val selHVal = selH(muxSleIns.io.selIndex(i).getWidth - 2 , 0)
    val selHValid = muxSleIns.io.selIndex(i) > 8.U
    val selHVal = muxSleIns.io.selIndex(i) - 8.U

//    val selIndex = Mux(
//      muxSleIns.io.over,
//      Mux(
//        selHL,
//        selHVal,
//        muxSleIns.io.selIndex(i)
//      ),
//      muxSleIns.io.selIndex(i)
//    )
val selIndex = Mux(
  muxSleIns.io.over && selHL,
  selHVal,
  muxSleIns.io.selIndex(i)
)


    val selOut = MuxLookup(
      selIndex,
      0.U,
      inMap
    )


    val outValid = Mux(
      muxSleIns.io.over,
      Mux(
        selHL,
        dmaCGRAInMask(i) && selHValid && io.valid,
        dmaCGRAInMask(i)&& !selHValid && io.valid
      ),
      dmaCGRAInMask(i) && io.valid
    )
    io.dataOut(i) := RegEnable(
      Cat(outValid, selOut),
      0.U,
       io.ready
    )


//    delayOverCnt := Mux(
//      recover0,
//      0.U,
//      Mux(
//        selHL,
//        delayCnt + 1.U,
//        Mux(
//          noWait,
//          0.U,
//          delayCnt + 1.U
//        )
//      )
//    )




//    val delayCnt = Wire(UInt(6.W))
//    delayCnt := RegEnable(
//      Mux(
//        (dataRst || delayCnt =/= 0.U) && delayReg =/= delayCnt,
//        delayCnt + 1.U,
//        0.U
//      ),
//      0.U,
//      delayReg =/= 0.U
//    )
  }

  val delayReg = RegEnable(io.delayCycle(5, 0), 0.U, io.delayen)
  val delayCnt = Wire(UInt(6.W))
  val recover0 = delayCnt === delayReg
  val noWait = delayCnt === 0.U

  val delayNoOverCnt = Wire(UInt(6.W))
  delayNoOverCnt := Mux(
    recover0,
    0.U,
    delayCnt + 1.U
  )

  val delayOverCnt = Wire(UInt(6.W))
  delayOverCnt := Mux(
    recover0 || (!selHL && noWait),
    0.U,
    delayCnt + 1.U
  )
  delayCnt := RegEnable(
    Mux(
      muxSleIns.io.over,
      delayOverCnt,
      delayNoOverCnt
    ),
    0.U,
    (dmaEnWR|| isMem2CGRA) && ((io.valid && io.ready) || !noWait)
  )


  io.ready := noWait

  io.over := muxSleIns.io.over

}

object inOrganizegen extends App {
  chisel3.Driver.execute(args,() => new inOrganize )


}
