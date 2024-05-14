package CGRA.BITS

import CGRA.element.ALU.OPC
import CGRA.element.ALU.OPC._
import CGRA.element.ALU.OpInfo.String2IntMap
import CGRA.parameter.Param.accCounterW
import chisel3.util.log2Ceil

import java.io.{FileInputStream, FileWriter}
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex
import scala.collection.mutable.Map
object CfgBitS {
 def TOPNAME = "CGRA"
  def ParseRoute(filename : java.io.InputStream) :List[Tuple2[String,Int]] ={
    val retBuf = new ListBuffer[Tuple2[String,Int]] ()

    val lines = scala.io.Source.fromInputStream(filename, "utf-8").getLines().foldLeft("")((a,b) => a + b)
    val muxPattern = new Regex("\\s\\S+(MUX|MUXR)[0-9]*\\.in[0-9]*\\s")
    val muxList = (muxPattern findAllIn lines).toList
     muxList.map( i => {
      val MuxPattern = new Regex("(?<=\\s)\\S+(MUX|MUXR)[0-9]*(?=\\.in)")
      val MuxFullName = (MuxPattern findAllIn i).toList(0)

      val numPattern = new Regex("(?<=in)[0-9]+")
      val num = (numPattern findAllIn i).toList(0).toInt
       if (TOPNAME.r.findAllIn(MuxFullName).length == 1) {
         retBuf.append(MuxFullName -> num)
       }
    })







    val obPattern = new Regex("\\s\\S+OB[0-9]*\\.in[0-9]*\\s")
    val obList = (obPattern findAllIn lines).toList
    obList.map(i => {
      val obPattern = new Regex("(?<=\\s)\\S+OB[0-9]*(?=\\.in)")
      val obFullName = (obPattern findAllIn i).toList(0)

      val numPattern = new Regex("(?<=in)[0-9]+")
      val num = (numPattern findAllIn i).toList(0).toInt
      if (TOPNAME.r.findAllIn(obFullName).length == 1) {
        retBuf.append(obFullName -> num)
      }
    })
    retBuf.distinct.toList.sortBy( i => i._1)

  }

  def ParsePlace(filename: java.io.InputStream): List[Tuple2[String,Int]] = {
    val retBuf = new ListBuffer[Tuple2[String,Int]] ()
    val lines = scala.io.Source.fromInputStream(filename, "utf-8").getLines().toList
    lines.foreach(line => {
      //获取alu的配置信息
      val ALUPattern = new Regex("(?<=\\s)\\S+ALU[0-9]*$")
      val ALUFullName = (ALUPattern findAllIn line).toList
      if(!ALUFullName.isEmpty){
        val ACCCntPattern = new Regex("(?<=ACC[0-9]*_)[0-9]*(?=\\s|_)")
        val ACCInitPattern = new Regex("(?<=ACC[0-9]*_[0-9]*_)[0-9]*(?=\\s)")
        val ACCCnt = (ACCCntPattern findAllIn line).toList
        if(!ACCCnt.isEmpty){
          val ACCInit = (ACCInitPattern findAllIn line).toList
          if(ACCInit.isEmpty){
            retBuf.append(ALUFullName(0) ->( ACC.id + (ACCCnt(0).toInt<<log2Ceil(OPC.numOPC))))
          }else{
            retBuf.append(ALUFullName(0) ->( ACC.id + (ACCCnt(0).toInt<<log2Ceil(OPC.numOPC))  +  (ACCInit(0).toInt<<(log2Ceil(OPC.numOPC) + accCounterW))    ))
          }

        }else {
          var opFind = false
          String2IntMap.toList.map {
            case (opName, opCfg) => {
              if (line.contains(opName)) {
                retBuf.append(ALUFullName(0) -> opCfg)
                opFind = true
              }
            }
          }
          if (!opFind) {
            println("cant find the op " + line)
          }
        }
      }

      //获取const的配置信息
      val ConstPattern = new Regex("(?<=\\s)\\S+ConstUnit[0-9]*$")
      val ConstFullName = (ConstPattern findAllIn line).toList
      if(!ConstFullName.isEmpty){
//        println(line)
        val ConstValPattern = new Regex("(?<=CONST[0-9]*_)[-]?[0-9]*(?=\\s)")
        val ConstVal = (ConstValPattern findAllIn line).toList
        if(!ConstVal.isEmpty){
          retBuf.append(ConstFullName(0) -> ConstVal(0).toInt)
        }else{
          println("常量格式有问题" + line)
        }
      }

    })
    retBuf.distinct.toList.sortBy( i => i._1)
  }

  def GetCfgAddr(filename: java.io.InputStream): Tuple2[Map[String,Tuple2[Tuple2[Int,Int],Tuple2[Int,Int]]] ,Int]= {
    val lines = scala.io.Source.fromInputStream(filename, "utf-8").getLines().toList
    val addrMap : Map[String,Tuple2[Tuple2[Int,Int],Tuple2[Int,Int]]] = Map()
    val addrWList  = ((new Regex("[0-9]") findAllIn lines(0)).toList).map(i => i.toInt)
    val nameList:Array[String] = new Array[String](addrWList.size + 1)
    val addrList:Array[Int] = new Array[Int](addrWList.size )
    nameList(0) = "CGRA"
    lines.foreach( line => {
      val spacelist = (new Regex("#+") findAllIn line).toList
      if(!spacelist.isEmpty) {
        if (spacelist.head.count(i => i == '#') > 0) {
          val level = spacelist.head.count(i => i == '#')/4
          val name = (new Regex("(?<=#)[A-Z|a-z|0-9]+(?=\\s)") findAllIn line).toList(0)
          nameList(level) = name


          val intList = (new Regex("(?<=\\s)[0-9]+") findAllIn line).toList
          if(intList.size == 1){
            addrList(level - 1) = intList(0).toInt
          }else {
            addrList(level - 1) = intList(0).toInt
            var addrLow = 0
            var offLow = 0
            for( i <- 0 until addrWList.size){
              addrLow = (addrList(addrWList.size - 1 - i) << offLow)| addrLow
              offLow = addrWList(addrWList.size - 1 - i) + offLow
            }

            addrList(level - 1) = intList(2).toInt
            var addrHigh = 0
            var offHigh = 0
            for(i <- 0 until addrWList.size) {
              addrHigh = (addrList(addrWList.size - 1 - i) << offHigh) | addrHigh
              offHigh = addrWList(addrWList.size - 1 - i) + offHigh
            }
            val name =  nameList.foldLeft("")((a,b) =>  if(b != null) {
              a + b + "."
            }else {
              a
            })
            addrMap += (name.dropRight(1)-> ((addrLow->intList(1).toInt)->(addrHigh->intList(3).toInt)))
          }

        }
      }
    })
    val addrW = addrWList.foldLeft(0)((a,b) => a + b)

//    var mask = 0
//    for( i <- 0 until addrW){
//      mask = mask| (1<<i)
//    }

    (addrMap,addrW)

  }

  def CfgGen(addrFile : String,routeFile: String,placeFile : String ) :Unit ={
    val cfgMess: Map[Int,Int] = Map()
    val (addrMap , addrW) = GetCfgAddr(new FileInputStream(addrFile))
//    println("addrMap is " + addrMap)
//    var mask = 0
//    for (i <- 0 until addrW) {
//      mask = mask | (1 << i)
//    }


    val routeRes = ParseRoute(new FileInputStream(routeFile))
    val placeRes = ParsePlace(new FileInputStream(placeFile))



    def CellCfgGen(name:String, cfgData:Int) :Unit = {
      val addr = addrMap(name)
      val addrLow = addr._1
      val addrHigh = addr._2
      if (addrLow._1 == addrHigh._1) { //最低位和最高位是否配置同一个地址
        if (cfgMess.contains(addrLow._1)) { //是否已经有单元使用了该地址
          cfgMess(addrLow._1) = cfgMess(addrLow._1) | (cfgData << addrLow._2) //使用了两个相或
        } else {
          cfgMess += (addrLow._1 -> (cfgData << addrLow._2)) //未使用直接添加
        }
      } else { //高低位不在同一个地址
        val highShift = 32 - addrLow._2//pigfly 这个addrW一定是有问题的
        if (cfgMess.contains(addrLow._1)) {
          cfgMess(addrLow._1) = cfgMess(addrLow._1) | (cfgData << addrLow._2)
        } else {
          cfgMess += (addrLow._1 -> ((cfgData << addrLow._2) ))
        }
        if (cfgMess.contains(addrHigh._1)) {
          cfgMess(addrHigh._1) = cfgMess(addrHigh._1) | (cfgData >> highShift)
        } else {
          cfgMess += (addrHigh._1 ->  (cfgData >> highShift))
        }

      }

    }
    routeRes.map{case(name  ,cfgData) =>CellCfgGen(name  ,cfgData) }
    placeRes.map{case(name  ,cfgData) =>CellCfgGen(name  ,cfgData) }

    val cfgMessList = cfgMess.toList.sortBy(i => (i._1 , i._2) )

    val file: FileWriter = new FileWriter(java.nio.file.Paths.get(routeFile).getParent.toString+ "/" + addrFile.replace(".txt" , "Cfg.txt"), false)
//    println("come here")
    file.write("addr\tcfgData\n")
    cfgMessList.map( i => {
//      if(i._1 == 93){
//        println(i._1)
//        println(i._2)
//        println(i._1.toLong)
//        println(i._1.toLong << 32)
//        println("0x" + ((i._1.toLong << 32) | (i._2.toLong & 0x00000000ffffffff)).toHexString)
//      }
      file.write(i._1.toHexString +"\t" + i._2.toHexString + "\n")     })

    file.write("\nCfgMessage\n")
    cfgMessList.map(i => {
      if(i._2 >0) {
        file.write("{0x" + (i._1.toLong << 32 | i._2).toHexString + ",0,0,0},\n")
      }else if(i._2 <0){
        file.write("{0x" + i._1.toLong.toHexString + i._2.toHexString + ",0,0,0},\n")
      }
    } )
    file.close

    val ioFile: FileWriter = new FileWriter(java.nio.file.Paths.get(routeFile).getParent.toString+ "/" + addrFile.replace(".txt" , "IO.txt"), false)
    val lines = scala.io.Source.fromInputStream(new FileInputStream(placeFile), "utf-8").getLines().toList
//    val IOBlinePar = new Regex("(IB|OB)[0-9]*$")
    val IONum = new Regex("(?<=(IB|OB))[0-9]*$")
    lines.filter{line =>(! (IONum findAllIn line).toList.isEmpty ) }.sortBy{
      line =>(IONum findAllIn line).toList(0).toInt
    }.sortBy {
      line => line(0)
    }.foreach(line => ioFile.write(line + "\n"))
    ioFile.close
//    lines.foreach(line => {
//      val IBlinePar = new  Regex("IB[0-9]*$")
//      val OBlinePar = new  Regex("OB[0-9]*$")
//      if(! (IBlinePar findAllIn line).toList.isEmpty | ! (OBlinePar findAllIn line).toList.isEmpty){
//        ioFile.write(line + "\n")
//      }
//    })
//    ioFile.close
  }







}


object zhengzetest extends App {
   val bench = "pedometer"
  val arc = "FC16161281"



  val routePath ="./src/resource/benchmark/" + bench +"/"+bench + "_DFG.routed.txt"
  val placePath = "./src/resource/benchmark/" + bench +"/"+bench + "_DFG.placed.txt"
  val addrFilePath = "cgra" + arc + ".txt"
  CfgBitS.CfgGen(addrFilePath,routePath,placePath)

//  CfgBitS.CfgGen(".txt","./src/resource/benchmark/conv2d_3x3/conv2d_3x3_DFG.routed.txt","./src/resource/benchmark/conv2d_3x3/conv2d_3x3_DFG.placed.txt")
}