package CGRA.element.ALU

import chisel3._
import chisel3.util._
//import common.Param.{SIMTEST, dataW}

import scala.collection.mutable.ListBuffer
class Add(val addW:Int) extends Module {
  val io = IO (new Bundle() {
    val cin = Input(Bool())
    val a = Input(UInt(addW.W))
    val b = Input(UInt(addW.W))
    val sum = Output(UInt(addW.W))
    val cout = Output(Bool())
  })
  //  if(SIMTEST) {
  val res = Wire(UInt((addW + 1).W ))
  res:=  Cat (false.B,io.a) + Cat (false.B,io.b) +io.cin
  io.sum := res(addW - 1,0)
  io.cout := res(addW)
}
