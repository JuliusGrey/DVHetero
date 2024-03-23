//package TOP.CGRAPort
//
//import CGRA.ARCH.CGRAGen._
//import CGRA.module.TopModule.topGen
//import RISCV.common.Interface.cpuRWIO
//import RISCV.common.Param.{addrW, dataW}
//import chisel3._
//import chisel3.util._
//
//
//
////这里只配置行列和位宽吧别的都在内部配置
////先不把输出接到处理器吧
//class CGRAFull extends Module{
//  val io = IO(new Bundle {
//    val CGRAIO = new cpuRWIO(dataW, addrW)
//    val outputs = Output(Vec(cgraParam.colNum,UInt((cgraParam.width + 1).W)))
//  })
//  val outQueue = List.fill(cgraParam.colNum)(Module( new Queue(UInt(cgraParam.width.W) , 20)))
//  val outArbiter = Module(new RRArbiter(UInt(cgraParam.width.W),cgraParam.colNum))
//  (0 until cgraParam.colNum).foreach(i => {
//    outArbiter.io.in(i) <> outQueue(i).io.deq
//  })
//
//  //调用CGRATop中的相关参数
//  val synInInst = Module(new SynIn(cgraParam.colNum,cgraParam.width,dataW))
//
//  val cgraInst = Module( topGen(CGRAModule, "CGRA.txt"))
//
//  (0 until cgraParam.colNum).foreach(i => {
//
//    cgraInst.io.inputs(i) := synInInst.io.dataOut(i)
//    outQueue(i).io.enq.bits := cgraInst.io.outputs(i)(cgraParam.width - 1 , 0)
//    outQueue(i).io.enq.valid := cgraInst.io.outputs(i)(cgraParam.width)
//    io.outputs(i) := outQueue(i).io.deq.bits
//
//  })
//
//  val oneHList1 = (0 until synInInst.io.dataIn.size).map { i =>
//    ("h02010000".U + (8 * i).U) === io.CGRAIO.addr
//  }.toList
////  val oneHList2 = (0 until( cgraInst.io.outputs.size)).map{
////    i => ("h02010000".U + (8 * synInInst.io.dataIn.size + i*4).U) === io.CGRAIO.addr
////  }
//  val oneHot =oneHList1 ++  List(("h02010000".U + (8 * synInInst.io.dataIn.size ).U) === io.CGRAIO.addr,
//  (("h02010000".U + (cgraParam.colNum*cgraParam.width*2/8).U) === io.CGRAIO.addr) ,(
//    (("h02010000".U + (cgraParam.colNum*cgraParam.width*2/8 + dataW/8).U) === io.CGRAIO.addr))
//  )
////  print("val is " + 8 * synInInst.io.dataIn.size)
//
//
//  io.CGRAIO.ready := Mux1H(
//    oneHot,
//    synInInst.io.dataIn.map{
//      i =>i.ready
//    }.toList ++
//      List (outArbiter.io.out.valid ,true.B,true.B)
//  )
//
//
//  (0 until synInInst.io.dataIn.size).map{
//    i => synInInst.io.dataIn(i).valid := io.CGRAIO.addr === ("h02010000".U + (i*dataW/8 ).U) && io.CGRAIO.valid
//  }
////  (0 until outQueue.size).map {
////    i => outQueue(i).io.deq.ready := io.CGRAIO.addr === ("h02010000".U + (8 * synInInst.io.dataIn.size + i*4).U) && io.CGRAIO.valid
////  }
//  outArbiter.io.out.ready := io.CGRAIO.addr === ("h02010000".U + (8 * synInInst.io.dataIn.size ).U) && io.CGRAIO.valid
//  cgraInst.io.cfgEn := io.CGRAIO.addr ===("h02010000".U + (cgraParam.colNum*cgraParam.width*2/8).U) && io.CGRAIO.valid
//  synInInst.io.delayen := io.CGRAIO.addr ===("h02010000".U + (cgraParam.colNum*cgraParam.width*2/8+ dataW/8).U) && io.CGRAIO.valid
//
//
//
//  (0 until synInInst.io.dataIn.size).map{i=>
//    synInInst.io.dataIn(i).dataIn := io.CGRAIO.data_write
//  }
//  cgraInst.io.cfgData := io.CGRAIO.data_write(31,0)
//  cgraInst.io.cfgAddr := io.CGRAIO.data_write(63,32)
//  synInInst.io.delayCycle := io.CGRAIO.data_write
//
//  io.CGRAIO.data_read:=Mux1H(
//    oneHot,
//    List.fill(synInInst.io.dataIn.size)(0.U) ++ List(outArbiter.io.out.bits , 0.U,0.U)
//  )
//
//
//
//
//}
//
//object CGRAFullAPP extends App {
//
//  chisel3.Driver.execute(args,() => new CGRAFull )
//
//
//}
