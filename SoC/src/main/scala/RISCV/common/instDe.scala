package RISCV.common

import chisel3.UInt

object instDe{


  //comman
  def getOp(instruction:UInt) : UInt = {
    instruction(6,0)
  }
  def getF3(instruction:UInt) : UInt = {
    instruction(14,12)
  }



  //Rtype
  def getRd(instruction:UInt) : UInt = {
    instruction(11,7)
  }
  def getRs1(instruction:UInt) : UInt = {
    instruction(19,15)
  }
  def getRs2(instruction:UInt) : UInt = {
    instruction(24,20)
  }
  //  def getRtypef7(instruction:UInt) : UInt = {
  //    instruction(31,25)
  //  }

}
