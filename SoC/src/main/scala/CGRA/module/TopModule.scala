package CGRA.module

//import ARCH.moduleIns.IB
import CGRA.IR.Info2Xml.getSubModIndex
import CGRA.element.ALU.ALU
import CGRA.element.{CfgMem, ConstUnit, Delay, IB, Multiplexer, MultiplexerR, OB, Register}
import CGRA.module.debug.{dbList, dpicCGRA, dpicDebug}
import CGRA.parameter.EleType
import CGRA.parameter.EleType._
import CGRA.parameter.EleTypeFun._
import CGRA.parameter.Param.cfgDataW
import chisel3._
import chisel3.util._
//import element._
//
//import module.ModuleInfo
import java.io.FileWriter
import scala.collection.mutable.{ArrayBuffer, ListBuffer, Map}

object TopModule {
  val cgfWidthList: ListBuffer[Int] = ListBuffer()

  def getWidthList(moduleInfo: ModuleInfo):Unit = {
    cgfWidthList.clear()
    var lastAddr = 0

    def getAddrWSub(level : Int,moduleInfo: ModuleInfo):Unit = {
      val elesAddr = (moduleInfo.getCfgW() + cfgDataW - 1)/cfgDataW
      if(lastAddr < elesAddr) lastAddr = elesAddr
      if(moduleInfo.getModuleInsts().size != 0){
        val thisAddrW = log2Ceil(moduleInfo.getModuleInsts().size + 1)
        if(cgfWidthList.size < level + 1 )cgfWidthList.append(thisAddrW)
        else{
          if(cgfWidthList(level) < thisAddrW) cgfWidthList(level) = thisAddrW
        }
        moduleInfo.getModuleInsts().foreach{
          index => getAddrWSub(level + 1 , moduleInfo.getModuleList()(index))
        }
      }
    }
    getAddrWSub(0,moduleInfo)
    cgfWidthList.append(log2Ceil(lastAddr))
  }

  class ModuleGen(
                 cfgFile:String,
                   modInsName : String,
                   moduleInfo: ModuleInfo,
                   cfgList:List[Int]
                 )extends Module{




    override val desiredName = moduleInfo.getName()
    val cfgLevel = cfgList.indexOf(-1)
    val cfgAddrW = if(cfgLevel == 0){
      cgfWidthList.foldLeft(0)((a,b) => a+b)
    }else if(cfgLevel == -1){
      cgfWidthList.last
    }else{
      (0 until cgfWidthList.size - cfgLevel ).map{
        i => cgfWidthList(i + cfgLevel  )
      }.foldLeft(0)((a,b) => a+b)
    }

    var file:FileWriter = new FileWriter( cfgFile , true)
    val spaceNum = if(cfgLevel == -1) cgfWidthList.size else cfgLevel
    file.write("\n" + "#"*4*spaceNum +modInsName + " :")
    if (cfgLevel == 0) {
      file.write(" " + 0)
    } else if (cfgLevel == -1) {
      cfgList.foreach(ins => file.write(" " + ins))
    } else {
      for (i <- 0 until cfgLevel) {
        file.write(" " + cfgList(i))
      }
    }


    var eleAddrOff= 0
    moduleInfo.getElementLists().toList.sortBy(_._1).map{
      case(eleType , eleList) =>{
        eleList.zipWithIndex.map{
          case(ele,index) =>{
            val headBase = eleAddrOff/cfgDataW
            val headOff = eleAddrOff%cfgDataW
            eleAddrOff = eleAddrOff + getEleCfgW(eleType,ele) - 1
            val tailBase  = eleAddrOff/cfgDataW
            val tailOff = eleAddrOff%cfgDataW
            eleAddrOff = eleAddrOff + 1
            val eleName = eleTypeName(eleType) + index
            if(getEleCfgW(eleType,ele) != 0)
            file.write("\n" + "#" * 4 * (spaceNum + 1) + eleName + " :" +
              "from " + headBase.toString + " " + headOff.toString + " " + "to " + tailBase.toString + " " + tailOff.toString + " \n")

          }
        }
      }
    }

    file.close()









    val width = moduleInfo.getWidth()
    val io = IO(new Bundle() {
      val cfgEn = Input(Bool())
      val cfgAddr = Input(UInt(cfgAddrW.W))
      val cfgData =Input(UInt(cfgDataW.W))
      val inputs = Input(Vec(moduleInfo.getInPortList().size, UInt((width + 1).W)))
      val outputs = Output(Vec(moduleInfo.getOutPortList().size, UInt((width + 1).W)))
    })
//    println("name is " + moduleInfo.getName())
//    if(moduleInfo.getName().contains("_debug")){
//      val monitor = Module(new dpicCGRA(io.inputs.size))
//      (0 until io.inputs.size).map {
//        i => {
//          monitor.io.ins(i) := io.inputs(i)
//        }
//      }
//    }

//    val cfgEn = if(cfgLevel == 0 ) {
//      io.cfgEn
//    }else{
//      io.cfgEn && io.cfgAddr(cfgAddrW - 1 ,cfgAddrW -  cgfWidthList(cfgLevel - 1) )=== cfgList(cfgLevel - 1).U
//    }

    val sinksUCon = new ArrayBuffer[List[Int]]
    val memberList : Map[Int, ArrayBuffer[Any]] = Map()
    (0 until moduleInfo.getOutPortList().size).map{
      i => sinksUCon.append(List(TYPE_IO.id, i))
    }

    memberList += (TYPE_Module.id -> new ArrayBuffer[Any])
    moduleInfo.getModuleInsts().zipWithIndex.map{
      case(modType, i) =>{
        val moduleInst = moduleInfo.getModuleList()(modType)
        for(j <- 0 until moduleInst.getInPortList().size){
           sinksUCon.append(List(TYPE_Module.id, i, j))
        }

        val cfgListSub = cfgList.to[ListBuffer]
        if(cfgLevel == -1){
          println("there is error")
        }else{
          cfgListSub(cfgLevel)= i + 1
        }

        val moduleSub = Module(new ModuleGen(cfgFile,moduleInst.getType()+getSubModIndex(i,moduleInfo.getModuleInsts(),moduleInfo.getModuleList()),moduleInst,cfgListSub.toList)).suggestName(moduleInst.getType()+getSubModIndex(i,moduleInfo.getModuleInsts(),moduleInfo.getModuleList()))
        memberList(TYPE_Module.id).append(moduleSub)
        moduleSub.io.cfgEn := io.cfgEn && io.cfgAddr(cfgAddrW - 1 ,cfgAddrW -  cgfWidthList(cfgLevel ) ) === (i + 1).U
        moduleSub.io.cfgAddr := io.cfgAddr
        moduleSub.io.cfgData := io.cfgData
        if (dbList.contains(moduleInst.getType()+getSubModIndex(i,moduleInfo.getModuleInsts(),moduleInfo.getModuleList()))) {
          (0 until moduleSub.io.outputs.size).map{
            i => {
              val debug = Module(new dpicDebug)
              debug.io.in := moduleSub.io.outputs(i)
            }
          }
//          val debug = Module(new dpicDebug(moduleSub.io.outputs.size))
//          (0 until moduleSub.io.outputs.size).map {
//            i => {
//              debug.io.ins(i) := moduleSub.io.outputs(i)
//            }
//          }
        }
      }
    }

    val eleLists = moduleInfo.getElementLists().toList.sortBy(_._1)
    eleLists.map{
      case(eleType , eleList) =>{
        memberList += (eleType  -> new ArrayBuffer[Any])
        for( i <- 0 until eleList.size){
          EleType(eleType) match {
            case TYPE_ALU =>{
              memberList(eleType).append(Module(new ALU(eleTypeName(eleType) + i,eleList(i)(0),eleList(i)(1))).suggestName(eleTypeName(eleType) + i))
               for (j <- 0 until memberList(eleType)(i).asInstanceOf[ALU].io.inputs.size){
                 sinksUCon.append(List(eleType,i,j))
               }
            }
            case TYPE_ConstUnit => {
              memberList(eleType).append(Module(new ConstUnit(eleTypeName(eleType) + i, eleList(i)(0))).suggestName(eleTypeName(eleType) + i))
            }
            case TYPE_Delay => {
              memberList(eleType).append(Module(new Delay(eleTypeName(eleType) + i, eleList(i)(0), eleList(i)(1))).suggestName(eleTypeName(eleType) + i))
                sinksUCon.append(List(eleType, i, 0))
            }
            case TYPE_Multiplexer => {
              memberList(eleType).append(Module(new Multiplexer(eleTypeName(eleType) + i, eleList(i)(0), eleList(i)(1))).suggestName(eleTypeName(eleType) + i))
              for (j <- 0 until memberList(eleType)(i).asInstanceOf[Multiplexer].io.inputs.size) {
                sinksUCon.append(List(eleType, i, j))
              }
            }
            case TYPE_MultiplexerR => {
              memberList(eleType).append(Module(new MultiplexerR(eleTypeName(eleType) + i, eleList(i)(0), eleList(i)(1))).suggestName(eleTypeName(eleType) + i))
              for (j <- 0 until memberList(eleType)(i).asInstanceOf[MultiplexerR].io.inputs.size) {
                sinksUCon.append(List(eleType, i, j))
              }
            }

            case TYPE_Register => {
              memberList(eleType).append(Module(new Register(eleTypeName(eleType) + i, eleList(i)(0))).suggestName(eleTypeName(eleType) + i))
                sinksUCon.append(List(eleType, i, 0))
            }
            case TYPE_IB => {
              memberList(eleType).append(Module(new IB(eleTypeName(eleType) + i, eleList(i)(0))).suggestName(eleTypeName(eleType) + i))
              sinksUCon.append(List(eleType, i, 0))
            }
            case TYPE_OB => {
              memberList(eleType).append(Module(new OB(eleTypeName(eleType) + i, eleList(i)(0), eleList(i)(1))).suggestName(eleTypeName(eleType) + i))
              for (j <- 0 until memberList(eleType)(i).asInstanceOf[OB].io.inputs.size) {
                sinksUCon.append(List(eleType, i, j))
              }
            }
          }
        }
      }
    }


    if(moduleInfo.getCfgW() != 0){
      val cfgMemIns = Module(new CfgMem(moduleInfo.getCfgW()))
//      if(moduleInfo.getModuleInsts().size == 0){
//        cfgMemIns.io.cfgEn := cfgEn
//      }else{
//        cfgMemIns.io.cfgEn := cfgEn && io.cfgAddr(cfgAddrW - 1 ,cfgAddrW -  cgfWidthList(cfgLevel ) )===0.U
//      }
            if(moduleInfo.getModuleInsts().size == 0){
              cfgMemIns.io.cfgEn := io.cfgEn
            }else {
              cfgMemIns.io.cfgEn := io.cfgEn && io.cfgAddr(cfgAddrW - 1, cfgAddrW - cgfWidthList(cfgLevel)) === 0.U
            }
      if(cfgMemIns.io.cfgAddr.getWidth != 0) {
        cfgMemIns.io.cfgAddr := io.cfgAddr(cfgMemIns.io.cfgAddr.getWidth - 1, 0)
      }else{
        cfgMemIns.io.cfgAddr := DontCare
      }
      cfgMemIns.io.cfgData := io.cfgData


      val eleCfgLists = moduleInfo.getCfgList().toList.sortBy(i => i._1)
      var eleCfgOff= 0
      eleCfgLists.map{
        case(eleType , eleCfgList) =>{
          eleCfgList.zipWithIndex.map{
            case(eleCfgW,i) =>{
              EleType(eleType) match {
                case TYPE_ALU => memberList(eleType)(i).asInstanceOf[ALU].io.cfg := cfgMemIns.io.cfgOut(eleCfgW + eleCfgOff - 1 ,eleCfgOff)
                case TYPE_ConstUnit => memberList(eleType)(i).asInstanceOf[ConstUnit].io.cfg := cfgMemIns.io.cfgOut(eleCfgW + eleCfgOff - 1 ,eleCfgOff)
                case TYPE_Delay => memberList(eleType)(i).asInstanceOf[Delay].io.cfg := cfgMemIns.io.cfgOut(eleCfgW + eleCfgOff - 1 ,eleCfgOff)
                case TYPE_Multiplexer => memberList(eleType)(i).asInstanceOf[Multiplexer].io.cfg := cfgMemIns.io.cfgOut(eleCfgW + eleCfgOff - 1 ,eleCfgOff)
                case TYPE_MultiplexerR => memberList(eleType)(i).asInstanceOf[MultiplexerR].io.cfg := cfgMemIns.io.cfgOut(eleCfgW + eleCfgOff - 1, eleCfgOff)
                case TYPE_OB => memberList(eleType)(i).asInstanceOf[OB].io.cfg := cfgMemIns.io.cfgOut(eleCfgW + eleCfgOff - 1 ,eleCfgOff)
//                case TYPE_Register.id => memberList(eleType)(i).asInstanceOf[Register].io.cfg := cfgMemIns.io.cfgOut(eleCfgW + eleCfgOff - 1 ,eleCfgOff)
                case _ =>
              }
              eleCfgOff = eleCfgOff + eleCfgW
            }
          }
        }
      }

    }

    //diff_accelerator
//    val monitor = Module(new dpicCGRA(io.inputs.size))
//    (0 until io.inputs.size).map {
//      i => {
//        if (modInsName == "TOP")
//          monitor.io.ins(i) := io.inputs(i)
//        else
//          monitor.io.ins(i) := DontCare
//      }
//    }
//    if (modInsName != "TOP")
//      (0 until 2).map {
//        i => monitor.io.outs(i) := DontCare
//      }

    if(modInsName == "TOP") {
      //diff_acceelerator
      val monitor = Module(new dpicCGRA(io.inputs.size))


        (0 until io.inputs.size).map {
          i => {
            monitor.io.ins(i) := io.inputs(i)
          }
        }




      moduleInfo.getConnect().map {
        case (source, sinkList) => {
          val fromPort = {
            EleType(source(0)) match {
              case TYPE_IO => io.inputs(source(1))
              case TYPE_Module => memberList(source(0))(source(1)).asInstanceOf[ModuleGen].io.outputs(source(2))
              case TYPE_ALU => memberList(source(0))(source(1)).asInstanceOf[ALU].io.outputs(source(2))
              case TYPE_ConstUnit => memberList(source(0))(source(1)).asInstanceOf[ConstUnit].io.outputs(source(2))
              case TYPE_Delay => memberList(source(0))(source(1)).asInstanceOf[Delay].io.outputs(source(2))
              case TYPE_Multiplexer => memberList(source(0))(source(1)).asInstanceOf[Multiplexer].io.outputs(source(2))
              case TYPE_MultiplexerR => memberList(source(0))(source(1)).asInstanceOf[MultiplexerR].io.outputs(source(2))
              case TYPE_Register => memberList(source(0))(source(1)).asInstanceOf[Register].io.outputs(source(2))
              case TYPE_IB => memberList(source(0))(source(1)).asInstanceOf[IB].io.outputs(source(2))
              case TYPE_OB => memberList(source(0))(source(1)).asInstanceOf[OB].io.outputs(source(2))
              case _ => DontCare
            }
          }

          sinkList.map {
            sink => {
              val toPort =
                EleType(sink(0)) match {
                  case TYPE_IO => io.outputs(sink(1))
                  case TYPE_Module => memberList(sink(0))(sink(1)).asInstanceOf[ModuleGen].io.inputs(sink(2))
                  case TYPE_ALU => memberList(sink(0))(sink(1)).asInstanceOf[ALU].io.inputs(sink(2))
                  //              case TYPE_ConstUnit.id =>
                  case TYPE_Delay => memberList(sink(0))(sink(1)).asInstanceOf[Delay].io.inputs(sink(2))
                  case TYPE_Multiplexer => memberList(sink(0))(sink(1)).asInstanceOf[Multiplexer].io.inputs(sink(2))
                  case TYPE_MultiplexerR => memberList(sink(0))(sink(1)).asInstanceOf[MultiplexerR].io.inputs(sink(2))
                  case TYPE_Register => memberList(sink(0))(sink(1)).asInstanceOf[Register].io.inputs(sink(2))
                  case TYPE_IB => memberList(sink(0))(sink(1)).asInstanceOf[IB].io.inputs(sink(2))
                  case TYPE_OB => memberList(sink(0))(sink(1)).asInstanceOf[OB].io.inputs(sink(2))
                  case _ => DontCare
                }
              //diff_accelerator
//              if (modInsName == "TOP") {
//                if (EleType(sink(0)) == TYPE_IO) {
//                  if (sink(1) == 4) {
//                    monitor.io.outs(0) := fromPort
//                  }
//                  if (sink(1) == 9) {
//                    monitor.io.outs(1) := fromPort
//                  }
//                }
//              }

              toPort := fromPort
              sinksUCon -= sink
            }
          }
        }
      }
    } else {
      moduleInfo.getConnect().map {
        case (source, sinkList) => {
          val fromPort = {
            EleType(source(0)) match {
              case TYPE_IO => io.inputs(source(1))
              case TYPE_Module => memberList(source(0))(source(1)).asInstanceOf[ModuleGen].io.outputs(source(2))
              case TYPE_ALU => memberList(source(0))(source(1)).asInstanceOf[ALU].io.outputs(source(2))
              case TYPE_ConstUnit => memberList(source(0))(source(1)).asInstanceOf[ConstUnit].io.outputs(source(2))
              case TYPE_Delay => memberList(source(0))(source(1)).asInstanceOf[Delay].io.outputs(source(2))
              case TYPE_Multiplexer => memberList(source(0))(source(1)).asInstanceOf[Multiplexer].io.outputs(source(2))
              case TYPE_MultiplexerR => memberList(source(0))(source(1)).asInstanceOf[MultiplexerR].io.outputs(source(2))
              case TYPE_Register => memberList(source(0))(source(1)).asInstanceOf[Register].io.outputs(source(2))
              case TYPE_IB => memberList(source(0))(source(1)).asInstanceOf[IB].io.outputs(source(2))
              case TYPE_OB => memberList(source(0))(source(1)).asInstanceOf[OB].io.outputs(source(2))
              case _ => DontCare
            }
          }

          sinkList.map {
            sink => {
              val toPort =
                EleType(sink(0)) match {
                  case TYPE_IO => io.outputs(sink(1))
                  case TYPE_Module => memberList(sink(0))(sink(1)).asInstanceOf[ModuleGen].io.inputs(sink(2))
                  case TYPE_ALU => memberList(sink(0))(sink(1)).asInstanceOf[ALU].io.inputs(sink(2))
                  //              case TYPE_ConstUnit.id =>
                  case TYPE_Delay => memberList(sink(0))(sink(1)).asInstanceOf[Delay].io.inputs(sink(2))
                  case TYPE_Multiplexer => memberList(sink(0))(sink(1)).asInstanceOf[Multiplexer].io.inputs(sink(2))
                  case TYPE_MultiplexerR => memberList(sink(0))(sink(1)).asInstanceOf[MultiplexerR].io.inputs(sink(2))
                  case TYPE_Register => memberList(sink(0))(sink(1)).asInstanceOf[Register].io.inputs(sink(2))
                  case TYPE_IB => memberList(sink(0))(sink(1)).asInstanceOf[IB].io.inputs(sink(2))
                  case TYPE_OB => memberList(sink(0))(sink(1)).asInstanceOf[OB].io.inputs(sink(2))
                  case _ => DontCare
                }

              toPort := fromPort
              sinksUCon -= sink
            }
          }
        }
      }
    }


















    sinksUCon.map(sink => {
      val toPort = EleType(sink(0)) match {
        case TYPE_IO => io.outputs(sink(1))
        case TYPE_Module => memberList(sink(0))(sink(1)).asInstanceOf[ModuleGen].io.inputs(sink(2))
        case TYPE_ALU => memberList(sink(0))(sink(1)).asInstanceOf[ALU].io.inputs(sink(2))
        //              case TYPE_ConstUnit.id =>
        case TYPE_Delay => memberList(sink(0))(sink(1)).asInstanceOf[Delay].io.inputs(sink(2))
        case TYPE_Multiplexer => memberList(sink(0))(sink(1)).asInstanceOf[Multiplexer].io.inputs(sink(2))
        case TYPE_MultiplexerR => memberList(sink(0))(sink(1)).asInstanceOf[MultiplexerR].io.inputs(sink(2))
        case TYPE_Register => memberList(sink(0))(sink(1)).asInstanceOf[Register].io.inputs(sink(2))
        case TYPE_IB => memberList(sink(0))(sink(1)).asInstanceOf[IB].io.inputs(sink(2))
        case TYPE_OB => memberList(sink(0))(sink(1)).asInstanceOf[OB].io.inputs(sink(2))
      }
      toPort := 0.U
    })



  }


  def topGen(module:ModuleInfo,cfgFile:String ) ={
    getWidthList(module)
    val file:FileWriter = new FileWriter( cfgFile , false)
    file.write("adress width : ")
    cgfWidthList.foreach(ins => file.write(" " + ins))
    file.write("\n")
    file.close()

    new ModuleGen(cfgFile,"TOP", module,(0 until cgfWidthList.size).map(ins => -1).toList)
  }
}
