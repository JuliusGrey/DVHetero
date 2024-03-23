package RISCV.BUS.mem

import chisel3._
import chisel3.util._
import chisel3.util.experimental.BoringUtils
import RISCV.common.Interface.{SignalRAM, cacheIO, cpuRWIO}
import RISCV.common.Param.{CacheSram, SRAMaddrW, SRAMdataW, addrW, dataW}
import TOP.paramG.memIP

import scala.collection.mutable.ListBuffer
//way 就等于2吧，主要参数在set
class IcachenoIP() extends Module{
  val io = IO( new Bundle() {
    val cacheOut = new cacheIO(dataW,addrW)
    val cacheIn = new cpuRWIO(dataW,addrW)
    val SRAMIO = Vec(CacheSram,new SignalRAM(SRAMdataW,SRAMaddrW))
  })

  val updataICache = Wire(Bool())
  updataICache :=false.B
  BoringUtils.addSink(updataICache, "updataICache")



  val deepth = 64
  //数据位处理
  val offW = log2Up(SRAMdataW / 8)
  val offset = io.cacheIn.addr(offW - 1, 0)
  val index = io.cacheIn.addr(offW + SRAMaddrW - 1, offW)
  val tag = io.cacheIn.addr(addrW - 1, offW + SRAMaddrW)
  //基本状态机
  val cacheIdle :: cacheMiss :: Nil = Enum(2)
  val cacheState = RegInit(cacheIdle)

  val hit = Wire(Bool())
  val IdleMux = Mux(io.cacheIn.valid && !hit,cacheMiss,cacheIdle)
  val missMux = Mux(io.cacheOut.r_last_i,cacheIdle,cacheMiss)
  cacheState := MuxLookup(
    cacheState,
    cacheIdle,
    Array(
      cacheIdle -> IdleMux,
      cacheMiss -> missMux
    )
  )
  val isIdle = cacheState === cacheIdle
  val isMiss = cacheState === cacheMiss

  //组合逻辑确定hit，首先根据index找到tag和v
  val tagArrayWire =Wire (Vec(deepth,Vec(CacheSram,(UInt((addrW - offW - SRAMaddrW).W)))))
  val vArrayWire = Wire( Vec(deepth,Vec(CacheSram,(Bool()))))

  val tagMuxIn = tagArrayWire.zipWithIndex.map { case (ins, j) => {
    j.U -> ins
  }
  }.toArray
  val tagMuxOut = MuxLookup(
    index,
    tagArrayWire(0),
    tagMuxIn
  )

  val vMuxIn = vArrayWire.zipWithIndex.map { case (ins, j) => {
    j.U -> ins
  }
  }.toArray
  val vMuxOut = MuxLookup(
    index,
    vArrayWire(0),
    vMuxIn
  )

  val hitArray = (0 until CacheSram).map { i => vMuxOut(i) && tagMuxOut(i) === tag }
  hit := hitArray.foldLeft(false.B)((b, a) => b || a)






  //开始读取到相应的sram
  //  io.SRAMIO.zipWithIndex.map { case (ins, num) => {
  //    ins.cen :=  !(io.cacheIn.valid && hitArray(num) && isIdle)
  //    ins.addr := index
  //    ins.wen := io.cacheIn.wen && isIdle
  //  }
  //  }

  val waysel = Mux1H(
    hitArray,
    io.SRAMIO.map { ins => ins.rdata }
  )
  io.cacheIn.data_read := Mux(offset(offW - 1),waysel(SRAMdataW - 1,dataW),waysel(dataW - 1,0))
  //  val readOver = isRead || isBlock

  //读miss
  val chainW = log2Up(CacheSram)
  val selArrayWire = Wire(Vec(deepth, UInt(chainW.W)))
  val sramSel = MuxLookup(
    index,
    selArrayWire(0),
    selArrayWire.zipWithIndex.map{ case(ins , j ) => {j.U -> ins}}
  )
  io.cacheOut.ar_len_o :=  isMiss
  io.cacheOut.ar_addr_o :=  Cat(io.cacheIn.addr(addrW - 1,offW),0.U(offW.W) )
  io.cacheOut.ar_valid_o := isMiss

  for (i <- 0 until deepth) {
    selArrayWire(i) := RegEnable(
      selArrayWire(i) + 1.U,
      0.U,
      io.cacheOut.r_last_i &&  i.U === index
    )
    for(j <- 0 until CacheSram){
      tagArrayWire(i)(j) := RegEnable(tag,0.U,io.cacheOut.r_last_i && i.U === index && selArrayWire(i) === j.U && isMiss)
      withReset( reset.asBool() || updataICache) {
        vArrayWire(i)(j) := RegEnable(true.B, false.B, io.cacheOut.r_last_i && i.U === index && selArrayWire(i) === j.U && isMiss)
      }

    }
  }

  //写过程
  //写sram

  val maskWrite = Wire(Vec(dataW / 8, UInt(8.W)))
  (0 until dataW / 8).map { i => {
    maskWrite(i) := Mux(
      io.cacheIn.mask(i),
      Fill(8, false.B),
      Fill(8, true.B)
    )
  }
  }
  val ramMaskWrite = Mux(offset(offW - 1), Cat(maskWrite.asTypeOf(UInt(dataW.W)), Fill(64, true.B)), Cat(Fill(64, true.B), maskWrite.asTypeOf(UInt(dataW.W))))

  //写axi
  io.cacheOut.w_valid_o := false.B
  io.cacheOut.w_data_o := io.cacheIn.data_write
  io.cacheOut.w_addr_o := io.cacheIn.addr
  io.cacheOut.w_mask_o := io.cacheIn.mask


  //val writeOver = isWrite && io.cacheOut.w_ready_i || isBlock


  io.SRAMIO.zipWithIndex.map { case (ins, num) => {
    ins.cen := !((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel)
    ins.addr := index
    ins.wen := !((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel)
    ins.wdata := Mux(io.cacheOut.r_last_i, Cat(io.cacheOut.r_data_i, 0.U(dataW.W)), Cat(0.U(dataW.W), io.cacheOut.r_data_i))
    ins.wmask := Mux(io.cacheOut.r_last_i, Cat(Fill(dataW, 0.U(1.W)), Fill(dataW, 1.U(1.W))), Cat(Fill(dataW, 1.U(1.W)), Fill(dataW, 0.U(1.W))))
  }
  }

  io.cacheIn.ready := isIdle && hit

  io.cacheOut.wsize := io.cacheIn.rsize


}

class DcachenoIP() extends Module{
  val io = IO( new Bundle() {
    val cacheOut = new cacheIO(dataW,addrW)
    val cacheIn = new cpuRWIO(dataW,addrW)
    val SRAMIO = Vec(CacheSram,new SignalRAM(SRAMdataW,SRAMaddrW))
    val block = Input(Bool())
  })
  val updataDCache = Wire(Bool())
  updataDCache := false.B
  BoringUtils.addSink(updataDCache, "updataICache")
  val deepth = 64
  //数据位处理
  val offW = log2Up(SRAMdataW / 8)
  val offset = io.cacheIn.addr(offW - 1, 0)
  val index = io.cacheIn.addr(offW + SRAMaddrW - 1, offW)
  val tag = io.cacheIn.addr(addrW - 1, offW + SRAMaddrW)
  //基本状态机
  val cacheIdle :: cacheMiss  :: cacheWrite :: cacheBlock :: Nil = Enum(4)
  val cacheState = RegInit(cacheIdle)

  val hit = Wire(Bool())
  val IdleMux = Mux(
    io.cacheIn.valid && !io.block,
    Mux(
      io.cacheIn.wen,
      cacheWrite,
      Mux(
        hit,
        cacheIdle,
        cacheMiss
      )
    ),
    cacheIdle
  )
  val missMux = Mux(io.cacheOut.r_last_i,cacheIdle,cacheMiss)
  val writeMux = Mux(
    io.cacheOut.w_ready_i,
    Mux(
      io.block,
      cacheBlock,
      cacheIdle),
    cacheWrite
  )
  val blocKMux = Mux(io.block,cacheBlock,cacheIdle )
  cacheState := MuxLookup(
    cacheState,
    cacheIdle,
    Array(
      cacheIdle -> IdleMux,
      cacheMiss -> missMux,
      cacheWrite -> writeMux,
      cacheBlock -> blocKMux
    )
  )
  val isIdle = cacheState === cacheIdle
  val isMiss = cacheState === cacheMiss
  val isWrite = cacheState === cacheWrite
  val isBlock = cacheState === cacheBlock

  //组合逻辑确定hit，首先根据index找到tag和v
  val tagArrayWire =Wire (Vec(deepth,Vec(CacheSram,(UInt((addrW - offW - SRAMaddrW).W)))))
  val vArrayWire = Wire( Vec(deepth,Vec(CacheSram,(Bool()))))

  val tagMuxIn = tagArrayWire.zipWithIndex.map { case (ins, j) => {
    j.U -> ins
  }
  }.toArray
  val tagMuxOut = MuxLookup(
    index,
    tagArrayWire(0),
    tagMuxIn
  )

  val vMuxIn = vArrayWire.zipWithIndex.map { case (ins, j) => {
    j.U -> ins
  }
  }.toArray
  val vMuxOut = MuxLookup(
    index,
    vArrayWire(0),
    vMuxIn
  )

  val hitArray = (0 until CacheSram).map { i => vMuxOut(i) && tagMuxOut(i) === tag }
  hit := hitArray.foldLeft(false.B)((b, a) => b || a)






  //开始读取到相应的sram
  //  io.SRAMIO.zipWithIndex.map { case (ins, num) => {
  //    ins.cen :=  !(io.cacheIn.valid && hitArray(num) && isIdle)
  //    ins.addr := index
  //    ins.wen := io.cacheIn.wen && isIdle
  //  }
  //  }

  val waysel = Mux1H(
    hitArray,
    io.SRAMIO.map { ins => ins.rdata }
  )
  io.cacheIn.data_read := Mux(offset(offW - 1),waysel(SRAMdataW - 1,dataW),waysel(dataW - 1,0))
  //  val readOver = isRead || isBlock

  //读miss
  val chainW = log2Up(CacheSram)
  val selArrayWire = Wire(Vec(deepth, UInt(chainW.W)))
  val sramSel = MuxLookup(
    index,
    selArrayWire(0),
    selArrayWire.zipWithIndex.map{ case(ins , j ) => {j.U -> ins}}
  )
  io.cacheOut.ar_len_o :=  isMiss
  io.cacheOut.ar_addr_o :=  Cat(io.cacheIn.addr(addrW - 1,offW),0.U(offW.W) )
  io.cacheOut.ar_valid_o := isMiss


  //  io.SRAMIO.zipWithIndex.map { case (ins, num) => {
  //    ins.cen :=  !((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel)
  //    ins.addr := index
  //    ins.wen := !((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel)
  //    ins.wdata := Mux(isMiss, Mux(io.cacheOut.r_last_i, Cat(io.cacheOut.r_data_i, 0.U(dataW.W)), Cat(0.U(dataW.W), io.cacheOut.r_data_i)) ,0.U)
  //    ins.wmask :=Mux(isMiss, Mux(io.cacheOut.r_last_i, Cat(Fill(dataW, 0.U(1.W)), Fill(dataW, 1.U(1.W))), Cat(Fill(dataW, 1.U(1.W)), Fill(dataW, 0.U(1.W)))),0.U)
  //  }
  //  }

  for (i <- 0 until deepth) {
    selArrayWire(i) := RegEnable(
      selArrayWire(i) + 1.U,
      0.U,
      io.cacheOut.r_last_i &&  i.U === index
    )
    for(j <- 0 until CacheSram){
      tagArrayWire(i)(j) := RegEnable(tag,0.U,io.cacheOut.r_last_i && i.U === index && selArrayWire(i) === j.U && isMiss)
      withReset( reset.asBool() || updataDCache) {
        vArrayWire(i)(j) := RegEnable(true.B, false.B, io.cacheOut.r_last_i && i.U === index && selArrayWire(i) === j.U && isMiss)
      }

    }
  }

  //写过程
  //写sram

  val maskWrite = Wire(Vec(dataW / 8, UInt(8.W)))
  (0 until dataW / 8).map { i => {
    maskWrite(i) := Mux(
      io.cacheIn.mask(i),
      Fill(8, false.B),
      Fill(8, true.B)
    )
  }
  }
  val ramMaskWrite = Mux(offset(offW - 1), Cat(maskWrite.asTypeOf(UInt(dataW.W)), Fill(64, true.B)), Cat(Fill(64, true.B), maskWrite.asTypeOf(UInt(dataW.W))))

  //  io.SRAMIO.zipWithIndex.map { case (ins, num) => {
  //    ins.cen := !(io.cacheIn.valid && hitArray(num) && isIdle)
  //    ins.addr := index
  //    ins.wen := io.cacheIn.wen && isIdle
  //    ins.wdata := Mux(
  //      isIdle ,
  //      Mux(
  //        offset(offW - 1) ,
  //        Cat(io.cacheIn.data_write,0.U(dataW.W)),
  //        Cat(0.U(dataW.W),io.cacheIn.data_write)
  //      ) ,
  //      0.U)
  //    ins.wmask :=  Mux(isIdle , ramMaskWrite, 0.U)
  //  }
  //  }
  //写axi
  io.cacheOut.w_valid_o := ( io.cacheIn.valid&& !io.block && io.cacheIn.wen && isIdle)
  io.cacheOut.w_data_o := io.cacheIn.data_write
  io.cacheOut.w_addr_o := io.cacheIn.addr
  io.cacheOut.w_mask_o := io.cacheIn.mask


  //val writeOver = isWrite && io.cacheOut.w_ready_i || isBlock


  io.SRAMIO.zipWithIndex.map { case (ins, num) => {
    ins.cen := !((io.cacheIn.valid && hitArray(num) && isIdle) || ((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel))
    ins.addr := index
    ins.wen := !((io.cacheIn.wen && isIdle) || ((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel))
    ins.wdata := Mux(
      isMiss,
      Mux(io.cacheOut.r_last_i, Cat(io.cacheOut.r_data_i, 0.U(dataW.W)), Cat(0.U(dataW.W), io.cacheOut.r_data_i)),
      Mux(offset(offW - 1), Cat(io.cacheIn.data_write, 0.U(dataW.W)), Cat(0.U(dataW.W), io.cacheIn.data_write))
    )
    ins.wmask := Mux(
      isMiss,
      Mux(io.cacheOut.r_last_i, Cat(Fill(dataW, 0.U(1.W)), Fill(dataW, 1.U(1.W))), Cat(Fill(dataW, 1.U(1.W)), Fill(dataW, 0.U(1.W)))),
      ramMaskWrite
    )
  }
  }

  io.cacheIn.ready := isWrite && io.cacheOut.w_ready_i  || isBlock||(isIdle &&hit && !io.cacheIn.wen)

  io.cacheOut.wsize := io.cacheIn.rsize


}
class Icache() extends Module{
  val io = IO( new Bundle() {
    val cacheOut = new cacheIO(dataW,addrW)
    val cacheIn = new cpuRWIO(dataW,addrW)
    val SRAMIO = Vec(CacheSram,new SignalRAM(SRAMdataW,SRAMaddrW))
    val block = Input(Bool())
  })
if(memIP) {
  val updataICache = Wire(Bool())
  updataICache := false.B
  BoringUtils.addSink(updataICache, "updataICache")


  val deepth = 64
  //数据位处理
  val offW = log2Up(SRAMdataW / 8)
  val offset = io.cacheIn.addr(offW - 1, 0)
  val index = io.cacheIn.addr(offW + SRAMaddrW - 1, offW)
  val tag = io.cacheIn.addr(addrW - 1, offW + SRAMaddrW)
  //基本状态机
  val cacheIdle :: cacheMiss :: cacheRead :: cacheBlock :: Nil = Enum(4)
  val cacheState = RegInit(cacheIdle)

  val hit = Wire(Bool())
  val IdleMux = Mux(
    io.cacheIn.valid,
    Mux(
      hit,
      cacheRead,
      cacheMiss,
    ),
    cacheIdle
  )


  //    Mux(io.cacheIn.valid && !hit,cacheMiss,cacheIdle)
  val missMux = Mux(io.cacheOut.r_last_i, cacheIdle, cacheMiss)
  val readMux = Mux(io.block, cacheBlock, cacheIdle)
  val blocKMux = Mux(io.block, cacheBlock, cacheIdle)
  cacheState := MuxLookup(
    cacheState,
    cacheIdle,
    Array(
      cacheIdle -> IdleMux,
      cacheMiss -> missMux,
      cacheRead -> readMux,
      cacheBlock -> blocKMux
    )
  )
  val isIdle = cacheState === cacheIdle
  val isMiss = cacheState === cacheMiss
  val isRead = cacheState === cacheRead
  val isBlock = cacheState === cacheBlock

  //组合逻辑确定hit，首先根据index找到tag和v
  val tagArrayWire = Wire(Vec(deepth, Vec(CacheSram, (UInt((addrW - offW - SRAMaddrW).W)))))
  val vArrayWire = Wire(Vec(deepth, Vec(CacheSram, (Bool()))))

  val tagMuxIn = tagArrayWire.zipWithIndex.map { case (ins, j) => {
    j.U -> ins
  }
  }.toArray
  val tagMuxOut = MuxLookup(
    index,
    tagArrayWire(0),
    tagMuxIn
  )

  val vMuxIn = vArrayWire.zipWithIndex.map { case (ins, j) => {
    j.U -> ins
  }
  }.toArray
  val vMuxOut = MuxLookup(
    index,
    vArrayWire(0),
    vMuxIn
  )

  val hitArray = (0 until CacheSram).map { i => vMuxOut(i) && tagMuxOut(i) === tag }
  hit := hitArray.foldLeft(false.B)((b, a) => b || a)






  //开始读取到相应的sram
  //  io.SRAMIO.zipWithIndex.map { case (ins, num) => {
  //    ins.cen :=  !(io.cacheIn.valid && hitArray(num) && isIdle)
  //    ins.addr := index
  //    ins.wen := io.cacheIn.wen && isIdle
  //  }
  //  }

  val waysel = Mux1H(
    hitArray,
    io.SRAMIO.map { ins => ins.rdata }
  )
  io.cacheIn.data_read := Mux(offset(offW - 1), waysel(SRAMdataW - 1, dataW), waysel(dataW - 1, 0))
  //  val readOver = isRead || isBlock

  //读miss
  val chainW = log2Up(CacheSram)
  val selArrayWire = Wire(Vec(deepth, UInt(chainW.W)))
  val sramSel = MuxLookup(
    index,
    selArrayWire(0),
    selArrayWire.zipWithIndex.map { case (ins, j) => {
      j.U -> ins
    }
    }
  )
  io.cacheOut.ar_len_o := isMiss
  io.cacheOut.ar_addr_o := Cat(io.cacheIn.addr(addrW - 1, offW), 0.U(offW.W))
  io.cacheOut.ar_valid_o := isMiss

  for (i <- 0 until deepth) {
    selArrayWire(i) := RegEnable(
      selArrayWire(i) + 1.U,
      0.U,
      io.cacheOut.r_last_i && i.U === index
    )
    for (j <- 0 until CacheSram) {
      tagArrayWire(i)(j) := RegEnable(tag, 0.U, io.cacheOut.r_last_i && i.U === index && selArrayWire(i) === j.U && isMiss)
      withReset(reset.asBool() || updataICache) {
        vArrayWire(i)(j) := RegEnable(true.B, false.B, io.cacheOut.r_last_i && i.U === index && selArrayWire(i) === j.U && isMiss)
      }

    }
  }

  //写过程
  //写sram

  val maskWrite = Wire(Vec(dataW / 8, UInt(8.W)))
  (0 until dataW / 8).map { i => {
    maskWrite(i) := Mux(
      io.cacheIn.mask(i),
      Fill(8, false.B),
      Fill(8, true.B)
    )
  }
  }
  val ramMaskWrite = Mux(offset(offW - 1), Cat(maskWrite.asTypeOf(UInt(dataW.W)), Fill(64, true.B)), Cat(Fill(64, true.B), maskWrite.asTypeOf(UInt(dataW.W))))

  //写axi
  io.cacheOut.w_valid_o := false.B
  io.cacheOut.w_data_o := io.cacheIn.data_write
  io.cacheOut.w_addr_o := io.cacheIn.addr
  io.cacheOut.w_mask_o := io.cacheIn.mask


  //val writeOver = isWrite && io.cacheOut.w_ready_i || isBlock


  io.SRAMIO.zipWithIndex.map { case (ins, num) => {
    ins.cen := !(((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel) || (io.cacheIn.valid && hitArray(num) && isIdle))
    ins.addr := index
    ins.wen := !((io.cacheIn.wen && isIdle) || ((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel))
    ins.wdata := Mux(io.cacheOut.r_last_i, Cat(io.cacheOut.r_data_i, 0.U(dataW.W)), Cat(0.U(dataW.W), io.cacheOut.r_data_i))
    ins.wmask := Mux(io.cacheOut.r_last_i, Cat(Fill(dataW, 0.U(1.W)), Fill(dataW, 1.U(1.W))), Cat(Fill(dataW, 1.U(1.W)), Fill(dataW, 0.U(1.W))))
  }
  }

  io.cacheIn.ready := isRead || isBlock

  io.cacheOut.wsize := io.cacheIn.rsize

}else{

  val updataICache = Wire(Bool())
  updataICache := false.B
  BoringUtils.addSink(updataICache, "updataICache")


  val deepth = 64
  //数据位处理
  val offW = log2Up(SRAMdataW / 8)
  val offset = io.cacheIn.addr(offW - 1, 0)
  val index = io.cacheIn.addr(offW + SRAMaddrW - 1, offW)
  val tag = io.cacheIn.addr(addrW - 1, offW + SRAMaddrW)
  //基本状态机
  val cacheIdle :: cacheMiss :: Nil = Enum(2)
  val cacheState = RegInit(cacheIdle)

  val hit = Wire(Bool())
  val IdleMux = Mux(io.cacheIn.valid && !hit, cacheMiss, cacheIdle)
  val missMux = Mux(io.cacheOut.r_last_i, cacheIdle, cacheMiss)
  cacheState := MuxLookup(
    cacheState,
    cacheIdle,
    Array(
      cacheIdle -> IdleMux,
      cacheMiss -> missMux
    )
  )
  val isIdle = cacheState === cacheIdle
  val isMiss = cacheState === cacheMiss

  //组合逻辑确定hit，首先根据index找到tag和v
  val tagArrayWire = Wire(Vec(deepth, Vec(CacheSram, (UInt((addrW - offW - SRAMaddrW).W)))))
  val vArrayWire = Wire(Vec(deepth, Vec(CacheSram, (Bool()))))

  val tagMuxIn = tagArrayWire.zipWithIndex.map { case (ins, j) => {
    j.U -> ins
  }
  }.toArray
  val tagMuxOut = MuxLookup(
    index,
    tagArrayWire(0),
    tagMuxIn
  )

  val vMuxIn = vArrayWire.zipWithIndex.map { case (ins, j) => {
    j.U -> ins
  }
  }.toArray
  val vMuxOut = MuxLookup(
    index,
    vArrayWire(0),
    vMuxIn
  )

  val hitArray = (0 until CacheSram).map { i => vMuxOut(i) && tagMuxOut(i) === tag }
  hit := hitArray.foldLeft(false.B)((b, a) => b || a)






  //开始读取到相应的sram
  //  io.SRAMIO.zipWithIndex.map { case (ins, num) => {
  //    ins.cen :=  !(io.cacheIn.valid && hitArray(num) && isIdle)
  //    ins.addr := index
  //    ins.wen := io.cacheIn.wen && isIdle
  //  }
  //  }

  val waysel = Mux1H(
    hitArray,
    io.SRAMIO.map { ins => ins.rdata }
  )
  io.cacheIn.data_read := Mux(offset(offW - 1), waysel(SRAMdataW - 1, dataW), waysel(dataW - 1, 0))
  //  val readOver = isRead || isBlock

  //读miss
  val chainW = log2Up(CacheSram)
  val selArrayWire = Wire(Vec(deepth, UInt(chainW.W)))
  val sramSel = MuxLookup(
    index,
    selArrayWire(0),
    selArrayWire.zipWithIndex.map { case (ins, j) => {
      j.U -> ins
    }
    }
  )
  io.cacheOut.ar_len_o := isMiss
  io.cacheOut.ar_addr_o := Cat(io.cacheIn.addr(addrW - 1, offW), 0.U(offW.W))
  io.cacheOut.ar_valid_o := isMiss

  for (i <- 0 until deepth) {
    selArrayWire(i) := RegEnable(
      selArrayWire(i) + 1.U,
      0.U,
      io.cacheOut.r_last_i && i.U === index
    )
    for (j <- 0 until CacheSram) {
      tagArrayWire(i)(j) := RegEnable(tag, 0.U, io.cacheOut.r_last_i && i.U === index && selArrayWire(i) === j.U && isMiss)
      withReset(reset.asBool() || updataICache) {
        vArrayWire(i)(j) := RegEnable(true.B, false.B, io.cacheOut.r_last_i && i.U === index && selArrayWire(i) === j.U && isMiss)
      }

    }
  }

  //写过程
  //写sram

  val maskWrite = Wire(Vec(dataW / 8, UInt(8.W)))
  (0 until dataW / 8).map { i => {
    maskWrite(i) := Mux(
      io.cacheIn.mask(i),
      Fill(8, false.B),
      Fill(8, true.B)
    )
  }
  }
  val ramMaskWrite = Mux(offset(offW - 1), Cat(maskWrite.asTypeOf(UInt(dataW.W)), Fill(64, true.B)), Cat(Fill(64, true.B), maskWrite.asTypeOf(UInt(dataW.W))))

  //写axi
  io.cacheOut.w_valid_o := false.B
  io.cacheOut.w_data_o := io.cacheIn.data_write
  io.cacheOut.w_addr_o := io.cacheIn.addr
  io.cacheOut.w_mask_o := io.cacheIn.mask


  //val writeOver = isWrite && io.cacheOut.w_ready_i || isBlock


  io.SRAMIO.zipWithIndex.map { case (ins, num) => {
    ins.cen := !((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel)
    ins.addr := index
    ins.wen := !((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel)
    ins.wdata := Mux(io.cacheOut.r_last_i, Cat(io.cacheOut.r_data_i, 0.U(dataW.W)), Cat(0.U(dataW.W), io.cacheOut.r_data_i))
    ins.wmask := Mux(io.cacheOut.r_last_i, Cat(Fill(dataW, 0.U(1.W)), Fill(dataW, 1.U(1.W))), Cat(Fill(dataW, 1.U(1.W)), Fill(dataW, 0.U(1.W))))
  }
  }

  io.cacheIn.ready := isIdle && hit

  io.cacheOut.wsize := io.cacheIn.rsize


}
}

class Dcache() extends Module{
  val io = IO( new Bundle() {
    val cacheOut = new cacheIO(dataW,addrW)
    val cacheIn = new cpuRWIO(dataW,addrW)
    val SRAMIO = Vec(CacheSram,new SignalRAM(SRAMdataW,SRAMaddrW))
    val block = Input(Bool())
  })
  if(memIP) {
    val updataDCache = Wire(Bool())
    updataDCache := false.B
    BoringUtils.addSink(updataDCache, "updataICache")
    val deepth = 64
    //数据位处理
    val offW = log2Up(SRAMdataW / 8)
    val offset = io.cacheIn.addr(offW - 1, 0)
    val index = io.cacheIn.addr(offW + SRAMaddrW - 1, offW)
    val tag = io.cacheIn.addr(addrW - 1, offW + SRAMaddrW)
    //基本状态机
    val cacheIdle :: cacheMiss :: cacheRead :: cacheWrite :: cacheBlock :: Nil = Enum(5)
    val cacheState = RegInit(cacheIdle)

    val hit = Wire(Bool())
    val IdleMux = Mux(
      io.cacheIn.valid,
      Mux(
        io.cacheIn.wen,
        cacheWrite,
        Mux(
          hit,
          cacheRead,
          cacheMiss
        )
      ),
      cacheIdle
    )
    val missMux = Mux(io.cacheOut.r_last_i, cacheIdle, cacheMiss)
    val readMux = Mux(io.block, cacheRead, cacheIdle)
    val writeMux = Mux(
      io.cacheOut.w_ready_i,
      Mux(
        io.block,
        cacheBlock,
        cacheIdle),
      cacheWrite
    )
    val blocKMux = Mux(io.block, cacheBlock, cacheIdle)
    cacheState := MuxLookup(
      cacheState,
      cacheIdle,
      Array(
        cacheIdle -> IdleMux,
        cacheMiss -> missMux,
        cacheRead -> readMux,
        cacheWrite -> writeMux,
        cacheBlock -> blocKMux
      )
    )
    val isIdle = cacheState === cacheIdle
    val isMiss = cacheState === cacheMiss
    val isRead = cacheState === cacheRead
    val isWrite = cacheState === cacheWrite
    val isBlock = cacheState === cacheBlock

    //组合逻辑确定hit，首先根据index找到tag和v
    val tagArrayWire = Wire(Vec(deepth, Vec(CacheSram, (UInt((addrW - offW - SRAMaddrW).W)))))
    val vArrayWire = Wire(Vec(deepth, Vec(CacheSram, (Bool()))))

    val tagMuxIn = tagArrayWire.zipWithIndex.map { case (ins, j) => {
      j.U -> ins
    }
    }.toArray
    val tagMuxOut = MuxLookup(
      index,
      tagArrayWire(0),
      tagMuxIn
    )

    val vMuxIn = vArrayWire.zipWithIndex.map { case (ins, j) => {
      j.U -> ins
    }
    }.toArray
    val vMuxOut = MuxLookup(
      index,
      vArrayWire(0),
      vMuxIn
    )

    val hitArray = (0 until CacheSram).map { i => vMuxOut(i) && tagMuxOut(i) === tag }
    hit := hitArray.foldLeft(false.B)((b, a) => b || a)






    //开始读取到相应的sram
    //  io.SRAMIO.zipWithIndex.map { case (ins, num) => {
    //    ins.cen :=  !(io.cacheIn.valid && hitArray(num) && isIdle)
    //    ins.addr := index
    //    ins.wen := io.cacheIn.wen && isIdle
    //  }
    //  }

    val waysel = Mux1H(
      hitArray,
      io.SRAMIO.map { ins => ins.rdata }
    )
    io.cacheIn.data_read := Mux(offset(offW - 1), waysel(SRAMdataW - 1, dataW), waysel(dataW - 1, 0))
    //  val readOver = isRead || isBlock

    //读miss
    val chainW = log2Up(CacheSram)
    val selArrayWire = Wire(Vec(deepth, UInt(chainW.W)))
    val sramSel = MuxLookup(
      index,
      selArrayWire(0),
      selArrayWire.zipWithIndex.map { case (ins, j) => {
        j.U -> ins
      }
      }
    )
    io.cacheOut.ar_len_o := isMiss
    io.cacheOut.ar_addr_o := Cat(io.cacheIn.addr(addrW - 1, offW), 0.U(offW.W))
    io.cacheOut.ar_valid_o := isMiss


    //  io.SRAMIO.zipWithIndex.map { case (ins, num) => {
    //    ins.cen :=  !((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel)
    //    ins.addr := index
    //    ins.wen := !((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel)
    //    ins.wdata := Mux(isMiss, Mux(io.cacheOut.r_last_i, Cat(io.cacheOut.r_data_i, 0.U(dataW.W)), Cat(0.U(dataW.W), io.cacheOut.r_data_i)) ,0.U)
    //    ins.wmask :=Mux(isMiss, Mux(io.cacheOut.r_last_i, Cat(Fill(dataW, 0.U(1.W)), Fill(dataW, 1.U(1.W))), Cat(Fill(dataW, 1.U(1.W)), Fill(dataW, 0.U(1.W)))),0.U)
    //  }
    //  }

    for (i <- 0 until deepth) {
      selArrayWire(i) := RegEnable(
        selArrayWire(i) + 1.U,
        0.U,
        io.cacheOut.r_last_i && i.U === index
      )
      for (j <- 0 until CacheSram) {
        tagArrayWire(i)(j) := RegEnable(tag, 0.U, io.cacheOut.r_last_i && i.U === index && selArrayWire(i) === j.U && isMiss)
        withReset(reset.asBool() || updataDCache) {
          vArrayWire(i)(j) := RegEnable(true.B, false.B, io.cacheOut.r_last_i && i.U === index && selArrayWire(i) === j.U && isMiss)
        }

      }
    }

    //写过程
    //写sram

    val maskWrite = Wire(Vec(dataW / 8, UInt(8.W)))
    (0 until dataW / 8).map { i => {
      maskWrite(i) := Mux(
        io.cacheIn.mask(i),
        Fill(8, false.B),
        Fill(8, true.B)
      )
    }
    }
    val ramMaskWrite = Mux(offset(offW - 1), Cat(maskWrite.asTypeOf(UInt(dataW.W)), Fill(64, true.B)), Cat(Fill(64, true.B), maskWrite.asTypeOf(UInt(dataW.W))))

    //  io.SRAMIO.zipWithIndex.map { case (ins, num) => {
    //    ins.cen := !(io.cacheIn.valid && hitArray(num) && isIdle)
    //    ins.addr := index
    //    ins.wen := io.cacheIn.wen && isIdle
    //    ins.wdata := Mux(
    //      isIdle ,
    //      Mux(
    //        offset(offW - 1) ,
    //        Cat(io.cacheIn.data_write,0.U(dataW.W)),
    //        Cat(0.U(dataW.W),io.cacheIn.data_write)
    //      ) ,
    //      0.U)
    //    ins.wmask :=  Mux(isIdle , ramMaskWrite, 0.U)
    //  }
    //  }
    //写axi
    io.cacheOut.w_valid_o := (io.cacheIn.valid && io.cacheIn.wen && isIdle)
    io.cacheOut.w_data_o := io.cacheIn.data_write
    io.cacheOut.w_addr_o := io.cacheIn.addr
    io.cacheOut.w_mask_o := io.cacheIn.mask


    //val writeOver = isWrite && io.cacheOut.w_ready_i || isBlock


    io.SRAMIO.zipWithIndex.map { case (ins, num) => {
      ins.cen := !((io.cacheIn.valid && hitArray(num) && isIdle) || ((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel))
      ins.addr := index
      ins.wen := !((io.cacheIn.wen && isIdle) || ((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel))
      ins.wdata := Mux(
        isMiss,
        Mux(io.cacheOut.r_last_i, Cat(io.cacheOut.r_data_i, 0.U(dataW.W)), Cat(0.U(dataW.W), io.cacheOut.r_data_i)),
        Mux(offset(offW - 1), Cat(io.cacheIn.data_write, 0.U(dataW.W)), Cat(0.U(dataW.W), io.cacheIn.data_write))
      )
      ins.wmask := Mux(
        isMiss,
        Mux(io.cacheOut.r_last_i, Cat(Fill(dataW, 0.U(1.W)), Fill(dataW, 1.U(1.W))), Cat(Fill(dataW, 1.U(1.W)), Fill(dataW, 0.U(1.W)))),
        ramMaskWrite
      )
    }
    }

    io.cacheIn.ready := isWrite && io.cacheOut.w_ready_i || isBlock || isRead

    io.cacheOut.wsize := io.cacheIn.rsize

  }else{
    val updataDCache = Wire(Bool())
    updataDCache := false.B
    BoringUtils.addSink(updataDCache, "updataICache")
    val deepth = 64
    //数据位处理
    val offW = log2Up(SRAMdataW / 8)
    val offset = io.cacheIn.addr(offW - 1, 0)
    val index = io.cacheIn.addr(offW + SRAMaddrW - 1, offW)
    val tag = io.cacheIn.addr(addrW - 1, offW + SRAMaddrW)
    //基本状态机
    val cacheIdle :: cacheMiss :: cacheWrite :: cacheBlock :: Nil = Enum(4)
    val cacheState = RegInit(cacheIdle)

    val hit = Wire(Bool())
    val IdleMux = Mux(
      io.cacheIn.valid && !io.block,
      Mux(
        io.cacheIn.wen,
        cacheWrite,
        Mux(
          hit,
          cacheIdle,
          cacheMiss
        )
      ),
      cacheIdle
    )
    val missMux = Mux(io.cacheOut.r_last_i, cacheIdle, cacheMiss)
    val writeMux = Mux(
      io.cacheOut.w_ready_i,
      Mux(
        io.block,
        cacheBlock,
        cacheIdle),
      cacheWrite
    )
    val blocKMux = Mux(io.block, cacheBlock, cacheIdle)
    cacheState := MuxLookup(
      cacheState,
      cacheIdle,
      Array(
        cacheIdle -> IdleMux,
        cacheMiss -> missMux,
        cacheWrite -> writeMux,
        cacheBlock -> blocKMux
      )
    )
    val isIdle = cacheState === cacheIdle
    val isMiss = cacheState === cacheMiss
    val isWrite = cacheState === cacheWrite
    val isBlock = cacheState === cacheBlock

    //组合逻辑确定hit，首先根据index找到tag和v
    val tagArrayWire = Wire(Vec(deepth, Vec(CacheSram, (UInt((addrW - offW - SRAMaddrW).W)))))
    val vArrayWire = Wire(Vec(deepth, Vec(CacheSram, (Bool()))))

    val tagMuxIn = tagArrayWire.zipWithIndex.map { case (ins, j) => {
      j.U -> ins
    }
    }.toArray
    val tagMuxOut = MuxLookup(
      index,
      tagArrayWire(0),
      tagMuxIn
    )

    val vMuxIn = vArrayWire.zipWithIndex.map { case (ins, j) => {
      j.U -> ins
    }
    }.toArray
    val vMuxOut = MuxLookup(
      index,
      vArrayWire(0),
      vMuxIn
    )

    val hitArray = (0 until CacheSram).map { i => vMuxOut(i) && tagMuxOut(i) === tag }
    hit := hitArray.foldLeft(false.B)((b, a) => b || a)






    //开始读取到相应的sram
    //  io.SRAMIO.zipWithIndex.map { case (ins, num) => {
    //    ins.cen :=  !(io.cacheIn.valid && hitArray(num) && isIdle)
    //    ins.addr := index
    //    ins.wen := io.cacheIn.wen && isIdle
    //  }
    //  }

    val waysel = Mux1H(
      hitArray,
      io.SRAMIO.map { ins => ins.rdata }
    )
    io.cacheIn.data_read := Mux(offset(offW - 1), waysel(SRAMdataW - 1, dataW), waysel(dataW - 1, 0))
    //  val readOver = isRead || isBlock

    //读miss
    val chainW = log2Up(CacheSram)
    val selArrayWire = Wire(Vec(deepth, UInt(chainW.W)))
    val sramSel = MuxLookup(
      index,
      selArrayWire(0),
      selArrayWire.zipWithIndex.map { case (ins, j) => {
        j.U -> ins
      }
      }
    )
    io.cacheOut.ar_len_o := isMiss
    io.cacheOut.ar_addr_o := Cat(io.cacheIn.addr(addrW - 1, offW), 0.U(offW.W))
    io.cacheOut.ar_valid_o := isMiss


    //  io.SRAMIO.zipWithIndex.map { case (ins, num) => {
    //    ins.cen :=  !((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel)
    //    ins.addr := index
    //    ins.wen := !((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel)
    //    ins.wdata := Mux(isMiss, Mux(io.cacheOut.r_last_i, Cat(io.cacheOut.r_data_i, 0.U(dataW.W)), Cat(0.U(dataW.W), io.cacheOut.r_data_i)) ,0.U)
    //    ins.wmask :=Mux(isMiss, Mux(io.cacheOut.r_last_i, Cat(Fill(dataW, 0.U(1.W)), Fill(dataW, 1.U(1.W))), Cat(Fill(dataW, 1.U(1.W)), Fill(dataW, 0.U(1.W)))),0.U)
    //  }
    //  }

    for (i <- 0 until deepth) {
      selArrayWire(i) := RegEnable(
        selArrayWire(i) + 1.U,
        0.U,
        io.cacheOut.r_last_i && i.U === index
      )
      for (j <- 0 until CacheSram) {
        tagArrayWire(i)(j) := RegEnable(tag, 0.U, io.cacheOut.r_last_i && i.U === index && selArrayWire(i) === j.U && isMiss)
        withReset(reset.asBool() || updataDCache) {
          vArrayWire(i)(j) := RegEnable(true.B, false.B, io.cacheOut.r_last_i && i.U === index && selArrayWire(i) === j.U && isMiss)
        }

      }
    }

    //写过程
    //写sram

    val maskWrite = Wire(Vec(dataW / 8, UInt(8.W)))
    (0 until dataW / 8).map { i => {
      maskWrite(i) := Mux(
        io.cacheIn.mask(i),
        Fill(8, false.B),
        Fill(8, true.B)
      )
    }
    }
    val ramMaskWrite = Mux(offset(offW - 1), Cat(maskWrite.asTypeOf(UInt(dataW.W)), Fill(64, true.B)), Cat(Fill(64, true.B), maskWrite.asTypeOf(UInt(dataW.W))))

    //  io.SRAMIO.zipWithIndex.map { case (ins, num) => {
    //    ins.cen := !(io.cacheIn.valid && hitArray(num) && isIdle)
    //    ins.addr := index
    //    ins.wen := io.cacheIn.wen && isIdle
    //    ins.wdata := Mux(
    //      isIdle ,
    //      Mux(
    //        offset(offW - 1) ,
    //        Cat(io.cacheIn.data_write,0.U(dataW.W)),
    //        Cat(0.U(dataW.W),io.cacheIn.data_write)
    //      ) ,
    //      0.U)
    //    ins.wmask :=  Mux(isIdle , ramMaskWrite, 0.U)
    //  }
    //  }
    //写axi
    io.cacheOut.w_valid_o := (io.cacheIn.valid && !io.block && io.cacheIn.wen && isIdle)
    io.cacheOut.w_data_o := io.cacheIn.data_write
    io.cacheOut.w_addr_o := io.cacheIn.addr
    io.cacheOut.w_mask_o := io.cacheIn.mask


    //val writeOver = isWrite && io.cacheOut.w_ready_i || isBlock


    io.SRAMIO.zipWithIndex.map { case (ins, num) => {
      ins.cen := !((io.cacheIn.valid && hitArray(num) && isIdle) || ((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel))
      ins.addr := index
      ins.wen := !((io.cacheIn.wen && isIdle) || ((isMiss && io.cacheOut.r_valid_i) && num.U === sramSel))
      ins.wdata := Mux(
        isMiss,
        Mux(io.cacheOut.r_last_i, Cat(io.cacheOut.r_data_i, 0.U(dataW.W)), Cat(0.U(dataW.W), io.cacheOut.r_data_i)),
        Mux(offset(offW - 1), Cat(io.cacheIn.data_write, 0.U(dataW.W)), Cat(0.U(dataW.W), io.cacheIn.data_write))
      )
      ins.wmask := Mux(
        isMiss,
        Mux(io.cacheOut.r_last_i, Cat(Fill(dataW, 0.U(1.W)), Fill(dataW, 1.U(1.W))), Cat(Fill(dataW, 1.U(1.W)), Fill(dataW, 0.U(1.W)))),
        ramMaskWrite
      )
    }
    }

    io.cacheIn.ready := isWrite && io.cacheOut.w_ready_i || isBlock || (isIdle && hit && !io.cacheIn.wen)

    io.cacheOut.wsize := io.cacheIn.rsize

  }
}
object Cachegen extends App {
  chisel3.Driver.execute(args,() => new Dcache )
}
