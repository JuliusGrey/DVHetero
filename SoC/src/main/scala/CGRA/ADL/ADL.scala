package CGRA.ADL

import CGRA.IR.Info2Xml.dumpIR
import CGRA.module.ModuleInfo
import CGRA.module.DumpInfo.infooutput
import CGRA.module.TopModule.topGen
import CGRA.parameter.EleType._

import scala.collection.mutable.{ArrayBuffer, ListBuffer, Map}

object ADL {
  abstract  class BaseInfo
  {
      var baseName: String = ""
      var baseType: Int = -1
      var inPorts: List[String] = List("")
      var outPorts: List[String] = List("")

    def setType ( baseType: Int): Unit = {
      this.baseType = baseType
    }

    def setName(baseName: String): Unit = {
      this.baseName = baseName
    }

    def setIns(inPorts: List[String]): Unit = {
      this.inPorts = inPorts
    }

    def addIn(inPorts: List[String]): Unit = {
      this.inPorts = this.inPorts ++ inPorts
    }

    def setOuts(outPorts: List[String]): Unit = {
      this.outPorts = outPorts
    }

    def addOut(outPorts: List[String]): Unit = {
      this.outPorts = this.outPorts ++ outPorts
    }

    def getType() :Int = {
      baseType
    }

    def getNmae() :String = {
      baseName
    }

    def getIns () :List[String] = {
      inPorts
    }
    def getOuts(): List[String] = {
      outPorts
    }

  }
  class EleTrace(eleName:String, eleType : Int = 0,eleInPorts: List[String] = List(),eleOutPorts: List[String]  = List(),eleParamList: List[Int] = List() ) extends BaseInfo{
    baseName = eleName
    baseType = eleType
    inPorts = eleInPorts
    outPorts = eleOutPorts
    var paramList : List[Int] = eleParamList
    def setparam(paramList :List[Int]) :Unit = {
      this.paramList = paramList
    }
    def getParam() : List[Int]= {
      paramList
    }
  }

  class ModuleTrace(moduleName :String ,  modTypeStr : String ="", modDeviceStr : String="", modWidth: Int = 0,modInPorts: List[String] = List(),modOutPorts: List[String]= List()) extends BaseInfo{
    baseType = TYPE_Module.id
    baseName = moduleName
    inPorts = modInPorts
    outPorts = modOutPorts
    var typeStr = modTypeStr
    var deviceStr = modDeviceStr
    var width = modWidth

    val moduleListBuf: ListBuffer[ModuleInfo] = new ListBuffer[ModuleInfo]()
    val moduleInstsBuf: ListBuffer[Int] = new ListBuffer[Int]()
    val elementListsBuf: Map[Int, ListBuffer[List[Int]]] = Map()
    val connectsBuf : Map[List[Int] , ArrayBuffer[List[Int]]] = Map()

    val nameInfoMap :Map[String, BaseInfo] = Map()
    val nameIndexMap : Map[String,Int] = Map()

    def getModuleInfo() : ModuleInfo = {
      val connectList = connectsBuf.map{
        case(source ,sinksBuf) => {
          (source,sinksBuf.toList)
        }
      }.toList
      val eleLise = elementListsBuf.map{
        case(eleType , eleBuf) => {
          (eleType -> eleBuf.toList)
        }
      }
      new ModuleInfo(typeStr,deviceStr,moduleListBuf.toList,moduleInstsBuf.toList,eleLise,inPorts,outPorts,connectList,width)
    }


    def setWidth(width: Int): Unit = {
      this.width = width
    }

    def geWidth(): Int = {
      width
    }

    def addModule(module : ModuleTrace) : Unit ={
      val modTypeIndex =  moduleListBuf.indexWhere{mod => mod.getName() ==module.deviceStr }
      if(modTypeIndex == -1){
        moduleListBuf.append(module.getModuleInfo())
        moduleInstsBuf.append(moduleListBuf.size - 1)
      }else{
        moduleInstsBuf.append(modTypeIndex)
      }
      nameInfoMap += (module.baseName -> module)
      nameIndexMap += (module.baseName -> (moduleInstsBuf.size - 1))
    }

    def addEle(ele: EleTrace) : Unit = {
      if(!elementListsBuf.contains(ele.baseType)){
        elementListsBuf += (ele.baseType ->  new ListBuffer[List[Int]]())
      }
      elementListsBuf(ele.baseType).append(ele.paramList)
      nameInfoMap += (ele.baseName -> ele)
      nameIndexMap += (ele.baseName -> (elementListsBuf(ele.baseType).size - 1))
    }

    def addConnect(from: Tuple2[String, String] , to : Tuple2[String,String]) : Unit ={
//      val fromType :Int = if(from._1 == "this"){
//        TYPE_IO.id
//      }else{
//        if(nameInfoMap.contains(from._1)){
//          nameInfoMap(from._1).baseType
//        }else{
//          -1
//        }
//      }

      val fromList : List[Int] = if(from._1 == "this"){
        List(TYPE_IO.id ,inPorts.indexOf(from._2) )
      }else{
        List(
          nameInfoMap(from._1).baseType,
          nameIndexMap(from._1),
          nameInfoMap(from._1).outPorts.indexOf(from._2)
        )
      }
      if(!connectsBuf.contains(fromList)){
        connectsBuf += fromList -> new ArrayBuffer[List[Int]]()
      }

      val toList : List[Int] = if(to._1 == "this"){
        List(TYPE_IO.id, outPorts.indexOf((to._2)))
      }else{
        List(
          nameInfoMap(to._1).baseType,
          nameIndexMap(to._1),
          nameInfoMap(to._1).inPorts.indexOf((to._2))
        )
      }
      connectsBuf(fromList).append(toList)
    }

  }

}

object ADLTest extends App{

  val mux0 = new ADL.EleTrace("mux0",TYPE_Multiplexer.id,
    List("in0", "in1", "in2", "in3"),List("out"),List(4, 32))
  val PETest = new ADL.ModuleTrace(
    "PETest" , "PETest", "PETest", 32,
      List("in0", "in1", "in2", "in3"),List("out")
  )
  PETest.addEle(mux0)
  for(i <- 0 until 4){
    PETest.addConnect(("this", "in" + i.toString), ("mux0", "in"+i.toString))
  }
  PETest.addConnect(("mux0", "out"), ("this", "out"))


//  PETest.addConnect(("this", "in0"), ("mux0", "in0"))
//  PETest.addConnect(("this", "in1"), ("mux0", "in1"))
//  PETest.addConnect(("this", "in2"), ("mux0", "in2"))
//  PETest.addConnect(("this", "in3"), ("mux0", "in3"))
//  PETest.addConnect(("mux0", "out"), ("this", "out"))


  infooutput("PETest.scala", PETest.getModuleInfo())
  dumpIR(PETest.getModuleInfo(),"PETest.xml","top")
//  chisel3.Driver.execute(args,() => topGen(test.getModuleInfo(), "test.txt") )//生成verilog
}
