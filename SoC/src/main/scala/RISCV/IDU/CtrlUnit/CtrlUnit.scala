package RISCV.IDU.CtrlUnit

import ALUOpSel.ALU_Ctrl_W
import chisel3.util._
import chisel3._
import RISCV.common.Param.instW
import RISCV.common.instList.FENCEI

class ALUCtrlInterf extends  Bundle{
  val AluSrc1 = Output(UInt(2.W))
  val AluSrc2 = Output(UInt(2.W))
  val ALUCtrl = Output(UInt(ALU_Ctrl_W.W))

  val memWriteM = Output(Bool())
  val memWriteMask = Output(UInt(8.W))
  val memReadNum = Output(UInt(3.W))

  val dnpcSrc = Output(Bool())
  val jump = Output(Bool())
  val branch = Output(Bool())

  val regEn = Output(Bool())
  val ResultSrc = Output(UInt(2.W))
  val fencei = Output(Bool())
}
class CtrlUnit extends RawModule{
  val io = IO(new Bundle {
    val inst = Input(UInt(instW.W))
    val CtrlS = new ALUCtrlInterf
  })


  val ALUCtrl_ins =Module( new ALUCtrl)
  ALUCtrl_ins.io.inst := io.inst
  io.CtrlS.ALUCtrl := ALUCtrl_ins.io.ALUCtrl

  val ALUSrcGen_ins = Module(new ALUSrcGen)
  ALUSrcGen_ins.io.inst := io.inst
  io.CtrlS.AluSrc1 := ALUSrcGen_ins.io.AluSrc1
  io.CtrlS.AluSrc2 := ALUSrcGen_ins.io.AluSrc2

  val memWriteMGen_ins = Module(new memWriteMGen)
  memWriteMGen_ins.io.inst := io.inst
  io.CtrlS.memWriteM := memWriteMGen_ins.io.memWriteM

  val memWriteMaskGen_ins = Module(new memWriteMaskGen)
  memWriteMaskGen_ins.io.inst := io.inst
  io.CtrlS.memWriteMask := memWriteMaskGen_ins.io.mask

  val memReadNumGen_ins = Module(new memReadNumGen)
  memReadNumGen_ins.io.inst := io.inst
  io.CtrlS.memReadNum := memReadNumGen_ins.io.memReadNum

  val dnpcSrcGen_ins   = Module(new dnpcSrcGen)
  dnpcSrcGen_ins.io.inst := io.inst
  io.CtrlS.dnpcSrc := dnpcSrcGen_ins.io.dnpcSrc

  val jumpGen_ins = Module(new jumpGen)
  jumpGen_ins.io.inst := io.inst
  io.CtrlS.jump := jumpGen_ins.io.jump

  val branchGen_ins = Module(new branchGen)
  branchGen_ins.io.inst := io.inst
  io.CtrlS.branch := branchGen_ins.io.branch


  val regEnGen_ins = Module(new regEnGen)
  regEnGen_ins.io.inst := io.inst
  io.CtrlS.regEn := regEnGen_ins.io.regEn

  val ResultSrcGen_ins = Module(new ResultSrcGen)
  ResultSrcGen_ins.io.inst := io.inst
  io.CtrlS.ResultSrc := ResultSrcGen_ins.io.ResultSrc

  io.CtrlS.fencei := io.inst === FENCEI

}
