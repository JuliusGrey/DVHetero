package CGRA.IR

import CGRA.module.ModuleInfo
import CGRA.parameter.EleType._
import CGRA.parameter.EleTypeFun._

import scala.collection.mutable.{ArrayBuffer, ListBuffer, Map}
import java.io.FileWriter
import scala.xml._

object Info2Xml {
  def elemXml(elementList : List[(Int,List[List[Int]])]) : Elem = {
    val node = new NodeBuffer
    elementList.map {
      case(eleType , eleLise) =>
        eleLise.zipWithIndex.map{
          case( elePar, i ) => {
            val eleTypeStr = eleTypeName(eleType)
            val eleName = eleTypeStr + i
            val inNum = getEleInNum(eleType,elePar)
            val outNum = getEleOutNum(eleType,elePar)
            val width = getEleWidth(eleType,elePar)
//            val device = eleType match {
//              case EleType.TYPE_Multiplexer.id => eleTypeStr + "_"+ inNum.toString+ "_" + outNum.toString
//              case _ => eleTypeStr
//            }
            val device = if(eleType == TYPE_Multiplexer.id || eleType == TYPE_MultiplexerR.id) { eleTypeStr + "_"+ inNum.toString+ "_" + outNum.toString}else
//                         if(eleType == TYPE_ALU.id){
////                           if(elePar(0) == 2) "ADD"
////                           else if(elePar(0) == 8) "MUL"
////                           else "ADDMUL"
//
////                           elePar(0) match {
////                             case 2 => "ADD"
////                             case 4 => "SUB"
////                             case 8 => "MUL"
////                             case 6 => "ADDSUB"
////                             case 10 => "ADDMUL"
////                             case 12 => "MULSUB"
////                             case 14 => "ADDMULSUB"
////                             case _ => "ALL"
////
////                           }
//
//                         }  else
                         {eleTypeStr}
            node +=
              <element name={eleName} type={if(eleType == TYPE_ALU.id) device else if (eleType == TYPE_MultiplexerR.id) "MUX" else eleTypeStr} count="1" device={device} in_num={inNum.toString} out_num={outNum.toString} has_reg="0">
                <inputs name={(0 to inNum - 1).foldLeft("")((b, a) => b + getElePortName(eleType, true, a) + ",")}>
                  {(0 to inNum - 1).map(i => <input name={getElePortName(eleType, true, i)} width={width.toString}/>)}
                </inputs>
                <outputs name={(0 to outNum - 1).foldLeft("")((b, a) => b + getElePortName(eleType, false, a) + ",")}>
                  {(0 to outNum - 1).map(i => <output name={getElePortName(eleType, false, i)} width={width.toString}/>)}
                </outputs>
              </element>
          }
        }
    }
    val node_ : NodeSeq = node;
    <elements>
      {node_}
    </elements>
  }


  def getSubModIndex(modIndex: Int, moduleInsts: List[Int] , moduleList: List[ModuleInfo]): Int = {
    val modType = moduleInsts(modIndex)
    val modTypeStr = moduleList(modType).getType()
    var subIndex = 0
    (0 until modIndex).map { i => if (moduleList(moduleInsts(i)).getType() == modTypeStr) subIndex = subIndex + 1 }
    subIndex
  }


  def expXml(connect :  List[Tuple2[List[Int],List[List[Int]]]], moduleList: List[ModuleInfo], moduleInsts: List[Int],
             inPortList: List[String], outPortList: List[String]): Elem = {
    val node = new NodeBuffer
    connect.map{
      case(source , sinkList) => {
        if(source(0) == TYPE_IO.id){
          sinkList.map{
            sink => {
              if(sink(0) != TYPE_IO.id){
                val original = if(sink(0)== TYPE_Module.id){
                  moduleList(moduleInsts(sink(1))).getType() + getSubModIndex(sink(1),moduleInsts,moduleList)
                }else{
                  eleTypeName(sink(0)) + sink(1)
                }
                val portNameSrc = inPortList(source(1))
                val portNameSink = if(sink(0)== TYPE_Module.id){
                  moduleList(moduleInsts(sink(1))).getInPortList()(sink(2))
                }else{
                  getElePortName(sink(0),true,sink(2))
                }
                node += <export export_name={portNameSrc} original={original} original_id="0" port={portNameSink} type="in" />
              }
            }
          }
        }else{
          sinkList.map{
            sink => {}
              if(sink(0) == TYPE_IO.id){
                val original = if(source(0) == TYPE_Module.id){
                  moduleList(moduleInsts(source(1))).getType() + getSubModIndex(source(1),moduleInsts,moduleList)
                }else{
                  eleTypeName(source(0)) + source(1)
                }
                val portNameSink = outPortList(sink(1))
                val portNameSrc = if(source(0)== TYPE_Module){
                  moduleList(moduleInsts(source(1))).getInPortList()(source(2))
                }else{
                  getElePortName(source(0),false,source(2))
                }
                node += <export export_name={portNameSink} original={original} original_id="0" port={portNameSrc} type="out" />
              }
          }
        }
      }
    }
    <exports> {node} </exports>
  }

  def conXml(modName:String,connect :  List[Tuple2[List[Int],List[List[Int]]]], moduleList: List[ModuleInfo], moduleInsts: List[Int],
             inPortList: List[String], outPortList: List[String]):Elem = {
    val node = new NodeBuffer
    var conNum = 0
    connect.map{
      case(source , sinkList) =>{
        if(source(0) != TYPE_IO.id){
          sinkList.map { sink => {
            if(sink(0) != TYPE_IO.id) {

              val sorceName = if (source(0) == TYPE_Module.id) {
                moduleList(moduleInsts(source(1))).getType() + getSubModIndex(source(1), moduleInsts,moduleList)
              } else {
                eleTypeName(source(0)) + source(1)
              }

              val sinkName = if (sink(0) ==TYPE_Module.id){
                moduleList(moduleInsts(sink(1))).getType() + getSubModIndex(sink(1), moduleInsts,moduleList)
              }else{
                eleTypeName(sink(0)) + sink(1)
              }

              val portSourceName = if(source(0) == TYPE_Module.id){
                moduleList(moduleInsts(source(1))).getOutPortList()
                moduleList(moduleInsts(source(1))).getOutPortList()(source(2))
              }else{
                getElePortName(source(0),false,source(2))
              }

              val portSinkName = if(sink(0) == TYPE_Module.id){
                moduleList(moduleInsts(sink(1))).getInPortList()(sink(2))
              }else{
                getElePortName(sink(0),true,sink(2))
              }
              node += <connection name={"C_"+conNum} source={sorceName} source_id="0" source_port={portSourceName} target={sinkName} target_id="0" target_port={portSinkName} />
              conNum = conNum + 1

            }
          }
          }
        }else{
          sinkList.map{sink =>
            if(sink(0)==TYPE_IO.id){
              node += <connection name={"C_"+conNum} source={modName} source_id="0" source_port={inPortList(source(1))} target={modName} target_id="0" target_port={outPortList( sink(1))} />
            }
          }
        }
      }
    }
    <connections>
      {node}
    </connections>
  }


  def inXml(inPortList: List[String],width:Int): Elem = {
    <inputs>
      {(0 to inPortList.size - 1).map(i => <input name={inPortList(i)} width={width.toString} match_type=""/>)}<name_list value={inPortList.foldLeft("")((b, a) => b + a + ",")}/>
    </inputs>
  }

  def outXml(outPortList: List[String],width:Int): Elem = {
    <outputs>
      {(0 to outPortList.size - 1).map(i => <output name={outPortList(i)} width={width.toString} match_type=""/>)}<name_list value={outPortList.foldLeft("")((b, a) => b + a + ",")}/>
    </outputs>
  }

  def modXml(modInsName : String,top:Int,moduleInfo : ModuleInfo) : Elem={
    val modType = moduleInfo.getType()
    val modName = moduleInfo.getName()
    val subModList = moduleInfo.getModuleList()
    val subModInst = moduleInfo.getModuleInsts()
    val eleLists =  moduleInfo.getElementLists()
    val inPortList = moduleInfo.getInPortList()
    val outPortList = moduleInfo.getOutPortList()
    val connect = moduleInfo.getConnect()
    val width = moduleInfo.getWidth()

    <module name={modInsName} count="1" type={modType} device={modName} in_num={inPortList.size.toString} out_num={outPortList.size.toString} data_width={width.toString} has_reg="0" is_top={top.toString}>
      <modules>{subModInst.indices.map{index => {
        modXml(subModList(subModInst(index)).getType() + getSubModIndex(index, subModInst, subModList), 0, subModList(subModInst(index)))
      }
      }}</modules>
      {elemXml(eleLists)}
      {expXml(connect,subModList,subModInst,inPortList,outPortList)}
      {conXml(modInsName,connect,subModList,subModInst,inPortList,outPortList)}
      {inXml(inPortList,width)}
      {outXml(outPortList,width)}
    </module>
  }

  def dumpIR(moduleInfo : ModuleInfo, fileName : String, topName: String) :Unit={
    val file:FileWriter = new FileWriter( fileName , false)
    val xmlInfo =
      <geda_arch>
        {modXml(topName, 1,moduleInfo)}
      </geda_arch>
    val pp = new xml.PrettyPrinter(10000, 4)
    val xmlOut = pp formatNodes xmlInfo
    file.write(xmlOut)
    file.close
  }
}
