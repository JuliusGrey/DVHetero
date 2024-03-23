package CGRA.element

import chisel3._
import chisel3.util._
import CGRA.parameter.Param.cfgDataW

class CfgMem(regW:Int) extends Module{
  val cfgAddrWidth = log2Ceil((regW  + cfgDataW - 1) / cfgDataW)
  val io = IO(
    new Bundle() {
      val cfgEn = Input(Bool())
      val cfgAddr = Input(UInt(cfgAddrWidth.W))
      val cfgData = Input(UInt(cfgDataW.W))
      val cfgOut = Output(UInt(regW.W))
    }
  )
  val num = (regW  + cfgDataW - 1) / cfgDataW
  val outWire = Wire(Vec(num , UInt(cfgDataW.W)))
  for( i<- 0 until num){
    outWire(i) := RegEnable(io.cfgData , 0.U , io.cfgEn && io.cfgAddr === i.U)
  }
  io.cfgOut := Cat(outWire.reverse)

//  val out = List.fill((regW  + cfgDataW - 1) / cfgDataW)(Wire(UInt(cfgDataW.W)))
////    RegInit(VecInit(Seq.fill((regW  + cfgW - 1) / cfgW){0.U(cfgW.W)}))
//
//  io.cfgOut :=Cat(out.reverse)(regW-1 , 0)
//  if(regW <= cfgDataW){
//    out(0) := RegEnable(io.cfgData,0.U,io.cfgEn)
//  }else{
//    out.indices.map{
//      index =>{
//        out(index) := RegEnable(io.cfgData,0.U,io.cfgEn && io.cfgAddr === index.U)
//      }
//    }
//  }

}

object CfgGen extends App {
  chisel3.Driver.execute(args,() => new CfgMem(48) )


}
