//package CGRA.yzArch
//
//import CGRA.IR.Info2Xml.dumpIR
//import CGRA.module.DumpInfo.infooutput
//import CGRA.module.ModuleInfo
//import CGRA.parameter.EleType._
//
//import scala.collection.mutable.{ArrayBuffer, Map}
//
//class connectinfo( type_ : String, name: String, inportlist: List[String],outportlist: List[String],
//                   connect : Map[List[Int] , List[List[Int]]],width: Int){
//
//  val conlist_G :Map[Int,  ArrayBuffer[Tuple2[List[List[Int]] , List[List[Int]]]] ] = Map()
//
//
//  def parase(merge_level: Int, level: Int, conlist: List[Tuple2[List[List[Int]], List[List[Int]]]], muxnum: Int, conlist_G: Map[Int, ArrayBuffer[Tuple2[List[List[Int]], List[List[Int]]]]] = this.conlist_G): Unit = {
//    require((Math.log(muxnum) / Math.log(2)) % 1 == 0)
//    if (merge_level == 0) {
//      for (i <- conlist.indices) {
//        val conlistlow = new ArrayBuffer[Tuple2[List[List[Int]], List[List[Int]]]]()
//        for (j <- i + 1 until conlist.size) {
//          if ((conlist(i)._1.toSet & conlist(j)._1.toSet).size >= muxnum) {
//            if (!conlist_G.contains(level + 1)) {
//              conlist_G += (level + 1 -> new ArrayBuffer[Tuple2[List[List[Int]], List[List[Int]]]])
//            }
//            conlist_G(level + 1).append((conlist(i)._1.toSet & conlist(j)._1.toSet).toList -> (conlist(i)._2.toSet | conlist(j)._2.toSet).toList)
//            conlistlow.append((conlist(i)._1.toSet & conlist(j)._1.toSet).toList -> (conlist(i)._2.toSet | conlist(j)._2.toSet).toList)
//          }
//        }
//        if (conlistlow.size > 1) {
//          parase(0, level + 1, conlistlow.toList, muxnum, conlist_G)
//        }
//      }
//    } else {
//      for (i <- conlist.indices) {
//        val conlistlow = new ArrayBuffer[Tuple2[List[List[Int]], List[List[Int]]]]()
//        for (j <- i + 1 until conlist.size) {
//          if ((conlist(i)._1.toSet & conlist(j)._1.toSet).size >= muxnum) {
//            if (!conlist_G.contains(level + 1)) {
//              conlist_G += (level + 1 -> new ArrayBuffer[Tuple2[List[List[Int]], List[List[Int]]]])
//            }
//            conlist_G(level + 1).append((conlist(i)._1.toSet & conlist(j)._1.toSet).toList -> (conlist(i)._2.toSet | conlist(j)._2.toSet).toList)
//            conlistlow.append((conlist(i)._1.toSet & conlist(j)._1.toSet).toList -> (conlist(i)._2.toSet | conlist(j)._2.toSet).toList)
//          }
//        }
//        if (conlistlow.size > 1 & level + 1 < merge_level) {
//          parase(merge_level, level + 1, conlistlow.toList, muxnum, conlist_G)
//        }
//      }
//    }
//  }
//
//
//
//
//
//  def devide_innum(): Map[List[Int], ArrayBuffer[ArrayBuffer[List[Int]]]] = {
//    val linkbuf: Map[List[Int], ArrayBuffer[ArrayBuffer[List[Int]]]] = Map()
//    connect.foreach {
//      case (sink, sourcelist) => {
//        linkbuf += (sink -> new ArrayBuffer[ArrayBuffer[List[Int]]])
//        val sourcemap: Map[Int, ArrayBuffer[List[Int]]] = Map()
//        sourcelist.foreach(ins => {
//          if (!sourcemap.contains(ins.last)) {
//            sourcemap += (ins.last -> new ArrayBuffer[List[Int]])
//          }
//          sourcemap(ins.last).append(ins.init)
//        })
//        val maxnum = sourcemap.keys.max
//        for (i <- 1 to maxnum) {
//          if (sourcemap.contains(i)) {
//            linkbuf(sink).append(sourcemap(i))
//          }
//        }
//      }
//    }
//    linkbuf
//  }
//
//  def product_mulin(mux_num: Int, merge_level: Int): ModuleInfo = {
//    val sink_sourcelist_innum = this.devide_innum()
//    val maxinnum = sink_sourcelist_innum.values.map(ins => ins.indices.last).toList.max
//    val muxbuf = new ArrayBuffer[List[Int]]()
//    val connectbuf: Map[List[Int], ArrayBuffer[List[Int]]] = Map()
//    val mux2sink = sink_sourcelist_innum.map { case (ins, buffer) => (ins -> new ArrayBuffer[List[Int]]()) }
//    for (i <- 0 to maxinnum) {
//      val conlistbuf: Map[Int, ArrayBuffer[Tuple2[List[List[Int]], List[List[Int]]]]] = Map()
//      conlistbuf += (1 -> new ArrayBuffer[Tuple2[List[List[Int]], List[List[Int]]]])
//
//      sink_sourcelist_innum.toList.foreach {
//        case (sink, sourcelist_innum) => {
//          if (sourcelist_innum.nonEmpty) {
//            conlistbuf(1).append(sourcelist_innum.foldLeft(new ArrayBuffer[List[Int]])((b, a) => b ++= a).toList -> List(sink))
//            sink_sourcelist_innum(sink) = sink_sourcelist_innum(sink).tail
//          }
//        }
//      }
//      val conlistbufsize = conlistbuf(1).size
//      if (merge_level != 1) {
//        parase(merge_level, 1, conlistbuf(1).toList, mux_num, conlistbuf)
//      }
//      var maxlevel = 1
//      conlistbuf.foreach(ins => if (ins._1 > maxlevel) {
//        maxlevel = ins._1
//      })
//      if (maxlevel > 1) {
//        for (i_ <- 2 to maxlevel) {
//          val i = maxlevel + 2 - i_
//          val conins = conlistbuf(i)
//          conins.foreach {
//            case (sourcelist, sinklist) => {
//              muxbuf.append(List(sourcelist.size, width))
//              sourcelist.foreach(ins => {
//                if (!connectbuf.contains(ins)) {
//                  connectbuf += (ins -> new ArrayBuffer[List[Int]]())
//                }
//                connectbuf(ins).append(List(TYPE_Multiplexer.id, muxbuf.indices.last, sourcelist.indexOf(ins)))
//              })
//              for (i2_ <- 1 to i - 1) {
//                val i2 = i - i2_
//                val conins2list = conlistbuf(i2).toList
//                conins2list.foreach {
//                  case (sourcelist2, sinklist2) => {
//                    if (((sinklist2.toSet & sinklist.toSet) == sinklist2.toSet) & (sourcelist2.toSet & sourcelist.toSet).size > 1) { //改了一下这个条件
//                      //                    if((sourcelist2.toSet&sourcelist.toSet) == sourcelist.toSet){
//                      conlistbuf(i2) -= (sourcelist2 -> sinklist2)
//                      if ((sourcelist2.toSet.size - sourcelist.toSet.size) > mux_num / 2 | i2 == 1) {
//                        conlistbuf(i2) += ((sourcelist2.toSet -- sourcelist.toSet + List(TYPE_Multiplexer.id, muxbuf.indices.last, 0)).toList -> sinklist2)
//                      }
//                    }
//                  }
//                }
//              }
//            }
//          }
//        }
//      }
//      conlistbuf(1).foreach {
//        case (sourcelist, sink) => {
//          if (sourcelist.size > 1) {
//            muxbuf.append(List(sourcelist.size, width))
//            sourcelist.foreach(source => {
//              if (!connectbuf.contains(source)) {
//                connectbuf += (source -> new ArrayBuffer[List[Int]]())
//              }
//              connectbuf(source).append(List(TYPE_Multiplexer.id, muxbuf.indices.last, sourcelist.indexOf(source)))
//            })
//            mux2sink(sink.head).append(List(TYPE_Multiplexer.id, muxbuf.indices.last, 0))
//          } else if (sourcelist.size == 1) {
//            mux2sink(sink.head).append(sourcelist.head)
//          } else {
//            println("there is something error when build mux2sink")
//          }
//        }
//      }
//    }
//    mux2sink.foreach {
//      case (sink, muxlist) => {
//        if (muxlist.size > 1) {
//          muxbuf.append(List(muxlist.size, width))
//          muxlist.foreach(muxout => {
//            if (!connectbuf.contains(muxout)) {
//              connectbuf += (muxout -> new ArrayBuffer[List[Int]]())
//            }
//            connectbuf(muxout).append(List(TYPE_Multiplexer.id, muxbuf.indices.last, muxlist.indexOf(muxout)))
//          }
//
//          )
//          connectbuf += (List(TYPE_Multiplexer.id, muxbuf.indices.last, 0) -> ArrayBuffer(sink))
//        } else if (muxlist.size == 1) {
//          if (connectbuf.contains(muxlist.head)) {
//            connectbuf(muxlist.head).append(sink)
//          } else {
//            connectbuf += (muxlist.head -> ArrayBuffer(sink))
//          }
//        } else {
//          println("there is something error when build connectbuf")
//        }
//      }
//    }
//    val module2list = connectbuf.map {
//      case (source, sinklist) => {
//        (source -> sinklist.toList)
//      }
//    }.toList
//    new ModuleInfo(type_, name, List(),List(),  Map(TYPE_Multiplexer.id -> muxbuf.toList), inportlist, outportlist, module2list, width)
//
//
//  }
//}
//
//class
//
//object conGenTest extends App {
//  val con = Map(List(8, 4) -> List(List(8, 25, 1), List(8, 8, 1), List(8, 2, 1), List(8, 12, 1), List(8, 5, 1), List(8, 1, 1), List(8, 17, 1)), List(8, 7) -> List(List(8, 17, 1), List(8, 18, 1), List(8, 15, 1), List(8, 11, 1), List(8, 10, 1), List(8, 26, 1), List(8, 5, 1), List(8, 22, 1)), List(8, 0) -> List(List(8, 14, 1), List(8, 6, 1), List(8, 2, 1), List(8, 4, 1), List(8, 26, 1), List(8, 22, 1)), List(8, 2) -> List(List(8, 15, 1), List(8, 7, 1), List(8, 3, 1), List(8, 20, 1)), List(8, 5) -> List(List(8, 16, 1), List(8, 17, 1), List(8, 10, 1), List(8, 11, 1), List(8, 14, 1), List(8, 24, 1), List(8, 3, 1), List(8, 23, 1)), List(8, 6) -> List(List(8, 24, 1), List(8, 9, 1), List(8, 4, 1), List(8, 3, 1)), List(8, 1) -> List(List(8, 23, 1), List(8, 12, 1), List(8, 18, 1), List(8, 19, 1), List(8, 27, 1), List(8, 0, 1), List(8, 3, 1), List(8, 1, 1)), List(8, 3) -> List(List(8, 21, 1), List(8, 13, 1), List(8, 19, 1), List(8, 18, 1), List(8, 25, 1), List(8, 1, 1)))
//  val inlist = (0 until 28).map{ in => "in"+in.toString}.toList
//  val outlist = (0 until 8).map{ in => "out"+in.toString}.toList
//  val conExample = new connectinfo("test" , "test" , List("in0"), List("out0") ,
//    Map(
//      List(8,0) ->
//        List(
//          List(8,0,1)
//        )
//    ),32)
//
////  infooutput("congen.scala" , conExample)
//  dumpIR(conExample.product_mulin(4,0),"congen.xml" ,"congen")
//}