package CGRA.ARCH.FullConnnect

import CGRA.element.ALU.OPC

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


  class IBParam(width_ : Int,deviceName_ : String){
//    val outPortsNum = outPortsNum_
    val width = width_
    val deviceName = deviceName_
  }
  object IBParam {
    def apply( width_ : Int,deviceName_ : String) : IBParam = {
      new IBParam(width_ ,deviceName_ )
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

  class MatrixFCParam( lastIn_ : Int,nextIn_ :Int,dirIn_  : Int,nextOutWithoutConst_ :Int, nextOutWithConst_ :Int,dirOut_ :Int, hasReg_ : Boolean, width_ : Int, deviceName_ : String){
    val lastIn = lastIn_
    val nextIn = nextIn_
    val dirIn = dirIn_
    val nextOutWithoutConst = nextOutWithoutConst_
    val nextOutWithConst = nextOutWithConst_
    val dirOut = dirOut_
    val hasReg = hasReg_
    val width = width_
    var deviceName = deviceName_

    def == (param :MatrixFCParam) :Boolean =param.nextOutWithoutConst == nextOutWithoutConst && param.nextOutWithConst==nextOutWithConst &&
      param.dirOut ==dirOut && param.hasReg == hasReg &&
      lastIn == param.lastIn &&
      nextIn == param.nextIn &&
      dirIn == param.dirIn

  }
  object  MatrixFCParam{
    def apply( lastIn_ : Int,nextIn_ :Int,dirIn_  : Int,nextOutWithoutConst_ :Int, nextOutWithConst_ :Int,dirOut_ :Int, hasReg_ : Boolean, width_ : Int, deviceName_ : String) :MatrixFCParam ={
      new MatrixFCParam( lastIn_ ,nextIn_ ,dirIn_  ,nextOutWithoutConst_ , nextOutWithConst_ ,dirOut_ , hasReg_ , width_ , deviceName_ )
    }
  }


  class CGRAFCParam(inNum_ : Int, outNum_ : Int ,dirNum_ :Int, PENums_ : List[Int] ,hasReg_ :Boolean ,width_ : Int){
    val inNum = inNum_
    val outNum =outNum_
    val dirNum = dirNum_
    val PENums =PENums_
    val hasReg = hasReg_
    val width = width_
  }

object CGRAFCParam{
  def apply(inNum_ : Int, outNum_ : Int ,dirNum_ :Int, PENums_ : List[Int] ,hasReg_ :Boolean ,width_ : Int) :CGRAFCParam= {
    new CGRAFCParam(inNum_ , outNum_  ,dirNum_ , PENums_  ,hasReg_  ,width_ )
  }
}

}
