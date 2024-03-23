package RISCV.IFU

import chisel3._
import chisel3.util.RegEnable
import chisel3.util.experimental.BoringUtils
import RISCV.common.Param._

class iFetch extends Module{
  val io = IO(new Bundle {
    val instIn = Input(UInt(instW.W))
    val instOut = Output(UInt(instW.W))
    val pc =  Output(UInt(addrW.W))

    val snpc = Output(UInt(addrW.W))
    val dnpc = Input(UInt(addrW.W))

    //ctrl信号
    val jump = Input(Bool())  })

  val hazardPCBlock = Wire(Bool())
  hazardPCBlock := false.B
  BoringUtils.addSink(hazardPCBlock , "hazardPCBlock")
  val block1 = Wire(Bool())
  block1 := false.B
  BoringUtils.addSink(block1, "block1")
  val block23 = Wire(Bool())
  block23 := false.B
  BoringUtils.addSink(block23, "block23")
  val blockDMA = Wire(Bool())
  blockDMA := false.B
  BoringUtils.addSink(blockDMA, "blockDMA")

  //  val pc = RegInit(baseAddr.U(addrW.W))
  val intrTimeCnt = Wire(Bool())
  intrTimeCnt := false.B
  BoringUtils.addSink(intrTimeCnt, "intrTimeCnt")

  val snpc = Wire(UInt(addrW.W))
  val pc = RegEnable(Mux(io.jump,io.dnpc,snpc),baseAddr.U(addrW.W), (!(block1|| block23||blockDMA)) && (!hazardPCBlock || intrTimeCnt) )
//  printf("if.pc is %x\n",pc)
  snpc := pc + 4.U
  io.snpc := snpc
  //  pc := Mux(io.jump,io.dnpc,snpc)

  //  val pc = RegEnable(Mux(io.jump,io.dnpc,snpc),baseAddr.U(addrW.W), !pcBlock )
//  printf("pc is %x\n",pc)


  io.pc := pc
  //  pc := pc + 4.U
  //  io.addr := pc

  //  val insReg = RegInit(0.U(instW.W))
  //  insReg := io.instIn
  //  io.instOut := insReg
  io.instOut := io.instIn





}

