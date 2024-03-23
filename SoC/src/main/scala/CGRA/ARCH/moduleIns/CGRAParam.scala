package CGRA.ARCH.moduleIns

import CGRA.element.ALU.{OPC, OpInfo}

object CGRAParam {
  class PEParam(opList_ :List[OPC.OPC] , width_ : Int ,deviceName_ :String){
    val opList:List[OPC.OPC] = opList_
    val width = width_
    val deviceName = deviceName_

    def == ( PE : PEParam): Boolean = PE.opList.toSet == opList.toSet
  }
  object PEParam{
    def apply(opList_ :List[OPC.OPC] , width_ : Int ,deviceName_ :String) : PEParam = {
      new PEParam(opList_  , width_ ,deviceName_ )
    }
  }


  class IBParam(maxDelay_ : Int, width_ : Int,deviceName_ : String){
    val maxDelay = maxDelay_
//    val outPortsNum = outPortsNum_
    val width = width_
    val deviceName = deviceName_
  }
  object IBParam {
    def apply(maxDelay_ : Int, width_ : Int,deviceName_ : String) : IBParam = {
      new IBParam(maxDelay_ ,width_ ,deviceName_ )
    }
  }


  class OBParam( inPortsNum_ : Int, width_ : Int, deviceName_ : String) {
    val inPortsNum = inPortsNum_
    val width = width_
    val deviceName = deviceName_
  }

  object OBParam {
    def apply(inPortsNum_ : Int, width_ : Int, deviceName_ : String): OBParam = {
      new OBParam( inPortsNum_, width_, deviceName_)
    }
  }

  //numIOPinList_ 先输出后输入 顺序和原来一样
  class GIBParam(inTrackDir_ : List[Int], outTrackDir_ : List[Int],numTrack_ : Int , diagPinConect_ : Boolean , numIOPinList_ : List[Int] , fcList_ : List[Int] , w_ : Int , devicename_ : String , withReg_ : Boolean){
    //  require(numIOPinList_.size == 8)
    var numTrack =numTrack_
    var inTrickDir = inTrackDir_
    var outTrickDir = outTrackDir_
    var diagPinConect = diagPinConect_
    //  var numIOPinList =numIOPinList_
    var fcList = fcList_
    var devicename = devicename_
    var width = w_
    var nNWi = numIOPinList_(0)
    var nNWo = numIOPinList_(1)
    var nNEi = numIOPinList_(2)
    var nNEo = numIOPinList_(3)
    var nSEi = numIOPinList_(4)
    var nSEo = numIOPinList_(5)
    var nSWi = numIOPinList_(6)
    var nSWo = numIOPinList_(7)
    var withReg = withReg_

    def printGIB () :Unit = {
      println("devicename_ " + devicename_)
      println("numTrack " + numTrack)
      println("inTrickDir " + inTrickDir)
      println("outTrickDir " + outTrickDir)
      println("diagPinConect " + diagPinConect)
      println("fcList " + fcList)
      println("width " + width)
      println("numIOPinList_ " + numIOPinList_)
      println("withReg " + withReg)
      println()
    }

    def == (GIB :GIBParam ) : Boolean = numTrack == GIB.numTrack && diagPinConect ==  GIB. diagPinConect && fcList == GIB.fcList && devicename == GIB.devicename &&
      (nNWi, nNWo ,nNEi, nNEo, nSEi, nSEo, nSWi, nSWo) ==( GIB.nNWi,GIB.nNWo ,GIB.nNEi, GIB.nNEo, GIB.nSEi, GIB.nSEo, GIB.nSWi, GIB.nSWo) &&  withReg == GIB.withReg
  }

  object GIBParam{
    def apply(inTrackDir_ : List[Int],outTrackDir_ : List[Int],numTrack_ : Int = 4 , diagPinConect_ : Boolean = true , numIOPinList_ : List[Int] = List(1,1,1,1,1,1,1,1) , fcList_ : List[Int] = List(2,2,2), w_ : Int  = 32, devicename_ : String = "gib" , withReg_ : Boolean = false): GIBParam = {
      new  GIBParam(inTrackDir_,outTrackDir_, numTrack_, diagPinConect_,numIOPinList_  , fcList_ , w_  , devicename_  ,withReg_)
    }
  }


  class CGRAParam(rowNum_ : Int, colNum_ :Int , trackNum_ : Int, width_ : Int ,maxDelay_  :Int , fList_ :List[Int], digConnect_ :Boolean , peOutNum_ :Int ,regMod_ : Int , ioMod_ : Int ,dirMod_ :Int){
    val rowNum = rowNum_
    val colNum =colNum_
    val trackNum =trackNum_
    val width =width_
    val maxDelay =maxDelay_
    val fList =fList_
    val digConnect =digConnect_
    val peOutNum = peOutNum_
    val regMod = regMod_
    val ioMod = ioMod_
    val dirMod = dirMod_

  }
  object CGRAParam{
    def apply(rowNum_ : Int, colNum_ :Int , trackNum_ : Int, width_ : Int ,maxDelay_  :Int , fList_ :List[Int], digConnect_ :Boolean ,peOutNum_ : Int , regMod_ : Int , ioMod_ :Int , dirMod_ : Int) : CGRAParam = {
      new CGRAParam(rowNum_, colNum_ , trackNum_, width_  ,maxDelay_ , fList_ , digConnect_ ,peOutNum_  ,regMod_ , ioMod_ , dirMod_)
    }
  }


}
