package CGRA.ARCH.moduleIns

import CGRA.ADL.ADL.{EleTrace, ModuleTrace}
import CGRAParam.GIBParam
import GIBHelp.getGIBConnect
import CGRA.IR.Info2Xml.dumpIR
import CGRA.module.DumpInfo.infooutput
import CGRA.module.TopModule.topGen
import CGRA.parameter.DirParam._
import CGRA.parameter.EleType._

import scala.collection.mutable.ListBuffer


object GIBHelp {

  def Mod(x: Int, y: Int): Int = java.lang.Math.floorMod(x, y)
  // connection rule: Map[(src, dst), (src_idx, N) => dst_idx]
  val rule: Map[(Int, Int), Seq[Int] => Int] = Map(
    (WEST, EAST)   -> ((x: Seq[Int]) => Mod(x(0), x(1))),    // i%N
    (EAST, WEST)   -> ((x: Seq[Int]) => Mod(x(0), x(1))),
    (NORTH, SOUTH) -> ((x: Seq[Int]) => Mod(x(0), x(1))),
    (SOUTH, NORTH) -> ((x: Seq[Int]) => Mod(x(0), x(1))),
    (WEST, NORTH)  -> ((x: Seq[Int]) => Mod(-x(0), x(1))),   //(N-i)%N
    (NORTH, WEST)  -> ((x: Seq[Int]) => Mod(-x(0), x(1))),
    (EAST, SOUTH)  -> ((x: Seq[Int]) => Mod(-x(0)-2, x(1))), // (2*N-2-i)%N
    (SOUTH, EAST)  -> ((x: Seq[Int]) => Mod(-x(0)-2, x(1))),
    (NORTH, EAST)  -> ((x: Seq[Int]) => Mod(x(0)+1, x(1))),  // (i+1)%N
    (EAST, NORTH)  -> ((x: Seq[Int]) => Mod(x(0)-1, x(1))),  // (N+i-1)%N
    (SOUTH, WEST)  -> ((x: Seq[Int]) => Mod(x(0)+1, x(1))),
    (WEST, SOUTH)  -> ((x: Seq[Int]) => Mod(x(0)-1, x(1))),
    (NORTHWEST, EAST)  -> ((x: Seq[Int]) => Mod(x(0), x(1))),
    (NORTHWEST, NORTH) -> ((x: Seq[Int]) => Mod(-x(0), x(1))),
    (NORTHWEST, SOUTH) -> ((x: Seq[Int]) => Mod(x(0)-1, x(1))),
    (SOUTHWEST, NORTH) -> ((x: Seq[Int]) => Mod(x(0), x(1))),
    (SOUTHWEST, WEST)  -> ((x: Seq[Int]) => Mod(x(0)+1, x(1))),
    (SOUTHWEST, EAST)  -> ((x: Seq[Int]) => Mod(-x(0)-2, x(1))),
    (SOUTHEAST, WEST)  -> ((x: Seq[Int]) => Mod(x(0), x(1))),
    (SOUTHEAST, SOUTH) -> ((x: Seq[Int]) => Mod(-x(0)-2, x(1))),
    (SOUTHEAST, NORTH) -> ((x: Seq[Int]) => Mod(x(0)-1, x(1))),
    (NORTHEAST, SOUTH) -> ((x: Seq[Int]) => Mod(x(0), x(1))),
    (NORTHEAST, EAST)  -> ((x: Seq[Int]) => Mod(x(0)+1, x(1))),
    (NORTHEAST, WEST)  -> ((x: Seq[Int]) => Mod(-x(0), x(1))),
    (NORTHWEST, SOUTHEAST) -> ((x: Seq[Int]) => Mod(x(0), x(1))),    // i%N
    (SOUTHEAST, NORTHWEST) -> ((x: Seq[Int]) => Mod(x(0), x(1))),
    (NORTHEAST, SOUTHWEST) -> ((x: Seq[Int]) => Mod(x(0), x(1))),
    (SOUTHWEST, NORTHEAST) -> ((x: Seq[Int]) => Mod(x(0), x(1))),
    (NORTHWEST, NORTHEAST) -> ((x: Seq[Int]) => Mod(-x(0), x(1))),   //(N-i)%N
    (NORTHEAST, NORTHWEST) -> ((x: Seq[Int]) => Mod(-x(0), x(1))),
    (SOUTHEAST, SOUTHWEST) -> ((x: Seq[Int]) => Mod(-x(0)-2, x(1))), // (2*N-2-i)%N
    (SOUTHWEST, SOUTHEAST) -> ((x: Seq[Int]) => Mod(-x(0)-2, x(1))),
    (NORTHEAST, SOUTHEAST) -> ((x: Seq[Int]) => Mod(x(0)+1, x(1))),  // (i+1)%N
    (SOUTHEAST, NORTHEAST) -> ((x: Seq[Int]) => Mod(x(0)-1, x(1))),  // (N+i-1)%N
    (SOUTHWEST, NORTHWEST) -> ((x: Seq[Int]) => Mod(x(0)+1, x(1))),
    (NORTHWEST, SOUTHWEST) -> ((x: Seq[Int]) => Mod(x(0)-1, x(1)))
  )

  def getTrack2TrackConnect(numTrack: Int): ListBuffer[Seq[Int]] = {
    val w = numTrack
    var connect = ListBuffer[Seq[Int]]() // (srcSide, srcTrack, dstSide, dstTrack)
    for(i <- 0 until w){
      connect += Seq(WEST, i, EAST, rule((WEST,EAST))(Seq(i, w)))
      connect += Seq(NORTH, i, SOUTH, rule((NORTH,SOUTH))(Seq(i, w)))
      connect += Seq(WEST, i, SOUTH, rule((WEST,SOUTH))(Seq(i, w)))
      connect += Seq(SOUTH, i, EAST, rule((SOUTH,EAST))(Seq(i, w)))
      connect += Seq(EAST, i, NORTH, rule((EAST,NORTH))(Seq(i, w)))
      connect += Seq(NORTH, i, WEST, rule((NORTH,WEST))(Seq(i, w)))
      connect += Seq(EAST, rule((WEST,EAST))(Seq(i, w)), WEST, i)
      connect += Seq(SOUTH, rule((NORTH,SOUTH))(Seq(i, w)), NORTH, i)
      connect += Seq(SOUTH, rule((WEST,SOUTH))(Seq(i, w)), WEST, i)
      connect += Seq(EAST, rule((SOUTH,EAST))(Seq(i, w)), SOUTH, i)
      connect += Seq(NORTH, rule((EAST,NORTH))(Seq(i, w)), EAST, i)
      connect += Seq(WEST, rule((NORTH,WEST))(Seq(i, w)), NORTH, i)
    }
    connect
  }

  def getOPin2TrackConnect(numTrack: Int, fc: Int, numPinList: List[Int]): ListBuffer[Seq[Int]] = {
    // numPinList
    val numNWpin = numPinList(0)  // NORTHWEST side
    val numNEpin = numPinList(1)  // NORTHEAST side
    val numSEpin = numPinList(2)  // SOUTHEAST side
    val numSWpin = numPinList(3)  // SOUTHWEST side

    val w = numTrack
    val fcStep = 2*w.toFloat/fc
    var connect = ListBuffer[Seq[Int]]() // (srcSide, srcTrack, dstSide, dstTrack)
    var pinStep = {
      if(numNWpin > 0) fcStep/numNWpin else 0.0
    }
    for(i <- 0 until numNWpin){
      for(j <- 0 until fc/2) { // connect the opposite and right sides
        val index = (i*pinStep + j*fcStep).toInt
        connect += Seq(NORTHWEST, i, EAST, rule((NORTHWEST,EAST))(Seq(index, w)))
        connect += Seq(NORTHWEST, i, SOUTH, rule((NORTHWEST,SOUTH))(Seq(index, w)))
      }
    }
    if(numNEpin > 0){
      pinStep = fcStep/numNEpin
    }
    for(i <- 0 until numNEpin){
      for(j <- 0 until fc/2) { // connect the opposite and right sides
        val index = (i*pinStep + j*fcStep).toInt
        connect += Seq(NORTHEAST, i, SOUTH, rule((NORTHEAST,SOUTH))(Seq(index, w)))
        connect += Seq(NORTHEAST, i, WEST, rule((NORTHEAST,WEST))(Seq(index, w)))
      }
    }
    if(numSEpin > 0){
      pinStep = fcStep/numSEpin
    }
    for(i <- 0 until numSEpin){
      for(j <- 0 until fc/2) { // connect the opposite and right sides
        val index = (i*pinStep + j*fcStep).toInt
        connect += Seq(SOUTHEAST, i, WEST, rule((SOUTHEAST,WEST))(Seq(index, w)))
        connect += Seq(SOUTHEAST, i, NORTH, rule((SOUTHEAST,NORTH))(Seq(index, w)))
      }
    }
    if(numSWpin > 0){
      pinStep = fcStep/numSWpin
    }
    for(i <- 0 until numSWpin){
      for(j <- 0 until fc/2) { // connect the opposite and right sides
        val index = (i*pinStep + j*fcStep).toInt
        connect += Seq(SOUTHWEST, i, NORTH, rule((SOUTHWEST,NORTH))(Seq(index, w)))
        connect += Seq(SOUTHWEST, i, EAST, rule((SOUTHWEST,EAST))(Seq(index, w)))
      }
    }
    connect
  }

  def getTrack2IPinConnect(numTrack: Int, fc: Int, numPinList: List[Int]): ListBuffer[Seq[Int]] = {
    // numPinList
    val numNWpin = numPinList(0)  // NORTHWEST side
    val numNEpin = numPinList(1)  // NORTHEAST side
    val numSEpin = numPinList(2)  // SOUTHEAST side
    val numSWpin = numPinList(3)  // SOUTHWEST side

    val w = numTrack
    val fcStep = 2*w.toFloat/fc
    var connect = ListBuffer[Seq[Int]]() // (srcSide, srcTrack, dstSide, dstTrack)
    var pinStep = {
      if(numNWpin > 0) fcStep/numNWpin else 0.0
    }
    for(i <- 0 until numNWpin){
      for(j <- 0 until fc/2) { // connect the opposite and right sides
        val index = (i*pinStep + j*fcStep).toInt
        connect += Seq(EAST, rule((NORTHWEST,EAST))(Seq(index, w)), NORTHWEST, i)
        connect += Seq(SOUTH, rule((NORTHWEST,SOUTH))(Seq(index, w)), NORTHWEST, i)
      }
    }
    if(numNEpin > 0){
      pinStep = fcStep/numNEpin
    }
    for(i <- 0 until numNEpin){
      for(j <- 0 until fc/2) { // connect the opposite and right sides
        val index = (i*pinStep + j*fcStep).toInt
        connect += Seq(SOUTH, rule((NORTHEAST,SOUTH))(Seq(index, w)), NORTHEAST, i)
        connect += Seq(WEST, rule((NORTHEAST,WEST))(Seq(index, w)), NORTHEAST, i)
      }
    }
    if(numSEpin > 0){
      pinStep = fcStep/numSEpin
    }
    for(i <- 0 until numSEpin){
      for(j <- 0 until fc/2) { // connect the opposite and right sides
        val index = (i*pinStep + j*fcStep).toInt
        connect += Seq(WEST, rule((SOUTHEAST,WEST))(Seq(index, w)), SOUTHEAST, i)
        connect += Seq(NORTH, rule((SOUTHEAST,NORTH))(Seq(index, w)), SOUTHEAST, i)
      }
    }
    if(numSWpin > 0){
      pinStep = fcStep/numSWpin
    }
    for(i <- 0 until numSWpin){
      for(j <- 0 until fc/2) { // connect the opposite and right sides
        val index = (i*pinStep + j*fcStep).toInt
        connect += Seq(NORTH, rule((SOUTHWEST,NORTH))(Seq(index, w)), SOUTHWEST, i)
        connect += Seq(EAST, rule((SOUTHWEST,EAST))(Seq(index, w)), SOUTHWEST, i)
      }
    }
    connect
  }

  def getOPin2IPinConnect(fc: Int, diag: Boolean, numIOPinList: List[Int]): ListBuffer[Seq[Int]] = {
    // numIOPinList
    val nNWi = numIOPinList(0)  // number of the PE input pins on the NORTHWEST side of the GIB
    val nNWo = numIOPinList(1)  // number of the PE output pins on the NORTHWEST side of the GIB
    val nNEi = numIOPinList(2)  // number of the PE input pins on the NORTHEAST side of the GIB
    val nNEo = numIOPinList(3)  // number of the PE output pins on the NORTHEAST side of the GIB
    val nSEi = numIOPinList(4)  // number of the PE input pins on the SOUTHEAST side of the GIB
    val nSEo = numIOPinList(5)  // number of the PE output pins on the SOUTHEAST side of the GIB
    val nSWi = numIOPinList(6)  // number of the PE input pins on the SOUTHWEST side of the GIB
    val nSWo = numIOPinList(7)  // number of the PE output pins on the SOUTHWEST side of the GIB
    val fcPerSide = { if(diag) fc/2 else fc }
    var connect = ListBuffer[Seq[Int]]() // (srcSide, srcTrack, dstSide, dstTrack)
    val fcNWstep = nNWi/fcPerSide.toFloat
    val fcNEstep = nNEi/fcPerSide.toFloat
    val fcSEstep = nSEi/fcPerSide.toFloat
    val fcSWstep = nSWi/fcPerSide.toFloat
    for(i <- 0 until nNWo){
      for(j <- 0 until fcPerSide) { // connect to the other sides
        if(nSEi > 0 && diag){
          val iSE = (i*fcSEstep/nNWo + j*fcSEstep).toInt
          connect += Seq(NORTHWEST, i, SOUTHEAST, rule((NORTHWEST,SOUTHEAST))(Seq(iSE, nSEi)))
        }
        //        if(nSWi > 0){
        //          val iSW = (i*fcSWstep/nNWo + j*fcSWstep).toInt
        //          connect += Seq(NORTHWEST, i, SOUTHWEST, rule((NORTHWEST,SOUTHWEST))(Seq(iSW, nSWi)))
        //        }
        if(nNEi > 0){
          val iNE = (i*fcNEstep/nNWo + j*fcNEstep).toInt
          connect += Seq(NORTHWEST, i, NORTHEAST, rule((NORTHWEST,NORTHEAST))(Seq(iNE, nNEi)))
        }
      }
    }
    for(i <- 0 until nNEo){
      for(j <- 0 until fcPerSide) { // connect to the other sides
        if(nSEi > 0){
          val iSE = (i*fcSEstep/nNEo + j*fcSEstep).toInt
          connect += Seq(NORTHEAST, i, SOUTHEAST, rule((NORTHEAST,SOUTHEAST))(Seq(iSE, nSEi)))
        }
        if(nSWi > 0  && diag){
          val iSW = (i*fcSWstep/nNEo + j*fcSWstep).toInt
          connect += Seq(NORTHEAST, i, SOUTHWEST, rule((NORTHEAST,SOUTHWEST))(Seq(iSW, nSWi)))
        }
        //        if(nNWi > 0){
        //          val iNW = (i*fcNWstep/nNEo + j*fcNWstep).toInt
        //          connect += Seq(NORTHEAST, i, NORTHWEST, rule((NORTHEAST,NORTHWEST))(Seq(iNW, nNWi)))
        //        }
      }
    }
    for(i <- 0 until nSEo){
      for(j <- 0 until fcPerSide) { // connect to the other sides
        if(nSWi > 0){
          val iSW = (i*fcSWstep/nSEo + j*fcSWstep).toInt
          connect += Seq(SOUTHEAST, i, SOUTHWEST, rule((SOUTHEAST,SOUTHWEST))(Seq(iSW, nSWi)))
        }
        if(nNWi > 0  && diag){
          val iNW = (i*fcNWstep/nSEo + j*fcNWstep).toInt
          connect += Seq(SOUTHEAST, i, NORTHWEST, rule((SOUTHEAST,NORTHWEST))(Seq(iNW, nNWi)))
        }
        //        if(nNEi > 0){
        //          val iNE = (i*fcNEstep/nSEo + j*fcNEstep).toInt
        //          connect += Seq(SOUTHEAST, i, NORTHEAST, rule((SOUTHEAST,NORTHEAST))(Seq(iNE, nNEi)))
        //        }
      }
    }
    for(i <- 0 until nSWo){
      for(j <- 0 until fcPerSide) { // connect to the other sides
        //        if(nSEi > 0){
        //          val iSE = (i*fcSEstep/nSWo + j*fcSEstep).toInt
        //          connect += Seq(SOUTHWEST, i, SOUTHEAST, rule((SOUTHWEST,SOUTHEAST))(Seq(iSE, nSEi)))
        //        }
        if(nNWi > 0){
          val iNW = (i*fcNWstep/nSWo + j*fcNWstep).toInt
          connect += Seq(SOUTHWEST, i, NORTHWEST, rule((SOUTHWEST,NORTHWEST))(Seq(iNW, nNWi)))
        }
        if(nNEi > 0  && diag){
          val iNE = (i*fcNEstep/nSWo + j*fcNEstep).toInt
          connect += Seq(SOUTHWEST, i, NORTHEAST, rule((SOUTHWEST,NORTHEAST))(Seq(iNE, nNEi)))
        }
      }
    }
    connect
  }

  def getGIBConnect(inTrackDir : List[Int], outTrackDir : List[Int],numTrack: Int, diagPinConect: Boolean, numIOPinList: List[Int], fcList: List[Int]): ListBuffer[(Seq[Int], ListBuffer[Seq[Int]])] = {
    var dstList = ListBuffer[Seq[Int]]()
    var srcsList = ListBuffer[ListBuffer[Seq[Int]]]() // srcs connected to each dst
    val oPin2IPinConnect = getOPin2IPinConnect(fcList(2), diagPinConect, numIOPinList)
    oPin2IPinConnect.map{ dWire =>
      val dst = Seq(TYPE_IPIN, dWire(2), dWire(3)) // type, direction, index
      if(!dstList.contains(dst)){
        dstList += dst
      }
      val index = dstList.indexOf(dst)
      if(srcsList.size <= index){
        srcsList += ListBuffer(Seq(TYPE_OPIN, dWire(0), dWire(1)))
      } else{
        srcsList(index) += Seq(TYPE_OPIN, dWire(0), dWire(1))
      }
    }
    if(numTrack > 0) {
      val track2IPinConnect = getTrack2IPinConnect(numTrack, fcList(0), List(numIOPinList(0), numIOPinList(2), numIOPinList(4), numIOPinList(6))).distinct.filter(ins =>
        inTrackDir.contains(ins(0))
      )
      track2IPinConnect.map { dWire =>
        val dst = Seq(TYPE_IPIN, dWire(2), dWire(3)) // type, direction, index
        if (!dstList.contains(dst)) {
          dstList += dst
        }
        val index = dstList.indexOf(dst)
        if (srcsList.size <= index) {
          srcsList += ListBuffer(Seq(TYPE_ITRACK, dWire(0), dWire(1)))
        } else {
          srcsList(index) += Seq(TYPE_ITRACK, dWire(0), dWire(1))
        }
      }
      val oPin2TrackConnect = getOPin2TrackConnect(numTrack, fcList(1), List(numIOPinList(1), numIOPinList(3), numIOPinList(5), numIOPinList(7))).distinct.filter(ins =>
        outTrackDir.contains(ins(2))
      )
      oPin2TrackConnect.map { dWire =>
        val dst = Seq(TYPE_OTRACK, dWire(2), dWire(3)) // type, direction, index
        if (!dstList.contains(dst)) {
          dstList += dst
        }
        val index = dstList.indexOf(dst)
        if (srcsList.size <= index) {
          srcsList += ListBuffer(Seq(TYPE_OPIN, dWire(0), dWire(1)))
        } else {
          srcsList(index) += Seq(TYPE_OPIN, dWire(0), dWire(1))
        }
      }
      val track2TrackConnect = getTrack2TrackConnect(numTrack).distinct.filter(ins =>
        inTrackDir.contains(ins(0)) && outTrackDir.contains(ins(2))
      )
      track2TrackConnect.map { dWire =>
        val dst = Seq(TYPE_OTRACK, dWire(2), dWire(3)) // type, direction, index
        if (!dstList.contains(dst)) {
          dstList += dst
        }
        val index = dstList.indexOf(dst)
        if (srcsList.size <= index) {
          srcsList += ListBuffer(Seq(TYPE_ITRACK, dWire(0), dWire(1)))
        } else {
          srcsList(index) += Seq(TYPE_ITRACK, dWire(0), dWire(1))
        }
      }
    }

    // remove duplicates and sort by side and index
    srcsList = srcsList.map{ x => x.distinct.sortBy(dst => (dst(0), dst(1), dst(2))) }
    dstList.zip(srcsList).sortBy{case (dst, srcs) => (dst(0), dst(1), dst(2)) } // return ListBuffer(dst, srcs)
  }


}

class GIB  (gibParam :GIBParam ,name :String)extends ModuleTrace(name ){
//  gibParam.printGIB()
  this.typeStr = "GIB"
  this.deviceStr = gibParam.devicename
  this.width = gibParam.width


  val nNWi = gibParam.nNWi
  val nNWo = gibParam.nNWo
  val nNEi = gibParam.nNEi
  val nNEo = gibParam.nNEo
  val nSEi = gibParam.nSEi
  val nSEo = gibParam.nSEo
  val nSWi = gibParam.nSWi
  val nSWo = gibParam.nSWo
  val numIOPinList = List(nNWi, nNWo, nNEi, nNEo, nSEi, nSEo, nSWi, nSWo)

  val numTrack = gibParam.numTrack
  val diagPinConect = gibParam.diagPinConect
  val fcList = gibParam.fcList

 val inBuf = new ListBuffer[String]()
  (0 until nNWo).map { i =>inBuf.append( "opinNW" + i.toString) }
  (0 until nNEo).map { i => inBuf.append( "opinNE" + i.toString) }
  (0 until nSEo).map { i => inBuf.append("opinSE" + i.toString) }
  (0 until nSWo).map { i => inBuf.append("opinSW" + i.toString) }
  if(gibParam.inTrickDir.contains(WEST)){
    (0 until numTrack).map{ i => inBuf.append("itrackW" + i.toString)}
  }
  if (gibParam.inTrickDir.contains(NORTH)) {
    (0 until numTrack).map{i => inBuf.append("itrackN" +i.toString)}
  }
  if (gibParam.inTrickDir.contains(EAST)) {
    (0 until numTrack).map { i => inBuf.append("itrackE" + i.toString) }
  }
  if (gibParam.inTrickDir.contains(SOUTH)) {
    (0 until numTrack).map { i => inBuf.append("itrackS" + i.toString) }
  }

  this.inPorts = inBuf.toList

  val outBuf = new ListBuffer[String]()
  (0 until nNWi).map { i =>outBuf.append( "ipinNW" + i.toString) }
  (0 until nNEi).map { i =>outBuf.append( "ipinNE" + i.toString) }
  (0 until nSEi).map { i =>outBuf.append( "ipinSE" + i.toString) }
  (0 until nSWi).map { i =>outBuf.append( "ipinSW" + i.toString) }

  if (gibParam.outTrickDir.contains(WEST)) {
    (0 until numTrack).map { i => outBuf.append("otrackW" + i.toString) }
  }
  if (gibParam.outTrickDir.contains(NORTH)) {
    (0 until numTrack).map { i => outBuf.append("otrackN" + i.toString) }
  }
  if (gibParam.outTrickDir.contains(EAST)) {
    (0 until numTrack).map { i => outBuf.append("otrackE" + i.toString) }
  }
  if (gibParam.outTrickDir.contains(SOUTH)) {
    (0 until numTrack).map { i => outBuf.append("otrackS" + i.toString) }
  }
  this.outPorts = outBuf.toList

  val connectLists = getGIBConnect(gibParam.inTrickDir , gibParam.outTrickDir,  numTrack, diagPinConect, numIOPinList, fcList)
  val hasConst = connectLists.map(_._1).exists(_(0) == TYPE_IPIN)
//    .contains( ins => _(0) == TYPE_IPIN)
  if(hasConst) {
    val constIns = new EleTrace("Const",TYPE_ConstUnit.id , List(),List("out0"),List(width))
    addEle(constIns)
    connectLists.map {
      case (dst, srcs) => {
        val dstPortName = dst(1) match {
          case WEST => "otrackW" + dst(2).toString
          case NORTH => "otrackN"+ dst(2).toString
          case EAST => "otrackE"+ dst(2).toString
          case SOUTH => "otrackS"+ dst(2).toString
          case NORTHWEST => "ipinNW"+ dst(2).toString
          case NORTHEAST => "ipinNE"+ dst(2).toString
          case SOUTHEAST => "ipinSE"+ dst(2).toString
          case SOUTHWEST => "ipinSW"+ dst(2).toString
        }
        outBuf -= dstPortName
        val inNum =if(dst(0) == TYPE_IPIN) srcs.size + 1 else  srcs.size
        if (inNum > 1) {
//          val inNum =if(dst(0) == TYPE_IPIN) srcs.size + 1 else  srcs.size
          val MuxInPortsList = ( 0 until inNum).map( "in" + _.toString).toList
          val dircon = dst(1) == WEST ||  dst(1) == EAST
          val mux = new EleTrace("mux"+ dst(1).toString  + dst(2).toString ,if(gibParam.withReg&&dircon )TYPE_MultiplexerR.id else TYPE_Multiplexer.id,MuxInPortsList , List("out0"),List(inNum,width) )
          addEle(mux)
          addConnect(("mux"+ dst(1).toString  + dst(2).toString, "out0"), ("this", dstPortName))
//          dst(1) match {
//            case WEST => addConnect(("mux"+ dst(1).toString  + dst(2).toString, "out0"), ("this", "otrackW" + dst(2).toString))
//            case NORTH => addConnect(("mux"+ dst(1).toString  + dst(2).toString, "out0"), ("this", "otrackN" + dst(2).toString))
//            case EAST => addConnect(("mux"+ dst(1).toString  + dst(2).toString, "out0"), ("this", "otrackE" + dst(2).toString))
//            case SOUTH => addConnect(("mux"+ dst(1).toString  + dst(2).toString, "out0"), ("this", "otrackS" + dst(2).toString))
//            case NORTHWEST => addConnect(("mux"+ dst(1).toString  + dst(2).toString, "out0"), ("this", "ipinNW" + dst(2).toString))
//            case NORTHEAST => addConnect(("mux"+ dst(1).toString  + dst(2).toString, "out0"), ("this", "ipinNE" + dst(2).toString))
//            case SOUTHEAST => addConnect(("mux"+ dst(1).toString  + dst(2).toString, "out0"), ("this", "ipinSE" + dst(2).toString))
//            case SOUTHWEST => addConnect(("mux"+ dst(1).toString  + dst(2).toString, "out0"), ("this", "ipinSW" + dst(2).toString))
//          }
          for (i <- 0 until srcs.size) {
            val src = srcs(i)
            src(1) match {
              case WEST => addConnect(("this", "itrackW" + src(2).toString), ("mux"+ dst(1).toString  + dst(2).toString, "in" + i.toString))
              case NORTH => addConnect(("this", "itrackN" + src(2).toString), ("mux"+ dst(1).toString  + dst(2).toString, "in" + i.toString))
              case EAST => addConnect(("this", "itrackE" + src(2).toString), ("mux"+ dst(1).toString  + dst(2).toString, "in" + i.toString))
              case SOUTH => addConnect(("this", "itrackS" + src(2).toString), ("mux"+ dst(1).toString  + dst(2).toString, "in" + i.toString))
              case NORTHWEST => addConnect(("this", "opinNW" + src(2).toString), ("mux"+ dst(1).toString  + dst(2).toString, "in" + i.toString))
              case NORTHEAST => addConnect(("this", "opinNE" + src(2).toString), ("mux"+ dst(1).toString  + dst(2).toString, "in" + i.toString))
              case SOUTHEAST => addConnect(("this", "opinSE" + src(2).toString), ("mux"+ dst(1).toString  + dst(2).toString, "in" + i.toString))
              case SOUTHWEST => addConnect(("this", "opinSW" + src(2).toString), ("mux"+ dst(1).toString  + dst(2).toString, "in" + i.toString))
            }
          }
          if(dst(0) == TYPE_IPIN) addConnect(("Const","out0"),("mux"+ dst(1).toString  + dst(2).toString,MuxInPortsList.last))
        }else{
//          val desName = dst(1) match{
//            case WEST => "otrackW"
//            case NORTH => "otrackN"
//            case EAST => "otrackE"
//            case SOUTH => "otrackS"
//            case NORTHWEST => "ipinNW"
//            case NORTHEAST => "ipinNE"
//            case SOUTHEAST =>"ipinSE"
//            case SOUTHWEST => "ipinSW"
//          }

          val srcName = srcs(0)(1) match {
            case WEST => "itrackW"
            case NORTH => "itrackN"
            case EAST => "itrackE"
            case SOUTH =>"itrackS"
            case NORTHWEST => "opinNW"
            case NORTHEAST => "opinNE"
            case SOUTHEAST => "opinSE"
            case SOUTHWEST => "opinSW"
          }
          if(dst(0) == TYPE_IPIN) addConnect(("Const","out0"),("this" ,dstPortName ))
          else addConnect(("this" ,srcName + srcs(0)(2).toString ),("this" ,dstPortName ))
        }
      }
    }
  }else{
    connectLists.map {
      case (dst, srcs) => {
        if (srcs.size > 1) {
          val inNum =  srcs.size
          val MuxInPortsList = (0 until inNum).map("in" + _.toString).toList
          val dircon = dst(1) == WEST ||  dst(1) == EAST
          val mux = new EleTrace("mux" + dst(1).toString + dst(2).toString,if(gibParam.withReg && dircon )TYPE_MultiplexerR.id else TYPE_Multiplexer.id, MuxInPortsList, List("out0"), List(inNum, width))
          addEle(mux)
          dst(1) match {
            case WEST => addConnect(("mux" + dst(1).toString + dst(2).toString, "out0"), ("this", "otrackW" + dst(2).toString))
            case NORTH => addConnect(("mux" + dst(1).toString + dst(2).toString, "out0"), ("this", "otrackN" + dst(2).toString))
            case EAST => addConnect(("mux" + dst(1).toString + dst(2).toString, "out0"), ("this", "otrackE" + dst(2).toString))
            case SOUTH => addConnect(("mux" + dst(1).toString + dst(2).toString, "out0"), ("this", "otrackS" + dst(2).toString))
            case NORTHWEST => addConnect(("mux" + dst(1).toString + dst(2).toString, "out0"), ("this", "ipinNW" + dst(2).toString))
            case NORTHEAST => addConnect(("mux" + dst(1).toString + dst(2).toString, "out0"), ("this", "ipinNE" + dst(2).toString))
            case SOUTHEAST => addConnect(("mux" + dst(1).toString + dst(2).toString, "out0"), ("this", "ipinSE" + dst(2).toString))
            case SOUTHWEST => addConnect(("mux" + dst(1).toString + dst(2).toString, "out0"), ("this", "ipinSW" + dst(2).toString))
          }
          for (i <- 0 until srcs.size) {
            val src = srcs(i)
            src(1) match {
              case WEST => addConnect(("this", "itrackW" + src(2).toString), ("mux" + dst(1).toString + dst(2).toString, "in" + i.toString))
              case NORTH => addConnect(("this", "itrackN" + src(2).toString), ("mux" + dst(1).toString + dst(2).toString, "in" + i.toString))
              case EAST => addConnect(("this", "itrackE" + src(2).toString), ("mux" + dst(1).toString + dst(2).toString, "in" + i.toString))
              case SOUTH => addConnect(("this", "itrackS" + src(2).toString), ("mux" + dst(1).toString + dst(2).toString, "in" + i.toString))
              case NORTHWEST => addConnect(("this", "opinNW" + src(2).toString), ("mux" + dst(1).toString + dst(2).toString, "in" + i.toString))
              case NORTHEAST => addConnect(("this", "opinNE" + src(2).toString), ("mux" + dst(1).toString + dst(2).toString, "in" + i.toString))
              case SOUTHEAST => addConnect(("this", "opinSE" + src(2).toString), ("mux" + dst(1).toString + dst(2).toString, "in" + i.toString))
              case SOUTHWEST => addConnect(("this", "opinSW" + src(2).toString), ("mux" + dst(1).toString + dst(2).toString, "in" + i.toString))
            }
          }
        }else{
          val desName = dst(1) match {
            case WEST => "otrackW"
            case NORTH => "otrackN"
            case EAST => "otrackE"
            case SOUTH => "otrackS"
            case NORTHWEST => "ipinNW"
            case NORTHEAST => "ipinNE"
            case SOUTHEAST => "ipinSE"
            case SOUTHWEST => "ipinSW"
          }

          val srcName = srcs(0)(1) match {
            case WEST => "itrackW"
            case NORTH => "itrackN"
            case EAST => "itrackE"
            case SOUTH => "itrackS"
            case NORTHWEST => "opinNW"
            case NORTHEAST => "opinNE"
            case SOUTHEAST => "opinSE"
            case SOUTHWEST => "opinSW"
          }
          addConnect(("this" ,srcName + srcs(0)(2).toString ),("this" ,desName + dst(2).toString ))
        }
      }
    }
  }
//  outBuf.map{
//    outPort => {
//      addConnect(("empty" , ""),("this" , ))
//    }
//  }
}
object GIB {
  def apply(gibParam: GIBParam, name: String): GIB = {
    new GIB(gibParam, name)
  }
}

object GIBFGen extends App {
  //  val pe = new PE(PEParam(List(OPC.ADD) , 32,"PETest"),"PE")
  val gibParam = GIBParam(List(2), List(2,3),4 , true ,List(0, 0, 0, 1, 1, 0, 0, 0) ,  List(4, 4, 4) , 32 , "GIB")
  val gibModule = GIB(gibParam,"GIB").getModuleInfo()
  infooutput("gib.scala", gibModule)
    chisel3.Driver.execute(args,() => topGen(GIB(gibParam,"GIB").getModuleInfo(), "GIBTest.txt") )//生成verilog
  dumpIR(gibModule,"gibModule.xml","gibModule")
}