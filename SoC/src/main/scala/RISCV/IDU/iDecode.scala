package RISCV.IDU

import RISCV.IDU.CtrlUnit.regEnGen
import chisel3._
import RISCV.common.Param._
import RISCV.common.Interface.de2ex
import RISCV.common.instDe._
import RISCV.common.instList._

class iDecode extends Module{
    val io = IO (new Bundle {
//      val abor = Output(Bool())
      val pc = Input(UInt(addrW.W))
      val inst = Input(UInt(instW.W))
      val regEn = Input(Bool())
      val dataEx = new de2ex

      //pip
      val rdOut = Output(UInt(5.W))
      val rdIn = Input(UInt(5.W))
      val rs1  = Output(UInt(5.W))
      val rs2 = Output(UInt(5.W))

      val rsWB = Input(UInt(5.W))
      val dOutWB = Output(UInt(dataW.W))
    })

  val imme =Module( new immeGen)
  imme.io.inst := io.inst
  io.dataEx.imme := imme.io.imme

//  val regCtrl = Module(new regEnGen)
//  regCtrl.io.inst :=  io.inst
  val rd = getRd(io.inst)
  val rs1 = getRs1(io.inst)
  val rs2 = getRs2(io.inst)
  io.rdOut := rd
  io.rs1 := rs1
  io.rs2 := rs2


  val rf = Module(new RF)
  rf.io.rs1 := rs1
  rf.io.rs2 := rs2
  rf.io.rd := io.rdIn
  rf.io.we := io.regEn
  rf.io.din := io.dataEx.dIn
  io.dataEx.dOut1 := rf.io.dout1
  io.dataEx.dOut2 := rf.io.dout2
  rf.io.rdID := rd //dma
  io.dataEx.rdDout := rf.io.rdDout

  rf.io.pc := io.pc

  rf.io.rsWB := io.rsWB
  io.dOutWB := rf.io.doutWB

//  io.abor := io.inst === EBREAK
}
