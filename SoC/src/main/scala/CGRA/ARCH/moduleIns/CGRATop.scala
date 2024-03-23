package CGRA.ARCH.moduleIns

import CGRA.ADL.ADL.{EleTrace, ModuleTrace}
import CGRA.ARCH.moduleIns
import CGRA.ARCH.moduleIns.CGRAParam._
import CGRA.IR.Info2Xml.dumpIR
import CGRA.element.ALU.{OPC, OpInfo}
import CGRA.module.DumpInfo.infooutput
import CGRA.module.TopModule.topGen
import CGRA.parameter.DirParam._
import CGRA.parameter.EleType._

import scala.collection.mutable.ListBuffer

class CGRATop(cgraParam :CGRAParam ,pesOpList  : List[List[List[OPC.OPC]]]) extends ModuleTrace("CGRATop"){

  this.typeStr = "CGRA"
  this.deviceStr = "CGRAJ"
  this.width = cgraParam.width

  val rowNum = cgraParam.rowNum
  val cloNum = cgraParam.colNum
  val trackNum = cgraParam.trackNum
  val maxDelay = cgraParam.maxDelay
  val fList = cgraParam.fList
  val digConnect = cgraParam.digConnect
  val peOutNum = cgraParam.peOutNum

  val ioMod1 = cgraParam.ioMod == 1 //1表示上面和侧面都有输入，0表示只有上面有输入
  val dirMod1 = cgraParam.dirMod == 1 //1表示双向，0表示流水线，事实上这里即便是双向也不行吧，因为只有顶端有输入
  val inNum = if(ioMod1) cloNum + rowNum else cloNum

  val inPortsList = (0 until inNum).map("in" + _.toString).toList
  this.inPorts = inPortsList

  val outPortsList = (0 until cloNum).map("out" + _.toString).toList
  this.outPorts = outPortsList

  for( i <- 0 until rowNum + 1){
    val hasN = i != 0
    val hasS = i != rowNum
    for( j <- 0 until cloNum + 1){
      val hasW =  j != 0
      val hasE = j != cloNum


      val inTrickDirBuf = new ListBuffer[Int]()
      val outTrickDirBuf = new ListBuffer[Int]()
      var gibName = "GIB"
//      val whthR = ((i + j )%2 == 1 ) && (cgraParam.regMod == 1)
val whthR = (j==8 ||j==4||j==12) && (cgraParam.regMod == 1)
      if(whthR) gibName = gibName + "R"
      if(hasW){
        inTrickDirBuf.append(WEST)
        outTrickDirBuf.append(WEST)
        gibName = gibName + "_Wio"
      }
      if (hasN) {
        inTrickDirBuf.append(NORTH)
        if(dirMod1) {
          outTrickDirBuf.append(NORTH)
          gibName = gibName + "_Nio"
        }else {
          gibName = gibName + "_Ni"
        }
      }
//      if (hasE) {
        inTrickDirBuf.append(EAST)
        outTrickDirBuf.append(EAST)
        gibName = gibName + "_Eio"
//      }
      if (hasS) {
//        inTrickDirBuf.append(SOUTH)
        outTrickDirBuf.append(SOUTH)
        if(dirMod1){
          inTrickDirBuf.append(SOUTH)
          gibName = gibName + "_Sio"
        }else {
          gibName = gibName + "_So"
        }
      }

//      val numIOPinBuf = new ListBuffer[Int]()

      val NWONum = 0

      val NWINum = if(hasW || (j == 0 && i >= 1 && i <= rowNum/2 &&ioMod1)) {
        gibName = gibName + "_NWi"
        1
      } else {
        0
      }

      val NEONum = if(hasN && hasE ) {
        if(pesOpList(i - 1)( j).map(OpInfo.getOperandNum(_)).max >= 3) {
          gibName = gibName + "_NEo"
          1
        }else{
          0
        }
      } else {
        0
      }

      val NEINum =  if((hasE && ( !hasN || cgraParam.peOutNum >= 2)) || (j == cloNum  && i >= 1 && i <=(rowNum - rowNum/2)  &&ioMod1)) {
        gibName = gibName + "_NEi"
        1
      } else {
        0
      }

      val SEONum = if(hasE &&(!hasS || pesOpList(i)( j).map(OpInfo.getOperandNum(_)).max >= 2)){
        gibName = gibName + "_SEo"
        1
      }else{
        0
      }

      val SEINum = if((hasE && hasS && cgraParam.peOutNum >=3) || (j == cloNum && i <= (rowNum - rowNum/2 - 1 ) &&ioMod1)){
        gibName = gibName + "_SEi"
        1
      }else{
        0
      }

      val SWONum = if(hasW ){
        gibName = gibName + "_SWo"
        1
      }else{
        0
      }

      val SWINum = if((hasW && hasS && cgraParam.peOutNum  >= 4) || (j == 0 && i <= rowNum/2 - 1 &&ioMod1)  ){
          gibName = gibName + "_SWi"
          1
      }else{
        0
      }


      val gibParam = GIBParam(inTrickDirBuf.toList,outTrickDirBuf.toList,cgraParam.trackNum,cgraParam.digConnect,
        List(NWONum,NWINum,NEONum,NEINum,SEONum,SEINum,SWONum,SWINum),
        cgraParam.fList,width,
        gibName,
        whthR
      )
      addModule(GIB(gibParam , "GIB"+i.toString+"_"+j.toString ))
    }
  }

  val OpNameMap: scala.collection.mutable.Map[Set[OPC.OPC], String] =  scala.collection.mutable.Map()
  for( i <- 0 until rowNum){
    for(j <- 0 until cloNum){
      val opList  = pesOpList(i)(j)
      if(!OpNameMap.contains(opList.toSet)) OpNameMap += (opList.toSet -> ("PE" + OpNameMap.size.toString))
      val peName =   OpNameMap(opList.toSet)
      val peParam = PEParam(pesOpList(i)(j) , width , peName )
      addModule(PE(peParam , "PE"+i.toString+"_"+j.toString ))
    }
  }

  for(i <- 0 until inNum){
//    val ibParam = IBParam(maxDelay, width , "IB")
//    addModule(IB(ibParam , "IB"+i.toString ))
val IB = new EleTrace("IB" + i.toString, TYPE_IB.id, List("in0"), List("out0"), List( width))
    addEle(IB)
  }

  for (i <- 0 until cloNum) {
    val OB = new EleTrace("OB" + i.toString, TYPE_OB.id, List("in0","in1"), List("out0"), List(2, width))

    addEle(OB)
  }


  //gib输入
  for (i <- 0 until rowNum + 1) {
    val hasN = i != 0
    val hasS = i != rowNum
    for (j <- 0 until cloNum + 1) {
      val hasW = j != 0
      val hasE = j != cloNum

      if (hasW) {
        for(k <- 0 until trackNum) {
          addConnect(("GIB" + i.toString + "_" + ( j - 1).toString, "otrackE" + k.toString), ("GIB" + i.toString + "_" + j.toString, "itrackW" + k.toString))
        }}
      if(hasN) {
        for (k <- 0 until trackNum)
          addConnect(("GIB" + (i - 1).toString + "_" + j.toString, "otrackS" + k.toString), ("GIB" + i.toString + "_" + j.toString, "itrackN" + k.toString))
      }
      if(hasE){
        for (k <- 0 until trackNum)
          addConnect(("GIB" + i.toString + "_" + (j + 1).toString, "otrackW" + k.toString), ("GIB" + i.toString + "_" + j.toString, "itrackE" + k.toString))
      }

      if(dirMod1 && hasS){
        for (k <- 0 until trackNum) {
          addConnect(("GIB" +( i + 1).toString + "_" + (j ).toString, "otrackN" + k.toString), ("GIB" + i.toString + "_" + j.toString, "itrackS" + k.toString))
        }

      }

      if(hasW){
        if(hasN){
          addConnect(("PE" + (i - 1).toString + "_" + (j - 1).toString, "out0"), ("GIB" + i.toString + "_" + j.toString, "opinNW0" ))
        }else{
          addConnect(("IB" + (j - 1).toString, "out0"), ("GIB" + i.toString + "_" + j.toString, "opinNW0" ))
        }
      }

      if(hasE){
        if(hasN ){
          if( peOutNum >= 2)
          addConnect(("PE" + (i - 1).toString + "_" + j.toString, "out0"), ("GIB" + i.toString + "_" + j.toString, "opinNE0"))
        }else{
          addConnect(("IB" + j.toString, "out0"), ("GIB" + i.toString + "_" + j.toString, "opinNE0" ))
        }
      }

      if(hasE && hasS &&peOutNum >= 3){
        addConnect(("PE" + i.toString + "_" + j.toString, "out0"), ("GIB" + i.toString + "_" + j.toString, "opinSE0"))
      }

      if(hasW && hasS && peOutNum >= 4){
        addConnect(("PE" + i.toString + "_" + (j - 1).toString, "out0"), ("GIB" + i.toString + "_" + j.toString, "opinSW0"))
      }

      if(j == 0 && i >= 1 && i <= rowNum/2 &&ioMod1 ){
        addConnect(("IB" +  (cloNum + i - 1 ).toString, "out0"), ("GIB" + i.toString + "_" + j.toString, "opinNW0" ))
      }

      if(j == 0 && i <= rowNum/2 - 1  &&ioMod1){
        addConnect(("IB" +  (cloNum + i ).toString, "out0"), ("GIB" + i.toString + "_" + j.toString, "opinSW0" ))
      }

      if (j == cloNum  && i >= 1 && i <=(rowNum - rowNum/2)  &&ioMod1) {
        addConnect(("IB" + (cloNum + rowNum/2 + i - 1).toString, "out0"), ("GIB" + i.toString + "_" + j.toString, "opinNE0"))
      }
      if (j == cloNum && i <= (rowNum - rowNum/2 - 1 )  &&ioMod1){
        addConnect(("IB" + (cloNum+ rowNum/2 + i).toString, "out0"), ("GIB" + i.toString + "_" + j.toString, "opinSE0"))
      }


    }
  }
//pe
  for (i <- 0 until rowNum) {
    for (j <- 0 until cloNum) {
      val opNum = pesOpList(i)( j).map(OpInfo.getOperandNum(_)).max
      addConnect(("GIB" + i.toString + "_" + (j + 1).toString , "ipinSW0"),("PE"+ i.toString + "_" + j.toString , "in0"))
      if(opNum >= 2)
      addConnect(("GIB" + i.toString + "_" + j.toString , "ipinSE0"),("PE"+ i.toString + "_" + j.toString , "in1"))
      if(opNum >= 3)
      addConnect(("GIB" + (i + 1).toString + "_" + j.toString , "ipinNE0"),("PE"+ i.toString + "_" + j.toString , "in2"))
    }
  }

  for(i <- 0 until cloNum){

    //ob
    addConnect(("GIB"+rowNum.toString + "_" + i.toString , "ipinSE0"),("OB" + i.toString , "in0"))
    addConnect(("GIB"+rowNum.toString + "_" + (i + 1).toString , "ipinSW0"),("OB" + i.toString , "in1"))


    //ib
    addConnect(("this" , "in" + i.toString),("IB" + i.toString , "in0"))

    //this
    addConnect(("OB" + i.toString, "out0"),("this" , "out" + i.toString))
  }
  for( i <- cloNum until inNum){
    addConnect(("this" , "in" + i.toString),("IB" + i.toString , "in0"))
  }



}

object CGRAGen {
  val rowNum = 4
  val colNum = 16

  val cgraParam = moduleIns.CGRAParam.CGRAParam(rowNum, colNum, 4, 32, 5, List(8, 8, 8), true, 4, 0, 0, 0)
  //  val rowNum = 4
//  val colNum = 4
//  val cgraParam = moduleIns.CGRAParam.CGRAParam(rowNum, colNum, 4, 32, 0, List(4, 4, 4), true, 4 , 1 , 1 , 1)


  val opListBuf = new ListBuffer[List[List[OPC.OPC]]]()
  (0 until rowNum).map {
    i => {
      val opListRow = new ListBuffer[List[OPC.OPC]]()
      (0 until colNum).map {
        i =>
          opListRow.append(List(OPC.ADD, OPC.MUL ,OPC.PASS , OPC.SUB,OPC.ACC ))
      }
      opListBuf.append(opListRow.toList)
    }

  }


  val CGRAModule = new CGRATop(cgraParam, opListBuf.toList).getModuleInfo()
}

object CGRAGenAPP extends App {

  infooutput("cgra41648880acc.scala", CGRAGen.CGRAModule)
  chisel3.Driver.execute(args,() => topGen(CGRAGen.CGRAModule, "cgra41648880acc.txt") )//生成verilog
  dumpIR(CGRAGen.CGRAModule,"cgra41648880acc.xml","CGRA")

}
