package CGRA.parameter

import CGRA.element.ALU.{OPC, OpInfo}
import CGRA.parameter.Param._
import chisel3.util._
object Param {
  val cfgDataW = 32
  val dataW = 32
  val accInitW = 8//现在累加器的配置信息先这么决定吧，后续如果有变化，就只需要变这里和Acc内部的逻辑
  val accCounterW=8
  val hasAcc = true//简单点，把是否有ACC的判断写到这里就好了
}
object EleType extends Enumeration {
  type EleType = Value

  val TYPE_ALU,
  TYPE_ConstUnit,
  TYPE_Delay,
  TYPE_Multiplexer,
  TYPE_MultiplexerR,
  TYPE_Register ,
  TYPE_IB ,
  TYPE_OB ,
  TYPE_IO,
  TYPE_Module= Value

  val eleTypeNum = this.values.size - 2


}

object EleTypeFun {
  def getEleCfgW (eleType : Int , elePar : List[Int]) : Int = {
    EleType(eleType) match {
      case EleType.TYPE_ALU => if(hasAcc) log2Ceil(OPC.numOPC) + accCounterW+accInitW else log2Ceil(OPC.numOPC)
      case EleType.TYPE_ConstUnit => elePar(0)
      case EleType.TYPE_Delay => elePar(0)
      case EleType.TYPE_Multiplexer => log2Up( elePar(0))
      case EleType.TYPE_MultiplexerR => log2Up( elePar(0))
      case EleType.TYPE_IB => 0
      case EleType.TYPE_OB =>log2Up( elePar(0))
      case EleType.TYPE_Register => 0
    }
  }

  def eleTypeName(eleType : Int) : String = {
    EleType(eleType) match {
      case EleType.TYPE_ALU => "ALU"
      case EleType.TYPE_ConstUnit => "ConstUnit"
      case EleType.TYPE_Delay => "Delay"
      case EleType.TYPE_Multiplexer => "MUX"
      case EleType.TYPE_MultiplexerR => "MUXR"
      case EleType.TYPE_Register => "Register"
      case EleType.TYPE_IB => "IB"
      case EleType.TYPE_OB => "OB"
    }
  }

  def getEleInNum(eleType : Int , elePar : List[Int]) : Int = {
    EleType(eleType) match {
      case EleType.TYPE_ALU => {
        val ops = OpInfo.fuget(elePar(0))
        ops.map(OpInfo.getOperandNum(_)).max
      }
      case EleType.TYPE_ConstUnit =>0
      case EleType.TYPE_Delay => 1
      case EleType.TYPE_Multiplexer => elePar(0)
      case EleType.TYPE_MultiplexerR => elePar(0)
      case EleType.TYPE_Register => 1
      case EleType.TYPE_IB => 1
      case EleType.TYPE_OB => elePar(0)
    }
  }

  def getEleOutNum(eleType: Int, elePar: List[Int]): Int = {
    EleType(eleType) match {
      case EleType.TYPE_ALU => 1
      case EleType.TYPE_ConstUnit => 1
      case EleType.TYPE_Delay => 1
      case EleType.TYPE_Multiplexer => 1
      case EleType.TYPE_MultiplexerR => 1
      case EleType.TYPE_Register => 1
      case EleType.TYPE_IB => 1
      case EleType.TYPE_OB => 1
    }
  }

  def getEleWidth(eleType: Int, elePar: List[Int]): Int = {
    EleType(eleType) match {
      case EleType.TYPE_ALU => elePar(1)
      case EleType.TYPE_ConstUnit => elePar(0)
      case EleType.TYPE_Delay =>  elePar(1)
      case EleType.TYPE_Multiplexer =>  elePar(1)
      case EleType.TYPE_MultiplexerR =>  elePar(1)
      case EleType.TYPE_Register => elePar(0)
      case EleType.TYPE_IB => elePar(0)
      case EleType.TYPE_OB => elePar(1)
    }
  }

  def getElePortName (eleType: Int, isIn : Boolean , num : Int) : String = {
    eleType match {
      case _ => {
        if(isIn){
          "in" + num
        } else {
          "out" + num
        }
      }
    }
  }
}



object DirParam{
  // Directions
  val WEST = 0
  val NORTH = 1
  val EAST = 2
  val SOUTH = 3
  val NORTHWEST = 4
  val NORTHEAST = 5
  val SOUTHEAST = 6
  val SOUTHWEST = 7
  // GIB port type
  val TYPE_IPIN = 0
  val TYPE_OPIN = 1
  val TYPE_ITRACK = 2
  val TYPE_OTRACK = 3
}
