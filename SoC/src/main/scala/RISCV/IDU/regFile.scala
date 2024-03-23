package RISCV.IDU

import Chisel.RegEnable
import chisel3._
import chisel3.util.MuxLookup
import chisel3.util.experimental.BoringUtils
import RISCV.common.Param._

import scala.collection.mutable.ListBuffer

class RF extends Module {
  val io = IO(new Bundle {
    val pc = Input(UInt(addrW.W))


    val we = Input(Bool())
    val rs1 = Input(UInt(5.W))
    val rs2 = Input(UInt(5.W))
    val rd = Input(UInt(5.W))
    val rdID = Input(UInt(5.W))//id阶段的rd，用于dma
    val dout1 = Output(UInt(dataW.W))
    val dout2 = Output(UInt(dataW.W))
    val rdDout  = Output(UInt(dataW.W))
    val din = Input(UInt(dataW.W))

    val rsWB = Input(UInt(5.W))
    val doutWB = Output(UInt(dataW.W))
  })
  val dataRegBuf :ListBuffer[Tuple2[UInt , UInt ]] = new ListBuffer()
  val DPIC_RegRead_ins =  Module(new (DPIC_RegRead))
    DPIC_RegRead_ins.asInstanceOf[DPIC_RegRead].io.pc := io.pc


  val block1 = Wire(Bool())
  block1 := false.B
  BoringUtils.addSink(block1, "block1")
  val block23 = Wire(Bool())
  block23 := false.B
  BoringUtils.addSink(block23, "block23")
  val blockDMA = Wire(Bool())
  blockDMA := false.B
  BoringUtils.addSink(blockDMA, "blockDMA")


//  val prf = Wire(Bool())
//  prf :=false.B
//  BoringUtils.addSink(prf, "prf")


  for(i <- 0 until 32){
    val regData = Wire(UInt(dataW.W))
    val en = if(i==0){
      false.B
    }else{
      io.we && io.rd === i.U
    }
    regData := RegEnable(io.din , en && !(block1 || block23 || blockDMA))
//    if(HASDPIC) {
      DPIC_RegRead_ins.io.inst(i) := regData
//    }
    dataRegBuf.append( i.U ->regData)
  }

  io.dout1 := MuxLookup(io.rs1,0.U,dataRegBuf)
  io.dout2 := MuxLookup(io.rs2,0.U,dataRegBuf)
  io.rdDout := MuxLookup(io.rdID,0.U,dataRegBuf)

  io.doutWB := RegEnable(io.din,0.U,io.we && !(block1 || block23 ||blockDMA))
//    MuxLookup(io.rsWB,0.U,dataRegBuf)



}

object rfgen extends App {
  chisel3.Driver.execute(args,() => new RF )
}
