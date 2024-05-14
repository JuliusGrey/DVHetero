package TOP

import RISCV.BUS.AXICache
import RISCV.BUS.mem._
import RISCV.DMA.dma256
import RISCV.TOPLEVEL._
import RISCV.common.Interface.{AXIIO, cpuRWIO}
import RISCV.common.Param.{addrW, dataW, skiphigh, skiplow}
import RISCV.riscv
import TOP.CGRA256.CGRAFull256
import TOP.paramG._
import chisel3._
import chisel3.util.experimental.BoringUtils
class TopOK extends Module{
  val io = IO(new Bundle {
    val cupIO = new AXIIO(64,32,4)
    val CGRAIO = new AXIIO(256,32,4)
    val mmio = Flipped (new cpuRWIO(dataW , addrW ))
  })

  val riscvIns = Module(new riscv)
  val arbCpuData = Module(new arbCpuioGen1_N(List((addrCGRAbase,addrCGRAHigh),(clintLow.U,clintHigh.U),(skiplow.U,skiphigh.U))))
  val iCache = Module(new Icache)
  val axiIIO = Module(new AXICache)
  val dCache = Module(new Dcache)
  val axiDIO = Module(new AXICache)
  val arbAXI = Module(new arbAxiioGenN_1(2,64,32,4))
  val cgra = Module(new CGRAFull256)
  val dma256 = Module(new dma256)
  val  clintIns = Module(new clint)
  (0 until 4).map {
    i => {
      val imem = Module(new mem)
      val dmem = Module(new mem)
      imem.io.memIO <> iCache.io.SRAMIO(i)
      dmem.io.memIO <> dCache.io.SRAMIO(i)
    }
  }



  riscvIns.io.instIO <> iCache.io.cacheIn
  axiIIO.io.cache <> iCache.io.cacheOut

  riscvIns.io.dataIO <> arbCpuData.io.arbIn
  arbCpuData.io.arbOuts(0)<> cgra.io.mmIO
  arbCpuData.io.arbOuts(1)<> clintIns.io.clintIO
  arbCpuData.io.arbOuts(2)<> dCache.io.cacheIn
  arbCpuData.io.arbOuts(3)<> io.mmio
  axiDIO.io.cache <> dCache.io.cacheOut

    io.CGRAIO <>dma256.io.dataAXI
  dma256.io.dataCGRA <> cgra.io.dmaIO

  dma256.io.cgraInOver8 := cgra.io.inOver8
  dma256.io.cgraOutOver8 := cgra.io.outOver8
  dma256.io.cgraOutSignle := cgra.io.signle

  cgra.io.batchOver := dma256.io.batchOver
  cgra.io.batchLastNum := dma256.io.batchLastNum


  val block2 = Wire(Bool())
  block2 := false.B
  BoringUtils.addSink(block2, "block2")
  val block3 = Wire(Bool())
  block3 := false.B
  BoringUtils.addSink(block3, "block3")
  iCache.io.block := block3
  dCache.io.block := block2

  val instHot = riscvIns.io.instIO.valid && !riscvIns.io.instIO.ready
  arbAXI.io.arbIns(0)<>axiIIO.io.axiIO
  arbAXI.io.hots(0) := instHot

  arbAXI.io.arbIns(1)<>axiDIO.io.axiIO
  arbAXI.io.hots(1) := !instHot

  io.cupIO <> arbAXI.io.arbOut


  io.mmio.valid :=  arbCpuData.io.arbOuts(3).valid && !block2//防止输出两次
}

object TopOKGen extends App {

  chisel3.Driver.execute(args,() => new TopOK )


}
