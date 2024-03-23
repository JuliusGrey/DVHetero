package RISCV.TOPLEVEL

import RISCV.IDU.CtrlUnit.CsrCode.{mie, mstatus}
import chisel3._
import chisel3.util._
import RISCV.common.Param.{addrW, dataW, instW}
import RISCV.common.instList.{FENCEI, csrList, map_type}


class missReport extends Module{
 val io = IO(new Bundle() {
   val inst = Input(UInt(instW.W))
   val pc = Input(UInt(addrW.W))
 })
  val v1 = map_type.keys.foldLeft(false.B)((a,b) => a || (b === io.inst))
  val v2 =  csrList.foldLeft(false.B)((a,b) => a || (b === io.inst))
  val v3 = io.inst === 0.U || io.inst ===FENCEI
  val valid = v1 || v2||v3
  when(!valid) {
    printf("the miss inst is %x\n",io.inst)
    printf("pc is %x\n",io.pc)
  }


//  var strout = "ojbk"
//  (0 to 15).map{ins => strout = strout + strout}
//  val test1 = Wire(Bool())
//  test1 := v2 && (io.inst(31,20).asUInt() === mstatus)
}
