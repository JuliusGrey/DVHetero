package TOP.CGRAPort
import CGRA.ARCH.FullConnnect.CGRAFCGen.CGRAFCModule
import CGRA.ARCH.moduleIns.CGRAGen._
import CGRA.module.TopModule.topGen
import RISCV.common.Interface.cpuRWIO
import RISCV.common.Param.{addrW, dataW}
import TOP.paramG.cgraFC
import chisel3._
import chisel3.util._
class CGRAFullMask extends Module {
  val io = IO(new Bundle {
    val CGRAIO = new cpuRWIO(dataW, addrW)
    val outputs = Output(Vec(cgraParam.colNum, UInt((cgraParam.width + 1).W)))
  })

  val outQueue = List.fill(cgraParam.colNum)(Module(new Queue(UInt(cgraParam.width.W), 20)))

  //调用CGRATop中的相关参数
  val synInInst = Module(new SynInMask(cgraParam.colNum, cgraParam.width, dataW))

//  val cgraInst = Module(topGen(CGRAModule, "CGRA.txt"))
//val cgraInst = Module(topGen(CGRAFCModule, "CGRA.txt"))
  val cgraInst = if(cgraFC) Module(topGen(CGRAFCModule, "CGRA.txt")) else  Module(topGen(CGRAModule, "CGRA.txt"))

  (0 until cgraParam.colNum).foreach(i => {

    cgraInst.io.inputs(i) := synInInst.io.dataOut(i)
    outQueue(i).io.enq.bits := cgraInst.io.outputs(i)(cgraParam.width - 1, 0)
    outQueue(i).io.enq.valid := cgraInst.io.outputs(i)(cgraParam.width)
    io.outputs(i) := cgraInst.io.outputs(i)

  })
  val addrIsW = io.CGRAIO.addr >= "h02010000".U && io.CGRAIO.addr <= "h02010000".U + (8 * 8).U
  //  val oneHList1 = (0 until synInInst.io.dataIn.size).map { i =>
  //    ("h02010000".U + (8 * i).U) === io.CGRAIO.addr
  //  }.toList
  val oneHotList1 = List(addrIsW)
  val oneHList2 = (0 until (cgraInst.io.outputs.size)).map {
    i => ("h02010000".U + (8 * 8 + i * 4).U) === io.CGRAIO.addr
  }
  val oneHotWire = dontTouch(Wire(UInt(19.W)))
  val outReadyList= dontTouch(Wire(UInt(19.W)))
  val oneHot = (oneHotList1 ++ oneHList2 ++ List((("h02010000".U + (cgraParam.colNum * cgraParam.width * 2 / 8).U) === io.CGRAIO.addr), (
    (("h02010000".U + (cgraParam.colNum * cgraParam.width * 2 / 8 + dataW / 8).U) === io.CGRAIO.addr))
  ))
  val oneHotMap = List(synInInst.io.ready) ++
    outQueue.map {
      i => i.io.deq.valid
    } ++
    List(true.B, true.B)
  oneHotWire := Cat (oneHot)
  outReadyList := Cat(oneHotMap)

  //加在了最低位还是最高位，要测试一下哈
    io.CGRAIO.ready := Mux1H(
      oneHot,
      oneHotMap
    )
//  io.CGRAIO.ready := synInInst.io.readySel


  //  (0 until synInInst.io.dataIn.size).map{
  //    i => synInInst.io.dataIn(i).valid := io.CGRAIO.addr === ("h02010000".U + (i*dataW/8 ).U) && io.CGRAIO.valid
  //  }
  synInInst.io.valid := io.CGRAIO.valid && io.CGRAIO.wen && addrIsW
  (0 until outQueue.size).map {
    i => outQueue(i).io.deq.ready := io.CGRAIO.addr === ("h02010000".U + (8 * 8 + i * 4).U) && io.CGRAIO.valid
  }
  cgraInst.io.cfgEn := io.CGRAIO.addr === ("h02010000".U + (cgraParam.colNum * cgraParam.width * 2 / 8).U) && io.CGRAIO.valid
  synInInst.io.delayen := io.CGRAIO.addr === ("h02010000".U + (cgraParam.colNum * cgraParam.width * 2 / 8 + dataW / 8).U) && io.CGRAIO.valid



  //  (0 until synInInst.io.dataIn.size).map{i=>
  //    synInInst.io.dataIn(i).dataIn := io.CGRAIO.data_write
  //  }
  synInInst.io.dataIn := io.CGRAIO.data_write
  cgraInst.io.cfgData := io.CGRAIO.data_write(31, 0)
  cgraInst.io.cfgAddr := io.CGRAIO.data_write(63, 32)
  synInInst.io.delayCycle := io.CGRAIO.data_write

  io.CGRAIO.data_read := Mux1H(
    oneHList2,
    outQueue.map { i => i.io.deq.bits }
  )
}