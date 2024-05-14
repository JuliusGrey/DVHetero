package CGRA.ARCH.FullConnnect

import CGRA.ADL.ADL.{EleTrace, ModuleTrace}
import CGRA.ARCH.FullConnnect.CGRAParam.{CGRAFCParam, MatrixFCParam, PEParam}
import CGRA.IR.Info2Xml.dumpIR
import CGRA.element.ALU.{OPC, OpInfo}
import CGRA.module.DumpInfo.infooutput
import CGRA.module.TopModule.topGen
import CGRA.parameter.DirParam._
import CGRA.parameter.EleType._

import scala.collection.mutable.ListBuffer

class CGRAFCTop(cgraFCParam :CGRAFCParam ,pesOpList  : List[List[List[OPC.OPC]]]) extends ModuleTrace("CGRATop"){

  this.typeStr = "CGRA"
  this.deviceStr = "CGRAJ"
  this.width = cgraFCParam.width

  val inPortsList = (0 until cgraFCParam.inNum).map("in" + _.toString).toList
  this.inPorts = inPortsList

  val outPortsList = (0 until cgraFCParam.outNum).map("out" + _.toString).toList
  this.outPorts = outPortsList
  //用来记录每一行，每一个pe的输入数量
  val pesInNums:ListBuffer[ListBuffer[Int]] = new ListBuffer[ListBuffer[Int]]()
  val OpNameMap: scala.collection.mutable.Map[Set[OPC.OPC], String] =  scala.collection.mutable.Map()
  for(i <- 0 until cgraFCParam.PENums.size){
    val pesInNumsLine = new ListBuffer[Int]()
    for(j <- 0 until cgraFCParam.PENums(i)){
      val opList  = pesOpList(i)(j)
      if(!OpNameMap.contains(opList.toSet)) OpNameMap += (opList.toSet -> ("PE" + OpNameMap.size.toString))
      val peName =   OpNameMap(opList.toSet)
      val peParam = PEParam(pesOpList(i)(j) , width , peName )
       addModule(PE(peParam , "PE"+i.toString+"_"+j.toString ))
      pesInNumsLine.append(pesOpList(i)(j).map(OpInfo.getOperandNum(_)).max)
    }
    pesInNums.append(pesInNumsLine)
  }


  //第一行和最后一行的matrix要连接IO，所以要单独拿出来,这里是出了第一行和最后一行之外的其他的
  val matrixNameMap = new ListBuffer[MatrixFCParam]()
//  :scala.collection.mutable.Map[MatrixFCParam, String] =  scala.collection.mutable.Map()
  for(i <- 0 until cgraFCParam.PENums.size -1){
    val lastInNum = cgraFCParam.PENums(i)
    val nextInNum = cgraFCParam.PENums(i+1)
    val dirInnum = cgraFCParam.dirNum
    val nextOutWithoutConstNum = pesInNums(i+1).sum -  cgraFCParam.PENums(i+1)
    val nextOutWithConstNum = cgraFCParam.PENums(i+1)
    val dirOutNum = cgraFCParam.dirNum
    val hasReg = cgraFCParam.hasReg
    val matrixFCParamInst = MatrixFCParam(
      lastInNum,
      nextInNum,
      dirInnum,
      nextOutWithoutConstNum,
      nextOutWithConstNum,
      dirOutNum,
      hasReg,
      cgraFCParam.width,
      ""
    )
    val findIndex = matrixNameMap.indexWhere{inst => inst ==  matrixFCParamInst}
    if(findIndex == -1){
      matrixFCParamInst.deviceName = "matrixFC" +"device" +matrixNameMap.size
      matrixNameMap.append(matrixFCParamInst)
      addModule(MatrixFC(matrixFCParamInst,"matrixFC"+i.toString))
    }else{
      addModule(MatrixFC(matrixNameMap(findIndex),"matrixFC"+i.toString))
    }

  }



  //添加第一行的matrix
  val lastInNumHead = cgraFCParam.inNum
  val nextInNumHead = cgraFCParam.PENums(0)
  val dirInnumHead = 0
  val nextOutWithoutConstNumHead = pesInNums(0).sum - cgraFCParam.PENums(0)
  val nextOutWithConstNumHead = cgraFCParam.PENums(0)
  val dirOutNumHead = cgraFCParam.dirNum
  val hasRegHead = cgraFCParam.hasReg
  val matrixFCParamInstHead = MatrixFCParam(
    lastInNumHead,
    nextInNumHead,
    dirInnumHead,
    nextOutWithoutConstNumHead,
    nextOutWithConstNumHead,
    dirOutNumHead,
    hasRegHead,
    cgraFCParam.width,
    ""
  )
  val findIndexHead = matrixNameMap.indexWhere{inst => inst ==  matrixFCParamInstHead}
  if(findIndexHead == -1){
    matrixFCParamInstHead.deviceName = "matrixFC" +"device" +matrixNameMap.size
    matrixNameMap.append(matrixFCParamInstHead)
    addModule(MatrixFC(matrixFCParamInstHead,"matrixFCHead"))
  }else{
    addModule(MatrixFC(matrixNameMap(findIndexHead),"matrixFCHead"))
  }



  //添加最后一行matrix
  val lastInNumTail = cgraFCParam.PENums.last
  val nextInNumTail = 0
  val dirInnumTail = cgraFCParam.dirNum
  val nextOutWithoutConstNumTail = cgraFCParam.outNum
  val nextOutWithConstNumTail = 0
  val dirOutNumTail = 0
  val hasRegTail = cgraFCParam.hasReg
  val matrixFCParamInstTail = MatrixFCParam(
    lastInNumTail,
    nextInNumTail,
    dirInnumTail,
    nextOutWithoutConstNumTail,
    nextOutWithConstNumTail,
    dirOutNumTail,
    hasRegTail,
    cgraFCParam.width,
    ""
  )
  val findIndexTail = matrixNameMap.indexWhere { inst => inst == matrixFCParamInstTail }
  if (findIndexTail == -1) {
    matrixFCParamInstTail.deviceName = "matrixFC" + "device" + matrixNameMap.size
    matrixNameMap.append(matrixFCParamInstTail)
    addModule(MatrixFC(matrixFCParamInstTail, "matrixFCTail"))
  }else{
    addModule(MatrixFC(matrixNameMap(findIndexTail),"matrixFCTail"))
  }



  //matrix <-> PE
  for(i <- 0 until cgraFCParam.PENums.size){
    var inWithoutConst = 0
    for(j <- 0 until cgraFCParam.PENums(i)){
      if(i == 0){
        for(k <- 0 until pesInNums(i)(j) -1){
          addConnect(("matrixFCHead","nextOutWithoutConst" +inWithoutConst.toString),("PE"+i.toString+"_"+j.toString,"in"+k.toString) )
          inWithoutConst = inWithoutConst +1
        }
        addConnect(("matrixFCHead","nextOutWithConst" +j.toString),("PE"+i.toString+"_"+j.toString,"in"+(pesInNums(i)(j) -1).toString) )
      }else{
        for(k <- 0 until pesInNums(i)(j) -1){
          addConnect(("matrixFC" + (i - 1).toString,"nextOutWithoutConst" +inWithoutConst.toString),("PE"+i.toString+"_"+j.toString,"in"+k.toString) )
          inWithoutConst = inWithoutConst +1
        }
        addConnect(("matrixFC" + (i - 1).toString,"nextOutWithConst" +j.toString),("PE"+i.toString+"_"+j.toString,"in"+ (pesInNums(i)(j) -1).toString))
      }
    }
  }
  //PE -> Matrix
  for(i <- 0 until cgraFCParam.PENums.size){
    for(j <- 0 until cgraFCParam.PENums(i)) {
      //pe到上一层
      if (i == 0) {
        addConnect(("PE" + i.toString + "_" + j.toString, "out0"),("matrixFCHead","nextIn"+j.toString))
      } else {
        addConnect(("PE" + i.toString + "_" + j.toString, "out0"),("matrixFC" +( i-1).toString,"nextIn"+j.toString))
      }
      //pe到下一层
      if (i == cgraFCParam.PENums.size - 1) {
        addConnect(("PE" + i.toString + "_" + j.toString, "out0"),("matrixFCTail","lastIn" + j.toString))
      } else {
        addConnect(("PE" + i.toString + "_" + j.toString, "out0"),("matrixFC" +i.toString,"lastIn" +j.toString))
      }
    }
  }

  //matrix -> matrix
  for(i <- 0 until cgraFCParam.PENums.size - 2){
    for (j <- 0 until cgraFCParam.dirNum) {
      addConnect(("matrixFC" + i.toString, "dirOut" + j.toString),("matrixFC" + (i+1).toString, "dirIn" + j.toString))
    }
  }
if(cgraFCParam.PENums.size >1 ) {
  for (j <- 0 until cgraFCParam.dirNum) {
    addConnect(("matrixFCHead", "dirOut" + j.toString), ("matrixFC0", "dirIn" + j.toString))
  }
  for (j <- 0 until cgraFCParam.dirNum) {
    addConnect(("matrixFC" + (cgraFCParam.PENums.size - 2).toString, "dirOut" + j.toString), ("matrixFCTail", "dirIn" + j.toString))
  }
}else {
  for (j <- 0 until cgraFCParam.dirNum) {
    addConnect(("matrixFCHead", "dirOut" + j.toString),  ("matrixFCTail", "dirIn" + j.toString))
  }
}

  //in -> matrix
  for( i <- 0 until cgraFCParam.inNum){
    val IB = new EleTrace("IB" + i.toString, TYPE_IB.id, List("in0"), List("out0"), List(width))
    addEle(IB)
    addConnect(("this","in"+i.toString),("IB" + i.toString,"in0"))
    addConnect(("IB" + i.toString,"out0"),("matrixFCHead","lastIn" +i.toString))
  }

  for(i <- 0 until cgraFCParam.outNum){
    val OB = new EleTrace("OB" + i.toString, TYPE_OB.id, List("in0"), List("out0"), List(1, width))
    addEle(OB)
    addConnect(("matrixFCTail","nextOutWithoutConst"+ i.toString),("OB" + i.toString,"in0"))
    addConnect(("OB" + i.toString,"out0"),("this","out"+i.toString))
  }








}

object CGRAFCGen {
  val cgraParam = CGRAFCParam(16,16,1,List(8,8),true,32)
  val opListBuf = new ListBuffer[List[List[OPC.OPC]]]()
  (0 until 2).map {
    i => {
      val opListRow = new ListBuffer[List[OPC.OPC]]()
      (0 until 8).map {
        i =>
          opListRow.append(List(OPC.ADD, OPC.MUL , OPC.SUB,OPC.OR, OPC.XOR, OPC.ACC,OPC.PASS))
      }
      opListBuf.append(opListRow.toList)
    }

  }

  val CGRAFCModule = new CGRAFCTop(cgraParam, opListBuf.toList).getModuleInfo()

}

object CGRAGenAPP extends App {
  infooutput("CGRAFC16161281.scala", CGRAFCGen.CGRAFCModule)
  chisel3.Driver.execute(args, () => topGen(CGRAFCGen.CGRAFCModule, "CGRAFC16161281.txt")) //生成verilog
  dumpIR(CGRAFCGen.CGRAFCModule, "CGRAFC16161281.xml", "CGRA")
}
