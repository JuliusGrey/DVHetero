package TOP.CGRAPort

import RISCV.common.Param.dataW
import chisel3._
import chisel3.util._
import chisel3.util.experimental.BoringUtils
//默认riscv比cgra宽
class SynInMask(CGRAPortNum : Int , wCGRA:Int , wRISCV : Int) extends Module{
//  val innum = (CGRAPortNum*wCGRA +wRISCV - 1) /wRISCV
  val io = IO(new Bundle() {
      val valid = Input(Bool())
      val ready = Output(Bool())
      val dataIn = Input(UInt(wRISCV.W))

    val dataOut = Output(Vec(CGRAPortNum,UInt((wCGRA + 1).W)))

    val delayen = Input(Bool())
    val delayCycle = Input(UInt(wRISCV.W))
  })
  val dmaCtrl = Wire(UInt((dataW * 3).W))
  dmaCtrl := 13.U
  BoringUtils.addSink(dmaCtrl, "dmaCtrl")
//  val dmaEn = Wire(Bool())
//  dmaEn := false.B
//  BoringUtils.addSink(dmaEn, "dmaEn")


  val adrGenInst= Module(new adressGenIn(CGRAPortNum))

  val dmaCGRAInMask= dmaCtrl( 3*dataW/2- 1,dataW ).asBools()
//  val dmaCGRAInMask = 13.U(32.W).asBools()
  val mask = dmaCGRAInMask.take(CGRAPortNum)
  val ones = dmaCGRAInMask.foldLeft(0.U(5.W))((a,b) => a + b.asUInt())
  val clearCount = Mux(ones(0).asBool(),ones(4,1) + 1.U,ones(4,1))
  val updateWire = Wire(Bool())
  val clear = Wire(Bool())
  val clearCountReg = Wire(UInt(4.W))
  val clearUp = clearCountReg + 1.U === clearCount
  clearCountReg := RegEnable(
    Mux(
      clearUp,
      0.U,
      clearCountReg + 1.U
    ),
    0.U,
    updateWire,
  )
   clear := clearUp &&updateWire
  adrGenInst.io.update := updateWire
  adrGenInst.io.clear := clear
  adrGenInst.io.validList := mask
  val lastNoValid = ones(0) && (clearCountReg + 1.U === clearCount)



  val outValidsWire = Wire(Vec(CGRAPortNum, Bool()))
  val rstList = (0 until CGRAPortNum).map{index => outValidsWire(index) ||  !mask(index)}
  val dataRst = rstList.foldLeft(true.B)((a,b) => a && b)


  val delayReg = RegEnable(io.delayCycle(5, 0), 0.U, io.delayen)
  val delayCnt = Wire(UInt(6.W))
  delayCnt := RegEnable(
    Mux(
      (dataRst || delayCnt =/= 0.U) && delayReg =/= delayCnt,
      delayCnt + 1.U,
      0.U
    ),
    0.U,
    delayReg =/= 0.U
  )
  val noWait = delayCnt === 0.U


  val readyList = Wire(Vec(CGRAPortNum, Bool()))
  val inDatasWire = Wire(Vec(CGRAPortNum, UInt(wCGRA.W)))
  //这里之所以要这么修改，是要保证流水线进行，此时第一组数据在复位的时候也能输入
  for (i <- 0 until CGRAPortNum) {
    val selLow = i.U === adrGenInst.io.sel1
    val selHig = i.U === adrGenInst.io.sel2 && !lastNoValid
    val data = Mux(selLow, io.dataIn(wCGRA - 1, 0), io.dataIn(2 * wCGRA - 1, wCGRA))
    val validWhenRst = (selLow| selHig) && dataRst
    withReset((dataRst && ! validWhenRst)|| reset.asBool()) {
      readyList(i) := ((!outValidsWire(i)||validWhenRst) && noWait && mask(i))
      inDatasWire(i) := RegEnable(data, 0.U, io.valid && (!outValidsWire(i)||validWhenRst) && noWait &&(selLow || selHig)&&mask(i))
      outValidsWire(i) := RegEnable(io.valid, false.B, (!outValidsWire(i)||validWhenRst) && io.valid && noWait&&(selLow || selHig)&&mask(i))
    }
  }
  val readyMap = readyList.zipWithIndex.map {
    case (valid, index) => (index.U -> valid)
  }.toList
  val readySel = MuxLookup(
    adrGenInst.io.sel1,
    false.B,
    readyMap
  )
  io.ready := readySel
  updateWire := readySel&&io.valid

  val dmaEnWR = Wire(Bool())
  dmaEnWR := false.B
  BoringUtils.addSink(dmaEnWR, "dmaEnWR")

  for (i <- 0 until CGRAPortNum) {
    io.dataOut(i) := Cat(dataRst&&outValidsWire(i) && dmaEnWR, inDatasWire(i))
  }






}
object SynInMaskgen extends App {
  chisel3.Driver.execute(args,() => new SynInMask(16,32,64) )


}

