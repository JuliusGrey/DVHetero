package RISCV.PIP

import Chisel._
import RISCV.IDU.CtrlUnit.ALUOpSel.ALU_Ctrl_W
import RISCV.common.Param._
object PipReg {
  //先数据流，后控制信号
  class IF2ID extends Bundle {
    val cmd = Bool()
    val inst = UInt(instW.W)
    val pc = UInt(addrW.W)
    val snpc = UInt(addrW.W)
    val ifjump = Bool()
    val ifdnpc =UInt(addrW.W)
  }

  class ID2EX extends  Bundle{
    val cmd = Bool()
    val inst = UInt(instW.W)
    val imme = UInt(dataW.W)
    val dOut1 = UInt(dataW.W)
    val dOut2 = UInt(dataW.W)
    val rdDout = UInt(dataW.W)//dma
    val pc = UInt(addrW.W)
    val snpc =  UInt(addrW.W)
    val rd = UInt(5.W)
    val ALUCtrl = UInt(ALU_Ctrl_W.W)
    val ALUSrc1 = UInt(2.W)
    val ALUSrc2 = UInt(2.W)
    val memReadNum = UInt(3.W)
    val valid = Bool()
    val mask = UInt(8.W)
    val memWriteM = Bool()
    val dnpcSrc = Bool()
    val jump = Bool()
    val branch = Bool()
    val regEn = Bool()
    val resultSrc = UInt(2.W)
    val rs1 = UInt(5.W)
    val rs2 = UInt(5.W)
    val fencei = Bool()
    val ifjump = Bool()
    val ifdnpc =UInt(addrW.W)
    val dma = Bool()
    val dmaWR = Bool()
  }

  class EX2MEM extends Bundle{
    val cmd = Bool()
    val inst = UInt(instW.W)
    val ALURes = UInt(dataW.W)
    val writeDataM =  UInt(dataW.W)
    val snpc =  UInt(addrW.W)
    val rd = UInt(5.W)
    val memReadNum = UInt(3.W)
    val valid = Bool()
    val mask = UInt(8.W)
    val memWriteM = Bool()
    val regEn = Bool()
    val ResultSrc = UInt(2.W)
    val pc = UInt(addrW.W)
    val fencei = Bool()
    val intr = Bool()
    val skip = Bool()
    val dnpc = UInt(addrW.W)
    val dnpcTakenWithoutPreB = Bool()

  }

  class MEM2WB extends Bundle{
    val cmd = Bool()
    val inst = UInt(instW.W)
    val ALURes = UInt(dataW.W)
    val rd = UInt(5.W)
    val snpc =  UInt(addrW.W)
    val memReadNum = UInt(3.W)
    val regEn = Bool()
    val ResultSrc = UInt(2.W)
    val pc = UInt(addrW.W)
    val intr = Bool()
    val skip = Bool()

  }

  class WB2END extends Bundle {
    val rd = UInt(5.W)
    val regEn = Bool()
    val inst = UInt(instW.W)
    val pc = UInt(addrW.W)

  }

  class CSRID2EX extends Bundle{
    val csrrwen = Bool()
    val csrswen =Bool()
    val csrrsien =Bool()
    val csrrcien =Bool()
    val csrrcen =Bool()
    val csrrwien =Bool()
    val ecall = Bool()
    val rfen = Bool()
    val mepc2pc = Bool()
  }

}
