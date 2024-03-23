package CGRA.ARCH.FullConnnect

import CGRA.ADL.ADL.{EleTrace, ModuleTrace}
import CGRAParam.PEParam
import CGRA.IR.Info2Xml.dumpIR
import CGRA.element.ALU.{OPC, OpInfo}
import OpInfo.fupar
import CGRA.module.TopModule.topGen
import CGRA.parameter.EleType._

class PE (peParam :PEParam ,name :String)extends ModuleTrace(name ){
  this.typeStr = "PE"
  this.deviceStr = peParam.deviceName
  this.width = peParam.width

  val inNum = peParam.opList.map(OpInfo.getOperandNum(_)).max
  val inPortList = (0 until inNum).map {
    i => {
      "in" + i.toString
    }
  }.toList
  this.inPorts = inPortList

  val outPortList = List("out0")
  this.outPorts = outPortList
  val aluIns = new EleTrace("ALU" , TYPE_ALU.id ,inPortList, outPortList, List(fupar(peParam.opList),peParam.width))
  addEle(aluIns)

//  val regIns = new EleTrace("Register", TYPE_Register.id,List("in0") , List("out0"), List(peParam.width))
//  addEle(regIns)


  inPortList.indices.map{
    i => addConnect(("this" , "in" + i.toString),("ALU" ,  "in" + i.toString))
  }


  addConnect(("ALU" , "out0"),("this" ,  "out0"))
//  addConnect(("Register" , "out0"),("this" ,  "out0"))




}

object PE{
  def apply (peParam :PEParam ,name :String): PE = {
    new PE (peParam  ,name )
  }
}



object PEFGen extends App {
//  val pe = new PE(PEParam(List(OPC.ADD) , 32,"PETest"),"PE")
  chisel3.Driver.execute(args,() => topGen(PE(PEParam(List(OPC.ADD,OPC.MUL) , 32,"PETest"),"PE").getModuleInfo(), "PETest.txt") )//生成verilog
  dumpIR(PE(PEParam(List(OPC.ADD,OPC.MUL) , 32,"PETest"),"PE").getModuleInfo(),"PE.xml","PE")
}