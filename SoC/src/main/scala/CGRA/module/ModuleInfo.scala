package CGRA.module

import CGRA.parameter.EleTypeFun.getEleCfgW

import scala.collection.mutable.{ArrayBuffer, ListBuffer, Map}

class ModuleInfo(
                  type_ : String,
                  name: String,
                  moduleList: List[ModuleInfo],
                  moduleInsts: List[Int],
                  elementLists: Map[Int,List[List[Int]]],
                  inPortList: List[String],
                  outPortList: List[String],
                  connect : List[Tuple2[List[Int],List[List[Int]]]],
                  width: Int)
{

  def getType(): String = {
    type_
  }

  def getName(): String = {
    name
  }

  def getModuleList(): List[ModuleInfo] = {
    moduleList
  }

  def getModuleInsts(): List[Int] = {
    moduleInsts
  }

  def getElementLists(): List[Tuple2[Int, List[List[Int]]]] = {
    elementLists.toList.sortBy(i => i._1)
  }

  def getInPortList(): List[String] = {
    inPortList
  }

  def getOutPortList(): List[String] = {
    outPortList
  }

  def getConnect(): List[Tuple2[List[Int], List[List[Int]]]] = {
    connect
  }

  def getCfgList(): List[Tuple2[Int, List[Int]] ]= {
    val cfglist: Map[Int, List[Int]] = Map()


    elementLists.map {
      case (eleType, elelist) => {
        cfglist += (eleType -> elelist.map(i => getEleCfgW(eleType,i)))
      }
    }
    cfglist.toList.sortBy(i => i._1)
  }

  def getCfgW(): Int = {
    val cfglist = this.getCfgList()
    (cfglist.map {
      case (type_, elelist) => elelist.foldLeft(0)((b, a) => b + a)
    }).foldLeft(0)((b, a) => b + a)
  }

  def getWidth(): Int = {
    width
  }



}
