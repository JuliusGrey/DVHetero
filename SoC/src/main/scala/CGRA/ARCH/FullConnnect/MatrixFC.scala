package CGRA.ARCH.FullConnnect

import CGRA.ADL.ADL.{EleTrace, ModuleTrace}
import CGRA.ARCH.FullConnnect.CGRAParam.MatrixFCParam
import CGRA.IR.Info2Xml.dumpIR
import CGRA.module.DumpInfo.infooutput
import CGRA.module.TopModule.topGen
import CGRA.parameter.EleType._

class MatrixFC(mFCParam:MatrixFCParam,name :String) extends ModuleTrace(name ) {
    this.typeStr = "MatrixFC"
    this.deviceStr = mFCParam.deviceName
    this.width = mFCParam.width
  //输入首先是直连，然后是上层，最后是下层
    this.inPorts =(0 until mFCParam.dirIn).map{i => "dirIn"+i.toString}.toList ++
      (0 until mFCParam.lastIn).map{i => "lastIn"+i.toString}.toList ++
      (0 until mFCParam.nextIn).map{i => "nextIn"+i.toString}.toList


  //输出首先是直连，然后是下层有寄存器，最后是下层无寄存器
   this.outPorts = (0 until mFCParam.dirOut).map{i => "dirOut"+i.toString}.toList ++
     (0 until mFCParam.nextOutWithoutConst).map{i => "nextOutWithoutConst"+i.toString}.toList ++
     (0 until mFCParam.nextOutWithConst).map{i => "nextOutWithConst"+i.toString}.toList

  (0 until mFCParam.dirOut).map{ i=>{
    val muxInNames =( 0 until inPorts.size).map{j => "in"+ j.toString}.toList
    val mux = new EleTrace("mux" + "dirOut"+i.toString,if(mFCParam.hasReg)TYPE_MultiplexerR.id else TYPE_Multiplexer.id, muxInNames, List("out0"), List(muxInNames.size, width))
    addEle(mux)
    ( 0 until inPorts.size).map{ j =>
      addConnect(("this", inPorts(j)), ("mux" + "dirOut"+i.toString, "in"+j.toString))
    }
    addConnect(("mux" + "dirOut"+i.toString, "out0"), ("this", "dirOut"+i.toString))
  }}

  (0 until mFCParam.nextOutWithoutConst).map { i => {
    val muxInNames = (0 until inPorts.size).map { j => "in" + j.toString }.toList
    val mux = new EleTrace("mux" + "nextOutWithoutConst" + i.toString, TYPE_Multiplexer.id, muxInNames, List("out0"), List(muxInNames.size, width))
    addEle(mux)
    (0 until inPorts.size).map { j => {
      addConnect(("this", inPorts(j)), ("mux" + "nextOutWithoutConst" + i.toString, "in" + j.toString))
    }
    }
    addConnect(("mux" + "nextOutWithoutConst" + i.toString, "out0"), ("this", "nextOutWithoutConst" + i.toString))
  }
  }
  (0 until mFCParam.nextOutWithConst).map { i => {
    val muxInNames = (0 to inPorts.size).map { j => "in" + j.toString }.toList
    val mux = new EleTrace("mux" + "nextOutWithConst" + i.toString, TYPE_Multiplexer.id, muxInNames, List("out0"), List(muxInNames.size, width))
    addEle(mux)
    val constIns = new EleTrace("Const" + i.toString, TYPE_ConstUnit.id, List(), List("out0"), List(width))
    addEle(constIns)
    (0 until inPorts.size).map { j => {
      addConnect(("this", inPorts(j)), ("mux" + "nextOutWithConst" + i.toString, "in" + j.toString))
    }
    }
    addConnect(("Const" + i.toString, "out0"), ("mux" + "nextOutWithConst" + i.toString, "in" + (muxInNames.size - 1).toString))
    addConnect(("mux" + "nextOutWithConst" + i.toString, "out0"), ("this", "nextOutWithConst" + i.toString))
  }
  }

}

object MatrixFC{
  def apply(mFCParam:MatrixFCParam,name :String) : MatrixFC={
  new MatrixFC(mFCParam,name)
  }
}

object MatrixGen extends App{
  val mFCParam = MatrixFCParam(2,2,2,2,2,2,true,32,"matrixtest")
  val matrixModule = MatrixFC(mFCParam,"matrix").getModuleInfo()
  infooutput("matrix.scala", matrixModule)
  chisel3.Driver.execute(args, () => topGen(matrixModule, "matrixTest.txt")) //生成verilog
  dumpIR(matrixModule, "matrixModule.xml", "matrixModule")
}