package RISCV.PIP

import RISCV.IDU.CtrlUnit.ResultSrcList.regFrom_DM
import hazardPa._
import chisel3._
import chisel3.util._
import firrtl.FirrtlProtos.Firrtl.Expression
object hazardPa{
  def FORWARDDEF = "b00".U(2.W)
  def FORWARD1 = "b10".U(2.W)
  def FORWARD2 = "b01".U(2.W)
  def FORWARD3 = "b11".U(2.W)
}
class hazard extends Module{
  val io = IO( new Bundle() {
    val regEnEXMEM = Input(Bool())
    val rdEXMEM = Input(UInt(5.W))
    val rs1IDEX =  Input(UInt(5.W))
    val rs2IDEX =  Input(UInt(5.W))
//    val rsdIDEX =  Input(UInt(5.W))//dma时候的第三个寄存器；这个端口已经有了，不用在添加
    val regEnMEMWB = Input(Bool())
    val rdMEMWB = Input(UInt(5.W))
    val regEnWBEND = Input(Bool())
    val rdWBEND = Input(UInt(5.W))

    val forwardA = Output(UInt(2.W))
    val forwardB = Output(UInt(2.W))
    val forwardC = Output(UInt(2.W))//dma时候的第三个寄存器的forward

    val rs1IFID =  Input(UInt(5.W))
    val rs2IFID =  Input(UInt(5.W))
    val rdIDEX =   Input(UInt(5.W))
    val resSrc =  Input(UInt(2.W))

    val loadHazard = Output(Bool())

  })
//使用有限编码器的话后面的条件是不需要的
  val forwardAOne = io.regEnEXMEM && io.rdEXMEM=== io.rs1IDEX && io.rs1IDEX=/=0.U
  val forwardATwo = io.regEnMEMWB && io.rdMEMWB === io.rs1IDEX && io.rs1IDEX=/=0.U &&(io.rdEXMEM=/= io.rs1IDEX || !io.regEnEXMEM)
  val forwardAThree = io.regEnWBEND && io.rdWBEND === io.rs1IDEX && io.rs1IDEX=/=0.U
  io.forwardA :=Mux(forwardAOne,FORWARD1,  Mux(forwardATwo ,FORWARD2 , Mux(forwardAThree,FORWARD3,FORWARDDEF) ))

  val forwardBOne = io.regEnEXMEM && io.rdEXMEM=== io.rs2IDEX && io.rs2IDEX=/=0.U
  val forwardBTwo = io.regEnMEMWB && io.rdMEMWB === io.rs2IDEX && io.rs2IDEX=/=0.U &&(io.rdEXMEM=/= io.rs2IDEX || !io.regEnEXMEM)
  val forwardBThree =  io.regEnWBEND && io.rdWBEND === io.rs2IDEX && io.rs2IDEX=/=0.U
  io.forwardB := Mux(forwardBOne,FORWARD1, Mux(forwardBTwo ,FORWARD2 , Mux(forwardBThree,FORWARD3,FORWARDDEF) ))


  val forwardCOne = io.regEnEXMEM && io.rdEXMEM === io.rdIDEX && io.rdIDEX =/= 0.U
  val forwardCTwo = io.regEnMEMWB && io.rdMEMWB === io.rdIDEX && io.rdIDEX =/= 0.U && (io.rdEXMEM =/= io.rdIDEX || !io.regEnEXMEM)
  val forwardCThree = io.regEnWBEND && io.rdWBEND === io.rdIDEX && io.rdIDEX =/= 0.U
  io.forwardC := Mux(forwardCOne, FORWARD1, Mux(forwardCTwo, FORWARD2, Mux(forwardCThree, FORWARD3, FORWARDDEF)))




  io.loadHazard := (io.rs1IFID === io.rdIDEX ||io.rs2IFID === io.rdIDEX) && io.resSrc === regFrom_DM




}
