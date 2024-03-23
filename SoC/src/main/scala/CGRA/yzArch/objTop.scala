//package CGRA.yzArch
//import CGRA.ADL.ADL.{EleTrace, ModuleTrace}
//import CGRA.ARCH.moduleIns.CGRAParam.PEParam
//import CGRA.ARCH.moduleIns.PE
//import CGRA.IR.Info2Xml.dumpIR
//import CGRA.element.ALU.OPC._
//import CGRA.module.DumpInfo.infooutput
//import CGRA.module.ModuleInfo
//import CGRA.module.TopModule.topGen
//import CGRA.parameter.EleType._
//import CGRA.yzArch.objTop.ArcTop
//import Replace.ReplaceModule
//object objTop  {
//  class ArcTop(col: Int, opList: List[Int], inList: List[Tuple3[Int, Int, Int]], outList: List[Tuple2[Int, Int]], matinxIn: List[List[Tuple2[Int, Int]]])
//    extends ModuleTrace("yzArch") {
//    this.typeStr = "Top"
//    this.deviceStr = "Top"
//    this.width = 32
//
//    this.inPorts = (0 until inList.size).map("in" + _.toString).toList
//
//    this.outPorts = (0 until outList.size).map("out" + _.toString).toList
//    //每一行的PE算子类型， 0 表示加， 1表示乘，其他表示有加有乘（0：加，1乘，2减，3加乘、4加减、5乘减、6加乘减）
//    for (i <- 0 until opList.size) {
////      val name = if (opList(i) == 0) "PEADD" else if (opList(i) == 1) "PEMUL" else "PEADDMUL"
////      val peOp = if (opList(i) == 0) List(ADD) else if (opList(i) == 1) List(MUL) else List(ADD, MUL)
//      val name = opList(i) match {
//        case 0 => "PEADD"
//        case 1 => "PEMUL"
//        case 2 => "PESUB"
//        case 3 => "PEADDMUL"
//        case 4 => "PEADDSUB"
//        case 5 => "PEMULSUB"
//        case 6 => "PEADDMULSUB"
//        case _ => "PEALL"
//      }
//      val peOp = opList(i) match {
//        case 0 => List(ADD)
//        case 1 => List(MUL)
//        case 2 => List(SUB)
//        case 3 => List(ADD, MUL)
//        case 4 => List(ADD, SUB)
//        case 5 => List(MUL, SUB)
//        case 6 => List(ADD, MUL ,SUB)
//        case _ => List(ADD , SUB , MUL, AND,OR,XOR,PASS)
//
//      }
//      for (j <- 0 until col) {
//        val peParam = PEParam(peOp, width, name)
//        addModule(PE(peParam, "PE" + i.toString + "_" + j.toString))
//      }
//    }
//
//
//    for (i <- 0 until opList.size) {
//      for (j <- 0 to 1) {
//        val moduleT = new ModuleTrace("Matrix" + i.toString + "_" + j.toString, "Matrix", "Matrix" + i.toString
//          , width, (0 until matinxIn(i).size).map { i => "in" + i.toString }.toList, (0 until col).map { i => "out" + i.toString }.toList)
//        println("modulename is " + "Matrix" + i.toString + "_" + j.toString)
//        addModule(moduleT)
//      }
//    }
//
//
//    for (i <- 0 until opList.size) {
//      for (j <- 0 until col) {
//        for (k <- 0 to 1) {
//          if (inList.contains(i, j, k)) {
//            val nuxIns = new EleTrace("Mux" + i.toString + "_" + j.toString + "_" + k.toString, TYPE_Multiplexer.id, List("in0", "in1"), List("out0"), List(2, width))
//            addEle(nuxIns)
//            val inNum = inList.indexOf((i, j, k))
//            addConnect(("this", "in" + inNum.toString), ("Mux" + i.toString + "_" + j.toString + "_" + k.toString, "in0"))
//            addConnect(("Matrix" + i.toString + "_" + k.toString, "out" + j.toString), ("Mux" + i.toString + "_" + j.toString + "_" + k.toString, "in1"))
//            addConnect(("Mux" + i.toString + "_" + j.toString + "_" + k.toString, "out0"), ("PE" + i.toString + "_" + j.toString, "in" + k.toString))
//          } else {
//            addConnect(("Matrix" + i.toString + "_" + k.toString, "out" + j.toString), ("PE" + i.toString + "_" + j.toString, "in" + k.toString))
//          }
//        }
//      }
//    }
//
//    for (p <- 0 until matinxIn.size) {
//      for (q <- 0 until matinxIn(p).size) {
//        addConnect(("PE" + matinxIn(p)(q)._1.toString + "_" + matinxIn(p)(q)._2.toString, "out0"), ("Matrix" + p.toString + "_" + 0.toString, "in" + q.toString))
//        addConnect(("PE" + matinxIn(p)(q)._1.toString + "_" + matinxIn(p)(q)._2.toString, "out0"), ("Matrix" + p.toString + "_" + 1.toString, "in" + q.toString))
//      }
//    }
//
//
//    for (i <- 0 until outList.size) {
//      addConnect(("PE" + outList(i)._1.toString + "_" + outList(i)._2.toString, "out0"), ("this", "out" + i.toString))
//    }
//
//
//  }
//
//
//  def TopYZGen(
//                col: Int, //每一行的PE数量
//                opList: List[Int],//每一行的PE算子类型， 0 表示加， 1表示乘，其他表示有加有乘（0：加，1乘，2减，3加乘、4加减、5乘减、6加乘减）
//                inList: List[Tuple3[Int, Int, Int]],//输入连接到那里，三个参数分别表示行、列、以及第几个端口
//                outList: List[Tuple2[Int, Int]],//输出来自那个PE，分别表示行、列
//                matinxIn: List[List[Tuple2[Int, Int]]],//每一行matrix的外部结构。
//              //matinxIn(0)表示第一行的matrix的输入来自那里，matinxIn(1)表示第一行，以此类推
//              //每一行的matrix数量由PE的输入数量决定，由于所有PE的输入端口数量都是2，所以每一行matrix数量为2
//              //每个matrix的输出数量由该行PE的数量决定，也就是col的值，输出一次连接到PE的输入端口
//              //每个Matrix的输入由List[Tuple2[Int, Int]]决定，每个Tuple2[Int, Int]表示输入端口连接的PE的行数和列数
//              //如List（（0，0），（1，1））表示该matrix的第0个输入来自第0行第0列的PE，第一个输入来自第1行第一列的PE
//                matrixInner : List[scala.collection.mutable.Map[List[Int] , List[List[Int]]]],//每一行matrix内部的连接关系
//              //matrixInner（0），即Map[List[Int] , List[List[Int]]， 表示第0行matrix内部的连接关系，以此类推。
//              //Map中的key为一个Sink，表示终点端口。为List(TYPE_IO.id , x),其中x表示第几个输出端口，TYPE_IO.id = 8.如List（TYPE_IO.id ， 0），表示第0个是输出端口
//              //Map中的value表示这个输入端口连接的所有输入端口，一个输入端口可以连接多次输出端口。其结构为List(TYPE_IO.id , x,y)其中x表示第几个输出端口，y表示连接了几次
//              //举个例子
//
////                List(
////                 scala.collection.mutable.Map(
////                  List(8, 0) -> List(List(8, 0, 1))
////                 )
////                )
//              //表示第0行的matirx，第0个输出端口可以来自第0个输入端口，且连接数量为1次
//                muxNum : Int //使用的mux的输入端口数量
//              ):ModuleInfo  ={
//    var moduleInit = new ArcTop(col,opList,inList,outList,matinxIn).getModuleInfo()
//    for ( i <- matrixInner.indices){
////      val matrix = moduleInit.getModuleList()(i)
//      val matrixInfo = new  connectinfo("Matrix" ,"Matrix" + i.toString,
//        (0 until matinxIn(i).size).map { i => "in" + i.toString }.toList, (0 until col).map { i => "out" + i.toString}.toList,
//        matrixInner(i),moduleInit.getWidth())
//      val matrixNew = matrixInfo.product_mulin(muxNum,0)
//      moduleInit = ReplaceModule(moduleInit , "Matrix" + i.toString,matrixNew)
//    }
//    moduleInit
//  }
//
//}
//
//
//object yazhouGen extends App {
//  val arcNew = objTop.TopYZGen(
//    1,
//    List(7),
//    List((0,0,0)),
//    List((0,0)),
//    List(
//      List((0,0))
//    ),
//    List(
//      scala.collection.mutable.Map(
//        List(8,0) ->List(List(8,0,1))
//      )
//    ),
//    2
//  )
//  dumpIR(arcNew, "arcNew.xml","arcNew")
////    //  val pe = new PE(PEParam(List(OPC.ADD) , 32,"PETest"),"PE")
////
////    val arcYZ = new ArcTop(1 , List(0), List((0,0,0)) , List((0,0)),List(
////      List((0,0))
////    ))
////  infooutput("arcNew.scala" , arcNew)
//    chisel3.Driver.execute(args,() => topGen(arcNew, "Test.txt") )
////  //  infooutput("arcYZ.scala", arcYZ.getModuleInfo())1
////    dumpIR(arcYZ.getModuleInfo(),"arcYZ.xml","arcYZ")
////  //  chisel3.Driver.execute(args,() => topGen(PE(PEParam(List(OPC.ADD) , 32,"PETest"),"PE").getModuleInfo(), "PETest.txt") )//生成verilog
//}
