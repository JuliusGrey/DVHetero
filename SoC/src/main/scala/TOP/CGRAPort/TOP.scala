package TOP.CGRAPort

import RISCV.BUS.AXICache
import RISCV.BUS.mem.{Dcache, Icache, mem, mmioCache}
import RISCV.DMA.DMACtrl
import RISCV.TOPLEVEL.{arbCpu2DCache, clint}
import RISCV.common.Interface.AXIIO
import RISCV.riscv
import chisel3._
import chisel3.util.experimental.BoringUtils

class TOP extends Module{
  val io = IO(new Bundle() {
    val dmaster = new AXIIO(64,32,4)
  })

  val cgraFullIns = Module(new CGRAFull)
  val dmaIns = Module(new DMACtrl)

  val riscvIns = Module(new riscv)
  val iCache = Module(new Icache)
  riscvIns.io.instIO <>iCache.io.cacheIn
  val axiIIO = Module(new AXICache)
  axiIIO.io.cache <> iCache.io.cacheOut

  val dArbIns = Module(new arbCpu2DCache)
  val mmioDCache = Module(new mmioCache)
  val dCache = Module(new Dcache)
  val  clintIns = Module(new clint)
  riscvIns.io.dataIO<>dArbIns.io.arbIn
  dArbIns.io.arbMMIO<>mmioDCache.io.mmioIn
  dArbIns.io.arbDCache <>dCache.io.cacheIn
  dArbIns.io.arbClint<>clintIns.io.clintIO
  //  val axiDMMIO = Module(new AXI4)
  val axiDIO = Module(new AXICache)
  //    mmioDCache.io.mmioOut<>axiDMMIO.io.vrIO
  axiDIO.io.cache<>dCache.io.cacheOut

  //  val memList = Vec(8,Module(new mem))
  (0 until 4).map{
    i => {
      val imem = Module(new mem)
      val dmem = Module(new mem)
      imem.io.memIO<> iCache.io.SRAMIO(i)
      dmem.io.memIO<>dCache.io.SRAMIO(i)
    }
  }
  val block2 = Wire(Bool())
  block2 := false.B
  BoringUtils.addSink(block2, "block2")
  val block3 = Wire(Bool())
  block3 := false.B
  BoringUtils.addSink(block3, "block3")
  iCache.io.block := block3
  mmioDCache.io.block := block2
  dCache.io.block := block2
  //
  //  io.imaster <> axiIIO.io.axiIO
  val iCacheAXIOUTList = List(
    axiIIO.io.axiIO.awvalid,
    axiIIO.io.axiIO.wvalid,
    axiIIO.io.axiIO.wlast,
    axiIIO.io.axiIO.bready,
    axiIIO.io.axiIO.arvalid,
    axiIIO.io.axiIO.rready,
    axiIIO.io.axiIO.awburst,
    axiIIO.io.axiIO.awsize,
    axiIIO.io.axiIO.awaddr,
    axiIIO.io.axiIO.araddr,
    axiIIO.io.axiIO.awid,
    axiIIO.io.axiIO.arid,
    axiIIO.io.axiIO.wdata,
    axiIIO.io.axiIO.awlen,
    axiIIO.io.axiIO.wstrb,
    axiIIO.io.axiIO.arlen,
    axiIIO.io.axiIO.arsize,
    axiIIO.io.axiIO.arburst,

  )
  //
  val iCacheAXIINList = List(
    axiIIO.io.axiIO.awready,
    axiIIO.io.axiIO.wready,
    axiIIO.io.axiIO.bvalid,
    axiIIO.io.axiIO.arready,
    axiIIO.io.axiIO.rvalid,
    axiIIO.io.axiIO.rlast,
    axiIIO.io.axiIO.bresp,
    axiIIO.io.axiIO.rresp,
    axiIIO.io.axiIO.bid,
    axiIIO.io.axiIO.rid,
    axiIIO.io.axiIO.rdata,

  )
  val dmasterOutList = List(
    io.dmaster.awvalid,
    io.dmaster.wvalid,
    io.dmaster.wlast,
    io.dmaster.bready,
    io.dmaster.arvalid,
    io.dmaster.rready,
    io.dmaster.awburst,
    io.dmaster.awsize,
    io.dmaster.awaddr,
    io.dmaster.araddr,
    io.dmaster.awid,
    io.dmaster.arid,
    io.dmaster.wdata,
    io.dmaster.awlen,
    io.dmaster.wstrb,
    io.dmaster.arlen,
    io.dmaster.arsize,
    io.dmaster.arburst,

  )
  //
  val dmasterInList = List(
    io.dmaster.awready,
    io.dmaster.wready,
    io.dmaster.bvalid,
    io.dmaster.arready,
    io.dmaster.rvalid,
    io.dmaster.rlast,
    io.dmaster.bresp,
    io.dmaster.rresp,
    io.dmaster.bid,
    io.dmaster.rid,
    io.dmaster.rdata,

  )
  val axiDIOOutList = List(
    axiDIO.io.axiIO.awvalid,
    axiDIO.io.axiIO.wvalid,
    axiDIO.io.axiIO.wlast,
    axiDIO.io.axiIO.bready,
    axiDIO.io.axiIO.arvalid,
    axiDIO.io.axiIO.rready,
    axiDIO.io.axiIO.awburst,
    axiDIO.io.axiIO.awsize,
    axiDIO.io.axiIO.awaddr,
    axiDIO.io.axiIO.araddr,
    axiDIO.io.axiIO.awid,
    axiDIO.io.axiIO.arid,
    axiDIO.io.axiIO.wdata,
    axiDIO.io.axiIO.awlen,
    axiDIO.io.axiIO.wstrb,
    axiDIO.io.axiIO.arlen,
    axiDIO.io.axiIO.arsize,
    axiDIO.io.axiIO.arburst,

  )
  //
  val axiDIOInList = List(
    axiDIO.io.axiIO.awready,
    axiDIO.io.axiIO.wready,
    axiDIO.io.axiIO.bvalid,
    axiDIO.io.axiIO.arready,
    axiDIO.io.axiIO.rvalid,
    axiDIO.io.axiIO.rlast,
    axiDIO.io.axiIO.bresp,
    axiDIO.io.axiIO.rresp,
    axiDIO.io.axiIO.bid,
    axiDIO.io.axiIO.rid,
    axiDIO.io.axiIO.rdata,

  )

  val dmaInsOutList = List(
    dmaIns.io.dataIn.awvalid,
    dmaIns.io.dataIn.wvalid,
    dmaIns.io.dataIn.wlast,
    dmaIns.io.dataIn.bready,
    dmaIns.io.dataIn.arvalid,
    dmaIns.io.dataIn.rready,
    dmaIns.io.dataIn.awburst,
    dmaIns.io.dataIn.awsize,
    dmaIns.io.dataIn.awaddr,
    dmaIns.io.dataIn.araddr,
    dmaIns.io.dataIn.awid,
    dmaIns.io.dataIn.arid,
    dmaIns.io.dataIn.wdata,
    dmaIns.io.dataIn.awlen,
    dmaIns.io.dataIn.wstrb,
    dmaIns.io.dataIn.arlen,
    dmaIns.io.dataIn.arsize,
    dmaIns.io.dataIn.arburst,

  )
  //
  val dmaInsInList = List(
    dmaIns.io.dataIn.awready,
    dmaIns.io.dataIn.wready,
    dmaIns.io.dataIn.bvalid,
    dmaIns.io.dataIn.arready,
    dmaIns.io.dataIn.rvalid,
    dmaIns.io.dataIn.rlast,
    dmaIns.io.dataIn.bresp,
    dmaIns.io.dataIn.rresp,
    dmaIns.io.dataIn.bid,
    dmaIns.io.dataIn.rid,
    dmaIns.io.dataIn.rdata,

  )
  val DMABuzy = Wire(Bool())
  DMABuzy := false.B
  BoringUtils.addSink(DMABuzy, "DMABuzy")
  for(i <- 0 until dmasterOutList.size){
    dmasterOutList(i) := Mux(
      DMABuzy,
      dmaInsOutList(i),
      Mux(
        riscvIns.io.instIO.valid && !riscvIns.io.instIO.ready,
        iCacheAXIOUTList(i),
        axiDIOOutList(i))
    )
  }
  for( i<- 0 until dmasterInList.size){
    dmaInsInList(i) := dmasterInList(i)
    axiDIOInList(i) := dmasterInList(i)
    iCacheAXIINList(i) := dmasterInList(i)
  }


  val ioMMIOOutList = List(
    cgraFullIns.io.CGRAIO.valid,
    cgraFullIns.io.CGRAIO.data_write,
    cgraFullIns.io.CGRAIO.wen,
    cgraFullIns.io.CGRAIO.addr ,
    cgraFullIns.io.CGRAIO.rsize,
    cgraFullIns.io.CGRAIO.mask
  )
  val ioMMIOInList = List(
    cgraFullIns.io.CGRAIO.ready,
    cgraFullIns.io.CGRAIO.data_read
  )
  val dmaMMIOOutList = List(
    dmaIns.io.dataOutMMIO.valid,
    dmaIns.io.dataOutMMIO.data_write,
    dmaIns.io.dataOutMMIO.wen,
    dmaIns.io.dataOutMMIO.addr,
    dmaIns.io.dataOutMMIO.rsize,
    dmaIns.io.dataOutMMIO.mask
  )
  val dmaMMIOInList = List(
    dmaIns.io.dataOutMMIO.ready,
    dmaIns.io.dataOutMMIO.data_read
  )
  val dCacheMMIOOutList = List(
    mmioDCache.io.mmioOut.valid,
    mmioDCache.io.mmioOut.data_write,
    mmioDCache.io.mmioOut.wen,
    mmioDCache.io.mmioOut.addr,
    mmioDCache.io.mmioOut.rsize,
    mmioDCache.io.mmioOut.mask
  )
  val dCacheMMIOInList = List(
    mmioDCache.io.mmioOut.ready,
    mmioDCache.io.mmioOut.data_read
  )

  for (i <- 0 until ioMMIOOutList.size) {
    ioMMIOOutList(i) := Mux(
      DMABuzy,
      dmaMMIOOutList(i),
      dCacheMMIOOutList(i)
    )
  }
  for (i <- 0 until ioMMIOInList.size) {
    dmaMMIOInList(i) := ioMMIOInList(i)
    dCacheMMIOInList(i) := ioMMIOInList(i)
  }
}

object topGen2 extends App {

  chisel3.Driver.execute(args,() => new TOP )


}