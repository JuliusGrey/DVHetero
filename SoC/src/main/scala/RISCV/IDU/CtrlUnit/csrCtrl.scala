package RISCV.IDU.CtrlUnit

import CsrCode._
import chisel3.util._
import chisel3._
import RISCV.common.Interface.CSRCtrlInterface
import RISCV.common.Param._
import RISCV.common.instList._
//中断的控制单元

//各个寄存器对应的编号
object CsrCode{
  val mstatus = "h300".U(12.W)
  val mtvec = "h305".U(12.W)
  val mepc = "h341".U(12.W)
  val mcause = "h342".U(12.W)
  val mie = "h304".U(12.W)
  val mip = "h344".U(12.W)
  val mscratch = "h340".U(12.W)
  val mhartid = "hf14".U(12.W)
}
////寄存器的选择信号
//object CsrRFSel{
//  val priRFW = 2
//  val mstatusSel = 0.U(priRFW.W)
//  val mtvecSel = 1.U(priRFW.W)
//  val mepcSel = 2.U(priRFW.W)
//  val mcauseSel = 3.U(priRFW.W)
//}


class csrCtrl extends Module{
  val io = IO(new Bundle {
    val inst = Input(UInt(instW.W))
    val CSRCtrlIf = new  CSRCtrlInterface
  })




  io.CSRCtrlIf.ecall :=  io.inst === ECALL


  io.CSRCtrlIf.csrrwen := io.inst ===CSRRW
  io.CSRCtrlIf.csrswen := io.inst ===CSRRS
  io.CSRCtrlIf.csrrsien := io.inst ===CSRRSI
  io.CSRCtrlIf.csrrcien := io.inst ===CSRRCI
  io.CSRCtrlIf.csrrcen := io.inst ===CSRRC
  io.CSRCtrlIf.csrrwien := io.inst ===CSRRWI

  io.CSRCtrlIf.rfen := io.inst ===CSRRW || io.inst ===CSRRS || io.inst ===CSRRSI|| io.inst ===CSRRCI|| io.inst ===CSRRC

  io.CSRCtrlIf.mepc2pc := io.inst === MRET

}
