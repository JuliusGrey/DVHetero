package RISCV.BUS.mem

import chisel3._
import chisel3.util._
import RISCV.common.Interface.SignalRAM
import RISCV.common.Param.{SRAMaddrW, SRAMdataW}
import TOP.paramG.memIP

import scala.collection.mutable.ListBuffer
class mem extends Module {
  val io = IO(new Bundle {
    val memIO = Flipped(new SignalRAM(SRAMdataW , SRAMaddrW))
  })
if(memIP) {
  val cen = (~io.memIO.cen).asBool()
  val bwen = (~io.memIO.wmask).asUInt()
  val wen = (~io.memIO.wen).asBool()
  val ramWire = Wire(Vec(64, UInt(128.W)))
  //  val outreg = Reg(UInt(128.W))
  //  ram.indices.map{i => {
  //
  //  }}
  val dataRegBuf: ListBuffer[Tuple2[UInt, UInt]] = new ListBuffer()
  (0 until 64).map {
    i => {
      ramWire(i) := RegEnable((io.memIO.wdata & bwen) | (ramWire(i) & io.memIO.wmask), cen && wen && io.memIO.addr === i.U)
      dataRegBuf.append(i.U -> ramWire(i))
    }
  }

  io.memIO.rdata := RegEnable(
    MuxLookup(
      io.memIO.addr,
      0.U,
      dataRegBuf
    ),
    cen && io.memIO.wen
  )
}else {

  val cen = (~io.memIO.cen).asBool()
  val bwen = (~io.memIO.wmask).asUInt()
  val wen = (~io.memIO.wen).asBool()
  val ramWire = Wire(Vec(64, UInt(128.W)))
  //  val outreg = Reg(UInt(128.W))
  //  ram.indices.map{i => {
  //
  //  }}
  val dataRegBuf: ListBuffer[Tuple2[UInt, UInt]] = new ListBuffer()
  (0 until 64).map {
    i => {
      ramWire(i) := RegEnable((io.memIO.wdata & bwen) | (ramWire(i) & io.memIO.wmask), cen && wen && io.memIO.addr === i.U)
      dataRegBuf.append(i.U -> ramWire(i))
    }
  }

  io.memIO.rdata :=
    MuxLookup(
      io.memIO.addr,
      0.U,
      dataRegBuf
    )

}


}

//class mem extends BlackBox {
//  val io = IO(new Bundle {
//    val memIO = Flipped(new SignalRAM(SRAMdataW , SRAMaddrW))
//  })
//
////  val cen = (~io.memIO.cen).asBool()
////  val bwen = (~io.memIO.wmask).asUInt()
////  val wen = (~io.memIO.wen).asBool()
////  val ramWire = Wire(Vec(64,UInt(128.W)))
////  //  val outreg = Reg(UInt(128.W))
////  //  ram.indices.map{i => {
////  //
////  //  }}
////  val dataRegBuf: ListBuffer[Tuple2[UInt, UInt]] = new ListBuffer()
////  (0 until 64).map {
////    i => {
////      ramWire(i) := RegEnable((io.memIO.wdata & bwen) | (ramWire(i) & io.memIO.wmask),cen && wen && io.memIO.addr === i.U)
////      dataRegBuf.append(i.U ->ramWire(i))
////    }
////  }
////
////  io.memIO.rdata :=
////    MuxLookup(
////      io.memIO.addr,
////      0.U,
////      dataRegBuf
////    )
//
//
//
//}
