package RISCV.TOPLEVEL

import chisel3._
import chisel3.util._
import RISCV.common.Interface.{AXIIO, cpuRWIO}
import RISCV.common.Param.{addrW, clintHigh, clintLow, dataW, skiphigh, skiplow}
class arbCpu2DCache extends Module{
  val io = IO(new Bundle() {
    val arbIn = new cpuRWIO(dataW,addrW)
    val arbMMIO = Flipped (new cpuRWIO(dataW,addrW))
    val arbClint = Flipped (new cpuRWIO(dataW,addrW))
    val arbDCache = Flipped (new cpuRWIO(dataW,addrW))
  })
  val inSourceList = List(
    io.arbIn.data_write,
    io.arbIn.wen,
    io.arbIn.addr,
    io.arbIn.rsize,
    io.arbIn.mask
  )
  val mmioSinkList = List(
    io.arbMMIO.data_write,
    io.arbMMIO.wen,
    io.arbMMIO.addr,
    io.arbMMIO.rsize,
    io.arbMMIO.mask
  )
  val clintSinkList = List(
    io.arbClint.data_write,
    io.arbClint.wen,
    io.arbClint.addr,
    io.arbClint.rsize,
    io.arbClint.mask
  )
  val dCacheSinkList = List(
    io.arbDCache.data_write,
    io.arbDCache.wen,
    io.arbDCache.addr,
    io.arbDCache.rsize,
    io.arbDCache.mask
  )

  val clinitV = io.arbIn.addr >= clintLow.U && io.arbIn.addr<clintHigh.U
  val dCacheV = io.arbIn.addr >= skiplow.U && io.arbIn.addr<skiphigh.U

  io.arbClint.valid := clinitV && io.arbIn.valid
  io.arbDCache.valid := dCacheV && io.arbIn.valid
  io.arbMMIO.valid := !clinitV && !dCacheV && io.arbIn.valid
  inSourceList.indices.map{
    i => {
      mmioSinkList(i) := inSourceList(i)
      clintSinkList(i) := inSourceList(i)
      dCacheSinkList(i) := inSourceList(i)
    }
  }

  io.arbIn.ready := (clinitV && io.arbClint.ready)|| (dCacheV && io.arbDCache.ready) || (!clinitV&& !dCacheV && io.arbMMIO.ready)
  io.arbIn.data_read :=Mux(dCacheV,io.arbDCache.data_read, Mux(clinitV,io.arbClint.data_read,io.arbMMIO.data_read))


}


class arbCpu2ICache extends Module{
  val io = IO(new Bundle() {
    val arbIn = new cpuRWIO(dataW,addrW)
    val arbMMIO = Flipped (new cpuRWIO(dataW,addrW))
    val arbICache = Flipped (new cpuRWIO(dataW,addrW))
  })
  val inSourceList = List(
    io.arbIn.data_write,
    io.arbIn.wen,
    io.arbIn.addr,
    io.arbIn.rsize,
    io.arbIn.mask
  )
  val mmioSinkList = List(
    io.arbMMIO.data_write,
    io.arbMMIO.wen,
    io.arbMMIO.addr,
    io.arbMMIO.rsize,
    io.arbMMIO.mask
  )
  val dCacheSinkList = List(
    io.arbICache.data_write,
    io.arbICache.wen,
    io.arbICache.addr,
    io.arbICache.rsize,
    io.arbICache.mask
  )

  val dCacheV = io.arbIn.addr >= skiplow.U && io.arbIn.addr<skiphigh.U


  io.arbICache.valid := dCacheV && io.arbIn.valid
  io.arbMMIO.valid :=  !dCacheV && io.arbIn.valid
  inSourceList.indices.map{
    i => {
      mmioSinkList(i) := inSourceList(i)
      dCacheSinkList(i) := inSourceList(i)
    }
  }

  io.arbIn.ready :=  (dCacheV && io.arbICache.ready) || ( !dCacheV && io.arbMMIO.ready)
  io.arbIn.data_read :=Mux(dCacheV,io.arbICache.data_read, io.arbMMIO.data_read)


}
class arbTop extends Module{
  val io = IO(new Bundle() {
    val arbIn = new cpuRWIO(dataW,addrW)
    val arbMMIO = Flipped (new cpuRWIO(dataW,addrW))
    val arbClint = Flipped (new cpuRWIO(dataW,addrW))
    val arbDCache = Flipped (new cpuRWIO(dataW,addrW))
    val arbCgra = Flipped (new cpuRWIO(dataW,addrW))
  })
  val inSourceList = List(
    io.arbIn.data_write,
    io.arbIn.wen,
    io.arbIn.addr,
    io.arbIn.rsize,
    io.arbIn.mask
  )
  val mmioSinkList = List(
    io.arbMMIO.data_write,
    io.arbMMIO.wen,
    io.arbMMIO.addr,
    io.arbMMIO.rsize,
    io.arbMMIO.mask
  )
  val clintSinkList = List(
    io.arbClint.data_write,
    io.arbClint.wen,
    io.arbClint.addr,
    io.arbClint.rsize,
    io.arbClint.mask
  )
  val dCacheSinkList = List(
    io.arbDCache.data_write,
    io.arbDCache.wen,
    io.arbDCache.addr,
    io.arbDCache.rsize,
    io.arbDCache.mask
  )
  val cgraSinkList = List(
    io.arbCgra.data_write,
    io.arbCgra.wen,
    io.arbCgra.addr,
    io.arbCgra.rsize,
    io.arbCgra.mask
  )

  val clinitV = io.arbIn.addr >= clintLow.U && io.arbIn.addr<clintHigh.U
  val dCacheV = io.arbIn.addr >= skiplow.U && io.arbIn.addr<skiphigh.U
  val mmioV = io.arbIn.addr >= skiphigh.U

  io.arbClint.valid := clinitV && io.arbIn.valid
  io.arbDCache.valid := dCacheV && io.arbIn.valid
  io.arbMMIO.valid :=mmioV && io.arbIn.valid
  io.arbCgra.valid :=   !clinitV && !dCacheV && !mmioV && io.arbIn.valid
  inSourceList.indices.map{
    i => {
      mmioSinkList(i) := inSourceList(i)
      clintSinkList(i) := inSourceList(i)
      dCacheSinkList(i) := inSourceList(i)
      cgraSinkList(i) :=  inSourceList(i)
    }
  }

  io.arbIn.ready := (clinitV && io.arbClint.ready)|| (dCacheV && io.arbDCache.ready) || (mmioV && io.arbMMIO.ready) || (!clinitV && !dCacheV && !mmioV) && io.arbCgra.ready
  io.arbIn.data_read :=Mux(dCacheV,io.arbDCache.data_read, Mux(clinitV,io.arbClint.data_read,Mux(mmioV , io.arbMMIO.data_read ,io.arbCgra.data_read)))


}

class arbCpuioGen1_N(val addrList : List[Tuple2[UInt,UInt]]) extends Module{
  val outNum = addrList.size
  val io = IO(new Bundle() {
    val arbIn = new cpuRWIO(dataW, addrW)
    val arbOuts =Vec(outNum + 1, Flipped(new cpuRWIO(dataW, addrW)))
  })

if(outNum ==0){
  io.arbIn <> io.arbOuts(0)
}else {
  val outSinks = ((0 to outNum).map {
    i =>
      List(
        io.arbOuts(i).data_write,
        io.arbOuts(i).wen,
        io.arbOuts(i).addr,
        io.arbOuts(i).rsize,
        io.arbOuts(i).mask,
        io.arbOuts(i).valid
      )
  })
  val inSrc = List(
    io.arbIn.data_write,
    io.arbIn.wen,
    io.arbIn.addr,
    io.arbIn.rsize,
    io.arbIn.mask,
    io.arbIn.valid
  )

  val hotInits = (0 until outNum).map {
    i => io.arbIn.addr >= addrList(i)._1 && io.arbIn.addr < addrList(i)._2
  }
  val hotLast = !(hotInits.foldLeft(false.B)((a, b) => a || b))
  val hots = hotInits :+ hotLast

  for (i <- 0 to outNum) {
    for(j <- 0 until outSinks(0).size)
    outSinks(i)(j) := Mux(hots(i), inSrc(j), 0.U)
  }

  io.arbIn.ready := Mux1H(
    hots,
    io.arbOuts.map(_.ready)
  )
  io.arbIn.data_read := Mux1H(
    hots,
    io.arbOuts.map(_.data_read)
  )

}
}

class arbAxiioGenN_1(val inNum :Int,dataW:Int,addrW:Int,idW:Int) extends Module {
  val io = IO(new Bundle() {
    val hots = Vec(inNum , Input(Bool()))
    val arbIns = Vec(inNum , Flipped(new AXIIO(dataW, addrW,idW)))
    val arbOut = new AXIIO(dataW, addrW,idW)
  })

  val inSources = (0 until inNum).map{
    i => List(
      io.arbIns(i).awvalid,
      io.arbIns(i).wvalid,
      io.arbIns(i).wlast,
      io.arbIns(i).bready,
      io.arbIns(i).arvalid,
      io.arbIns(i).rready,
      io.arbIns(i).awburst,
      io.arbIns(i).awsize,
      io.arbIns(i).awaddr,
      io.arbIns(i).araddr,
      io.arbIns(i).awid,
      io.arbIns(i).arid,
      io.arbIns(i).wdata,
      io.arbIns(i).awlen,
      io.arbIns(i).wstrb,
      io.arbIns(i).arlen,
      io.arbIns(i).arsize,
      io.arbIns(i).arburst,
    )
  }
  val inSinks = (0 until inNum).map{
    i => List(
      io.arbIns(i).awready,
      io.arbIns(i).wready,
      io.arbIns(i).bvalid,
      io.arbIns(i).arready,
      io.arbIns(i).rvalid,
      io.arbIns(i).rlast,
      io.arbIns(i).bresp,
      io.arbIns(i).rresp,
      io.arbIns(i).bid,
      io.arbIns(i).rid,
      io.arbIns(i).rdata,
    )
  }
  val outSink = List(
    io.arbOut.awvalid,
    io.arbOut.wvalid,
    io.arbOut.wlast,
    io.arbOut.bready,
    io.arbOut.arvalid,
    io.arbOut.rready,
    io.arbOut.awburst,
    io.arbOut.awsize,
    io.arbOut.awaddr,
    io.arbOut.araddr,
    io.arbOut.awid,
    io.arbOut.arid,
    io.arbOut.wdata,
    io.arbOut.awlen,
    io.arbOut.wstrb,
    io.arbOut.arlen,
    io.arbOut.arsize,
    io.arbOut.arburst,
  )
  val outSource = List(
    io.arbOut.awready,
    io.arbOut.wready,
    io.arbOut.bvalid,
    io.arbOut.arready,
    io.arbOut.rvalid,
    io.arbOut.rlast,
    io.arbOut.bresp,
    io.arbOut.rresp,
    io.arbOut.bid,
    io.arbOut.rid,
    io.arbOut.rdata,
  )

  for( i <- 0 until outSink.size) {
    outSink(i) := Mux1H(io.hots, inSources.map(_(i)))
  }



  (0 until inNum).map{
    i => {
      for(j <-  0 until  outSource.size)
      inSinks(i)(j) := Mux(io.hots(i),outSource(j),0.U)
    }
  }
}


class arbCgraGen1_N(val addrList : List[UInt]) extends Module{
  val outNum = addrList.size
  val io = IO(new Bundle() {
    val arbIn = new cpuRWIO(dataW, addrW)
    val arbOuts =Vec(outNum, Flipped(new cpuRWIO(dataW, addrW)))
  })

  {
    val outSinks = ((0 until outNum).map {
      i =>
        List(
          io.arbOuts(i).data_write,
          io.arbOuts(i).wen,
          io.arbOuts(i).addr,
          io.arbOuts(i).rsize,
          io.arbOuts(i).mask,
          io.arbOuts(i).valid
        )
    })
    val inSrc = List(
      io.arbIn.data_write,
      io.arbIn.wen,
      io.arbIn.addr,
      io.arbIn.rsize,
      io.arbIn.mask,
      io.arbIn.valid
    )

    val hots = (0 until outNum).map {
      i => io.arbIn.addr === addrList(i)
    }


    for (i <- 0 until outNum) {
      for (j <- 0 until outSinks(0).size)
        outSinks(i)(j) := Mux(hots(i), inSrc(j), 0.U)
    }

    io.arbIn.ready := Mux1H(
      hots,
      io.arbOuts.map(_.ready)
    )
    io.arbIn.data_read := Mux1H(
      hots,
      io.arbOuts.map(_.data_read)
    )
  }
}

object arbgen extends App {
    val outAddrs = (0 until 16).map(4*_).map(_.U ).toList
//  //  val arbInst = Module(new  arbCgraGen1_N(outAddrs :+ addrDelay256))
  chisel3.Driver.execute(args,() => new arbCgraGen1_N(outAddrs) )


}
