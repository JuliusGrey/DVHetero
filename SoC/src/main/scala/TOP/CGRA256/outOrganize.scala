package TOP.CGRA256
import RISCV.common.Param.dataW
import chisel3._
import chisel3.util._
import chisel3.util.experimental.BoringUtils

class fifoPort extends Bundle{
  val fifoValid = Input(Bool())
//  val fifoReady = Output(Bool())
  val fifoData = Input(UInt(32.W))
}
class fifoPortTrue extends Bundle{
  val fifoValid = Input(Bool())
  val fifoReady = Output(Bool())
  val fifoData = Input(UInt(32.W))
}
class outOrganize  extends Module{
  val fifoNum = 16
  val fifoDeepth = 128
  val io = IO(new Bundle{
    val fifo = Vec(fifoNum , new fifoPort)
    val outValid = Output(Bool())
    val outReady = Input(Bool())
    val outData = Output(UInt(256.W))


    //for sig
    val batchOver = Input(Bool())
    val batchLastNum = Input(UInt(3.W))

    val over = Output(Bool())

    val signle = Output(Bool())

    val fifoOut = Vec(fifoNum,Flipped(new fifoPortTrue))
  })


  val dmaCtrl = Wire(UInt((dataW * 3).W))
  dmaCtrl := 0.U
  BoringUtils.addSink(dmaCtrl, "dmaCtrl")
  val judgeType = dmaCtrl( dataW - 1,  dataW/2).asUInt()


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

  val isCGRA2Mem = judgeType === 1.U && dmaEn
  val isMem2CGRA = judgeType === 2.U && dmaEn







  val dmaCGRAOutMask = dmaCtrl( 2*dataW - 1,3*dataW/2 ).asUInt()
  val maskList = dmaCGRAOutMask(15,0).asBools()

  val outQueues = List.fill(fifoNum)(Module(new Queue(UInt(32.W), fifoDeepth)))
  val fifo2OutInst = Module(new fifo2Out)
  val CGRA2FIFOSingleIns = Module(new CGRA2FIFOSingle)

  CGRA2FIFOSingleIns.io.clear := io.batchOver && io.outValid && io.outReady
  CGRA2FIFOSingleIns.io.start := dmaWRStart || (dmaStart && (isMem2CGRA || isCGRA2Mem))
  CGRA2FIFOSingleIns.io.validList := maskList
  for(i <- 0 until fifoNum){
    CGRA2FIFOSingleIns.io.fifoIn(i).fifoValid := io.fifo(i).fifoValid && fifo2OutInst.io.single
    CGRA2FIFOSingleIns.io.fifoIn(i).fifoData := io.fifo(i).fifoData
  }



  for(i <- 0 until fifoNum){
    outQueues(i).io.enq.valid := Mux(
      fifo2OutInst.io.single,
      if(i < 8 )CGRA2FIFOSingleIns.io.fifoOut(i).fifoValid else 0.U,
      io.fifo(i).fifoValid)

    if(i < 8)
     outQueues(i).io.enq.bits :=  Mux(fifo2OutInst.io.single,CGRA2FIFOSingleIns.io.fifoOut(i).fifoData, io.fifo(i).fifoData)
    else
      outQueues(i).io.enq.bits := io.fifo(i).fifoData
    //    io.fifo(i).fifoReady := outQueues(i).io.enq.ready
    if(i <8)
     outQueues(i).io.deq.ready := Mux(fifo2OutInst.io.single , io.outReady && io.outValid, fifo2OutInst.io.readyFIFO(i) && io.outReady && io.outValid) || io.fifoOut(i).fifoReady
    else
      outQueues(i).io.deq.ready := (fifo2OutInst.io.readyFIFO(i) && io.outReady && io.outValid)|| io.fifoOut(i).fifoReady
  }


  fifo2OutInst.io.en := dmaWRStart ||(dmaStart && (isMem2CGRA||isCGRA2Mem))
  fifo2OutInst.io.read := io.outReady && io.outValid
  for(i <- 0 until fifoNum){
    fifo2OutInst.io.dataFIFO(i) := outQueues(i).io.deq.bits
    fifo2OutInst.io.inValid(i) := maskList(i)
  }

  val outValidMul = (0 until fifoNum).map{
    i => {
      fifo2OutInst.io.readyFIFO(i) === outQueues(i).io.deq.valid
    }
  }.foldLeft(true.B)((a, b) => a && b)
  val outDataMul = fifo2OutInst.io.dataOut


  val batchLastSum = outQueues.slice(0,8).foldLeft(0.U(3.W))((a,b) => a + b.io.deq.valid.asUInt())
  val outValidSig= Mux(
    io.batchOver && io.batchLastNum =/=0.U,
    batchLastSum === io.batchLastNum,
    outQueues.slice(0,8).foldLeft(true.B)((a, b) => a && b.io.deq.valid)
  )
  val outDataSignal = Cat(outQueues.slice(0,8).reverse.map(_.io.deq.bits))

  io.outValid:= Mux(
    fifo2OutInst.io.single,
    outValidSig,
    outValidMul
  )
  io.outData := Mux(
    fifo2OutInst.io.single,
    outDataSignal,
    outDataMul
  )

  io.over := fifo2OutInst.io.over
  io.signle := fifo2OutInst.io.single


  for( i <- 0 until fifoNum) {
    io.fifoOut(i).fifoData := outQueues(i).io.deq.bits
    io.fifoOut(i).fifoValid := outQueues(i).io.deq.valid
  }

}

object outOrganizegen extends App {
  chisel3.Driver.execute(args,() => new outOrganize )


}
