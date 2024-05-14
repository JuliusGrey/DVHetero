package RISCV.DMA

import RISCV.common.Param.addrW
import chisel3._
import chisel3.util._

import scala.collection.mutable.ListBuffer
//先用加法器整一个简单的，后面再改吧
class MyArbiter extends Module {
  val io = IO(new Bundle {
    val regEn = Input(Bool())
    val in = Flipped(Vec(3, Decoupled(UInt(8.W))))
    val out = Decoupled(UInt(8.W))
    val chosen = Output(UInt())
  })
//  //  withReset(reset) {
//  val arbiter = Module(new RRArbiter(UInt(8.W), 3)) // 2 to 1 Priority Arbiter
//  //    }
//  arbiter.reset<> reset
//  arbiter.io.in <> io.in
//  io.out <> arbiter.io.out
//  io.chosen := arbiter.io.chosen
}

class arbitInport extends Bundle {
  val valid = Input(Bool())
  val address = Input(UInt(addrW.W))
}


//class adresPortArbit(portNum : Int) extends Module{
// val io = IO(new  Bundle {
//   val regEn = Input(Bool())
//   val in =  Vec(portNum ,new (arbitInport) )
//   val sel = Input(UInt(log2Up(portNum).W))
//   val out = Output(UInt(addrW.W))
// })
//  val indexWires = Wire(Vec(portNum,UInt(log2Up(portNum).W)))
//  indexWires(0) := Mux( io.in(0).valid , 1.U , 0.U)
//  for( i <- 1 until portNum){
//    val indexAdd = 0.U
//    for(j <-  0  to i ){
//      indexAdd === indexAdd + io.in(j).valid.asUInt()
//    }
//    indexWires(i) :=Mux(
//      io.in(i).valid,
//      indexAdd,
//      0.U
//    )
//  }
////  val outWire = MuxCase(
////    0.U,
////    io.in.map(_.address).zipWithIndex.map {
////      case (addres , index) => {
////        (indexWires(index) === io.sel + 1.U, addres)
////      }
////    }
////  )
//  val outWire = MuxLookup(
//    io.sel + 1.U,
//    0.U,
//    io.in.map(_.address).zipWithIndex.map{
//      case (addres ,index) =>{
//      (indexWires(index) , addres)
//      }
//    }
//  )
//
//  io.out := RegEnable(outWire,0.U,io.regEn)
//
//
//
//}
//输入使能之后下一周期得到地址
class adresPortArbit(portNum : Int) extends Module{
  val io = IO(new  Bundle {
    val regEn = Input(Bool())
    val batchBgein = Input(Bool())
    val in =  Vec(portNum ,new (arbitInport) )
    val  dmaEn = Input(Bool())
    val out = Output(UInt(addrW.W))
  })
  dontTouch(io.in)
    //每一个端口都对应一组所有端口的有效信号和索引
  val validList = io.in.map(_.valid)
  val validListArbit = List.fill(portNum)(Wire(Vec(portNum,Bool())))
  val indexeListArbit = List.fill(portNum)(Wire(Vec(portNum,UInt(log2Up(portNum).W))))
//这里是将每个端口的索引赋值给
  for( i <- 0 until portNum){
    for(j <- 0 until portNum){
      val index = if(i + j >= portNum) i + j - portNum else i+j
      validListArbit(i)(j) := validList(index)
      indexeListArbit(i)(j) := index.U
    }
  }

//计算索引
  val indexWire = Wire(UInt(log2Up(portNum).W))
  val indexReg = Wire(UInt(log2Up(portNum).W))
  val dmaNext = RegNext(io.dmaEn)
  withReset((io.dmaEn&& !dmaNext) || reset.asBool()) {
    indexReg := RegEnable(
      indexWire,
      0.U(log2Up(portNum).W),
      io.regEn || io.batchBgein
    )
  }

 //每一个端口索引对应的一组优先编码器
  val porMuxList = (0 until portNum).map{ i =>
    i.U -> PriorityMux(validListArbit(i), indexeListArbit(i))
  }
//由优先编码器选出下一个端口
  val selOut = MuxLookup(
    indexReg,
    0.U,
    porMuxList
  )
  indexWire :=  Mux(selOut + 1.U  === portNum.U , 0.U , selOut + 1.U )

//进行地址选择
  io.out := RegEnable(
    MuxLookup(
      selOut,
      0.U,
      ( 0 until portNum).map{
        i => (i.U -> io.in(i).address)
      }
    ),
    0.U,
    io.regEn || io.batchBgein
  )







}

object abritGen extends App {
  chisel3.Driver.execute(args,() => new adresPortArbit(2) )

}
