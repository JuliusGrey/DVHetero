package CGRA.element.ALU
import CGRA.parameter.Param._
import chisel3._
import chisel3.util._
class Acc extends Module{
  val io = IO(new Bundle {
    val iniVal = Input(UInt(accInitW.W))
    val counter = Input(UInt(accCounterW.W))
    val inputVal =  Input(UInt((dataW + 1).W))
    val outputVal = Output(UInt((dataW + 1).W))
  })
  val valid = io.inputVal(dataW).asBool()
  val inVal = io.inputVal(dataW - 1 , 0)
  val accCntWire = Wire(UInt(accCounterW.W))
  val cntFinish = accCntWire === io.counter
  val accCntReg = RegEnable(
    Mux(
      cntFinish,
      Mux(
        valid,
        1.U,
        0.U
      ),
      accCntWire + 1.U
    )
    ,0.U,valid || cntFinish)
  accCntWire := accCntReg

  val accValWire = Wire(UInt(dataW .W))
  val accValReg = RegEnable(
    Mux(
      valid,
      Mux(
        cntFinish||(accCntReg === 0.U),//valid有效需要进行累加操作，复原状态或是计数刚结束，加初始值，否则累加。
        inVal + io.iniVal,
        inVal + accValWire
      ),
        0.U//此时valid无效cntFinish有效，清0就行

    ),
    0.U,
    valid || cntFinish
  )
  accValWire :=  accValReg


  io.outputVal := Cat(
    cntFinish,
    Mux(
      cntFinish,
      accValWire,
      0.U(32.W)
    )
  )

}

object AccGen extends App {
  chisel3.Driver.execute(args,() => new Acc )


}