package TOP.CGRA256
import CGRA.ARCH.FullConnnect.CGRAFCGen.CGRAFCModule
import CGRA.ARCH.moduleIns.CGRAGen.CGRAModule
import CGRA.module.TopModule.topGen
import Chisel.log2Ceil
import RISCV.TOPLEVEL.arbCgraGen1_N
import RISCV.common.Interface.cpuRWIO
import RISCV.common.Param.addrW
import TOP.paramG._
import chisel3._
import chisel3.util._
class CGRAFull256 extends Module{
  val ionum = 16
  val io = IO(new Bundle {
    val mmIO = new cpuRWIO(64, addrW)
    val dmaIO = new cpuRWIO(256, addrW)

    val batchOver = Input(Bool())
    val batchLastNum = Input(UInt(3.W))

    val inOver8 = Output(Bool())
    val outOver8 = Output(Bool())

    val signle = Output(Bool())
  })
  val inOrganizeInst = Module(new inOrganize)
  val cgraInst = if(cgraFC) Module(topGen(CGRAFCModule, "CGRA.txt")) else  Module(topGen(CGRAModule, "CGRA.txt"))
  val outOrganizeInst = Module(new outOrganize)

  val isIn = io.dmaIO.addr === addrIn256
  val isOut = io.dmaIO.addr === addrOut256
  val isCfg = io.dmaIO.addr === addrCfg256
  val isDelay = io.dmaIO.addr === addrDelay256
  val read = io.dmaIO.valid && !io.dmaIO.wen
  val write = io.dmaIO.valid && io.dmaIO.wen

  val oneHSel = Cat(isIn,isOut,isCfg||isDelay)
  val readyList:List[Bool] = List(true.B,outOrganizeInst.io.outValid,inOrganizeInst.io.ready)
  io.dmaIO.ready := Mux1H(oneHSel,readyList)
  io.dmaIO.data_read := outOrganizeInst.io.outData
  io.inOver8 := inOrganizeInst.io.over
  io.outOver8 := outOrganizeInst.io.over

  inOrganizeInst.io.valid := isIn && write
  assert(!(isIn &&(io.dmaIO.valid ^ io.dmaIO.wen)))
  inOrganizeInst.io.dataIn := io.dmaIO.data_write
//  inOrganizeInst.io.delayen := isDelay && write
  assert(!(isDelay &&(io.dmaIO.valid ^ io.dmaIO.wen)))
  inOrganizeInst.io.delayCycle:=io.dmaIO.data_write

  cgraInst.io.inputs.zipWithIndex.foreach{
    case(inPort , index) => {
      inPort := inOrganizeInst.io.dataOut(index)
    }
  }
  val cfgData = io.dmaIO.data_write(31,0)
  val cfgAddr = io.dmaIO.data_write(63,32)
  cgraInst.io.cfgEn := isCfg && write
  assert(!(isCfg &&(io.dmaIO.valid ^ io.dmaIO.wen)))
  cgraInst.io.cfgAddr := cfgAddr
  cgraInst.io.cfgData := cfgData

  outOrganizeInst.io.fifo.zipWithIndex.map{
    case(port , index) => {
      port.fifoValid := cgraInst.io.outputs(index)(32)
      port.fifoData := cgraInst.io.outputs(index)(31,0)
    }
  }
  outOrganizeInst.io.outReady := isOut && read
  outOrganizeInst.io.batchOver := io.batchOver
  outOrganizeInst.io.batchLastNum := io.batchLastNum

  io.signle := outOrganizeInst.io.signle

//
  val outAddrs = List(
    "h02010040".U,
    "h02010044".U,
    "h02010048".U,
    "h0201004C".U,
    "h02010050".U,
    "h02010054".U,
    "h02010058".U,
    "h0201005C".U,
    "h02010060".U,
    "h02010064".U,
    "h02010068".U,
    "h0201006C".U,
    "h02010070".U,
    "h02010074".U,
    "h02010078".U,
    "h0201007C".U,
  )
//  val outAddrs = (0 until ionum).map(4*_).map(_.U + addrOut256).toList
  val arbInst = Module(new  arbCgraGen1_N(outAddrs :+ addrDelay256))
  io.mmIO <> arbInst.io.arbIn
  (0 until ionum ).map {
    i => {
      outOrganizeInst.io.fifoOut(i).fifoReady := arbInst.io.arbOuts(i).valid
      arbInst.io.arbOuts(i).ready := outOrganizeInst.io.fifoOut(i).fifoValid
      arbInst.io.arbOuts(i).data_read :=Mux(io.mmIO.addr(2).asBool(), Cat(outOrganizeInst.io.fifoOut(i).fifoData,0.U(32.W)) ,outOrganizeInst.io.fifoOut(i).fifoData)
    }
  }
  inOrganizeInst.io.delayen := (arbInst.io.arbOuts(ionum ).valid)&&arbInst.io.arbOuts(ionum ).wen
  inOrganizeInst.io.delayCycle := arbInst.io.arbOuts(ionum).data_write
  arbInst.io.arbOuts(ionum ).ready := true.B
  arbInst.io.arbOuts(ionum ).data_read := 0.U
//val oneHList2 = (0 until (cgraInst.io.outputs.size)).map {
//  i => ("h02010000".U + (8 * 8 + i * 4).U) === io.CGRAIO.addr
//}

//  val outAddrs = (0 until ionum).map {
//    i => "h02010040".U + 1.U
//  }.toList
//  val arbInst = Module(new arbCgraGen1_N(outAddrs :+ addrDelay256))
//  arbInst.io.arbIn<>DontCare
//  arbInst.io.arbOuts<>DontCare
//  io.mmIO <> DontCare
//  (0 until ionum ).map {
//    i => {
//      outOrganizeInst.io.fifoOut(i).fifoReady := DontCare
////      arbInst.io.arbOuts(i).ready := outOrganizeInst.io.fifoOut(i).fifoValid
////      arbInst.io.arbOuts(i).data_read := outOrganizeInst.io.fifoOut(i).fifoData
//    }
//  }
//  inOrganizeInst.io.delayen := DontCare
//  inOrganizeInst.io.delayCycle := DontCare
////  arbInst.io.arbOuts(ionum - 1).ready := true.B



}


object CGRAFull256Gen extends App {

  chisel3.Driver.execute(args,() => new CGRAFull256 )
//  val inVal = 64
//  val test = log2Ceil(log2Ceil(inVal/8) + 1)
//  println( "test is " + test)


}
