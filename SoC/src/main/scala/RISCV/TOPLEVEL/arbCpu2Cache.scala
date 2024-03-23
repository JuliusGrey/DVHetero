package RISCV.TOPLEVEL

import chisel3._
import chisel3.util._
import RISCV.common.Interface.cpuRWIO
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