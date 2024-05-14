package TOP.PAD

import RISCV.TopOK
import RISCV.common.Interface.AXIIO
import chisel3._
import chisel3.util.Cat
class Top extends Module {
  val io = IO(new Bundle() {
    //    val imaster = new AXIIO
    val dmaster = new AXIIO(64,32,4)

    //val instIO = Flipped(new cpuRWIO(dataW, addrW))
    //    val dataIO = Flipped(new cpuRWIO(dataW, addrW))
  })
  val topIns = Module(new TopOK)
  topIns.io.mmio <> DontCare

  val clk = Wire(Bool())
  val rst = Wire(Bool())

  val padClk = Module(new PIS)
  padClk.io.IE := 1.U
  padClk.io.PAD := clock.asBool()
  clk := padClk.io.C


  val padRst = Module(new PIS)
  padRst.io.IE := 1.U
  padRst.io.PAD := reset.asBool()
  rst := padRst.io.C


  withClockAndReset(clk.asClock(),rst){
    topIns
  }

  val inList = List(
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
    io.dmaster.rdata
  )
  val subModInList =List(
    topIns.io.dmaster.awready,
    topIns.io.dmaster.wready,
    topIns.io.dmaster.bvalid,
    topIns.io.dmaster.arready,
    topIns.io.dmaster.rvalid,
    topIns.io.dmaster.rlast,
    topIns.io.dmaster.bresp,
    topIns.io.dmaster.rresp,
    topIns.io.dmaster.bid,
    topIns.io.dmaster.rid,
    topIns.io.dmaster.rdata)
  for( i <- 0 until  inList.size){
    if(inList(i).getWidth >1 ) {
      val padOut =  ( 0 until inList(i).getWidth).map {
        j => {
          val padI = Module(new PI)
          padI.io.IE := 1.U
          padI.io.PAD := inList(i)(j)
          padI.io.C
        }
      }
      subModInList(i) := Cat(padOut)
    }else{
      val padI = Module(new PI)
      padI.io.IE := 1.U
      padI.io.PAD := inList(i)
      subModInList(i) := padI.io.C
    }

  }


  val outList = List(
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


  val subModoutList = List(
    topIns.io.dmaster.awvalid,
    topIns.io.dmaster.wvalid,
    topIns.io.dmaster.wlast,
    topIns.io.dmaster.bready,
    topIns.io.dmaster.arvalid,
    topIns.io.dmaster.rready,
    topIns.io.dmaster.awburst,
    topIns.io.dmaster.awsize,
    topIns.io.dmaster.awaddr,
    topIns.io.dmaster.araddr,
    topIns.io.dmaster.awid,
    topIns.io.dmaster.arid,
    topIns.io.dmaster.wdata,
    topIns.io.dmaster.awlen,
    topIns.io.dmaster.wstrb,
    topIns.io.dmaster.arlen,
    topIns.io.dmaster.arsize,
    topIns.io.dmaster.arburst,

  )
  for (i <- 0 until outList.size) {
//    if (outList(i).getWidth > 1) {

     val padIn =  ( 0 until outList(i).getWidth).map{
        j => {
          val padO = Module(new PO12)
          padO.io.I := subModoutList(i)(j)
          padO.io.PAD
        }
      }
      outList(i) := Cat(padIn)
//    } else {
//      val padO = Module(new PO12)
//      padO.io.I := subModoutList(i)
//      outList(i) := padO.io.PAD
//    }

  }
}
object TopGen extends App {

  chisel3.Driver.execute(args,() => new Top )


}