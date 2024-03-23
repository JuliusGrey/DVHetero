//package RISCV.DMA
//import chisel3.util._
//import chisel3._
//import chisel3.util.experimental.BoringUtils
//import RISCV.common.Interface.{AXIIO, cpuRWIO}
//import RISCV.common.Param.{AXIID, addrW, dataW}
//class DMACtrl extends Module{
//  val io = IO(new Bundle {
//    val dataIn = new AXIIO
//    val dataOutMMIO = Flipped (new cpuRWIO(dataW , addrW ))
//  })
//
//  val dmaCtrl = Wire(UInt((dataW*3 ).W))
//  dmaCtrl := 0.U
//  BoringUtils.addSink(dmaCtrl, "dmaCtrl")
//  val dmaSrcAddrR = dmaCtrl( dataW/2 - 1,0 ).asUInt()
//  val dmaSrcAddrW = dmaCtrl( dataW - 1,dataW/2 ).asUInt()
//  val dmaDstAddrR = dmaCtrl( 3*dataW/2- 1,dataW ).asUInt()
//  val dmaDstAddrW = dmaCtrl( 2*dataW - 1,3*dataW/2 ).asUInt()
//  val dmaDstLenR = dmaCtrl( 5*dataW/2 - 1,dataW*2 ).asUInt()
//  val dmaDstLenW = dmaCtrl( 3*dataW - 1,5*dataW/2 ).asUInt()
//
//
//  val dmaSrcAddr = dmaCtrl(dataW - 1, 0).asUInt()
//  val dmaDstAddr = dmaCtrl(2 * dataW - 1, dataW).asUInt()
//  val dmaDstLen = dmaCtrl(3 * dataW - 1, dataW * 2).asUInt()
//
//
//
//
//
//
//
//
//  val dmaEn = Wire(Bool())
//  dmaEn:= false.B
//  BoringUtils.addSink(dmaEn, "dmaEn")
//
//
//  val dmaEnWR = Wire(Bool())
//  dmaEnWR := false.B
//  BoringUtils.addSink(dmaEnWR, "dmaEnWR")
//
//
//  val block2 = Wire(Bool())
//  block2 := false.B
//  BoringUtils.addSink(block2, "block2")
//
//  val block3 = Wire(Bool())
//  block3 := false.B
//  BoringUtils.addSink(block3, "block3")
//
//  val block =  block2 | block3
//
//  val changeState = RegInit(false.B)
//
//
//  //read
//  val rIdle :: rReq :: rData  ::rBlock :: Nil = Enum(4)
//  val rState = RegInit(rIdle)
//
//  val idleMux = Mux((dmaEn || dmaEnWR) && !changeState,rReq,rIdle)
//  val reqMux = Mux(
//    io.dataIn.arready && !changeState,
//    Mux(
//      io.dataIn.rlast,
//      Mux(
//        block && dmaEn,
//        rBlock,
//        rIdle),
//      rData
//    ),
//    rReq
//  )
//  val dataMux = Mux(
//    io.dataIn.rlast ,
//    Mux(
//      block&& dmaEn,
//      rBlock,
//      rIdle),
//    rData)
//  val blockMux = Mux(
//    block&& dmaEn,
//    rBlock,
//    rIdle
//  )
//
//  rState := MuxLookup(
//    rState,
//    rState,
//    Array(
//      rIdle -> idleMux,
//      rReq -> reqMux,
//      rData -> dataMux,
//      rBlock -> blockMux,
//    )
//  )
//
//  val isIdle = rState === rIdle
//  val isReq = rState === rReq
//  val isData = rState === rData
//  val isBlock = rState === rBlock
//
//
//  io.dataIn.arvalid := isReq
//  io.dataIn.araddr := Mux(dmaEn,dmaSrcAddr,  dmaSrcAddrR)
//  io.dataIn.arid := AXIID.U
//  io.dataIn.arlen :=Mux(dmaEn, dmaDstLen(7, 0).asUInt(), dmaDstLenR(7, 0).asUInt())
//  io.dataIn.arsize := 3.U
//  io.dataIn.arburst := 1.U(2.W)
//
//  io.dataIn.rready := (isReq || isData) && io.dataOutMMIO.ready
//
//
//
//  //write
//  val wCnt  = RegInit(0.U(4.W))
//  val wIdle :: wReq :: wData :: wB::wBlock :: Nil = Enum(5)
//  val wState = RegInit(wIdle)
//
//
//  val isWIdle = wState === wIdle
//  val isWReq = wState === wReq
//  val isWData = wState === wData
//  val isWB = wState === wB
//  val isWBlock = wState === wBlock
//
//  val wIdleMux = Mux(
//    dmaEnWR && changeState ,
//    wReq,
//    wIdle
//  )
//  val wReqMux = MuxLookup(
//    Cat(io.dataIn.awready ,  io.dataIn.wready && io.dataIn.wlast && io.dataOutMMIO.ready),
//    wReq,
//    Array(
//      "b10".U(2.W) -> wData,
//      "b11".U(2.W) -> wB
//    )
//  )
//  val wDataMux = Mux(io.dataIn.wready && io.dataIn.wlast && io.dataOutMMIO.ready,wB,wData)
//  val wBMux = Mux(
//    io.dataIn.bvalid,
//    Mux(
//      block,
//      wBlock,
//      wIdle),
//    wB)
//  val wBlockMux = Mux(
//    block,
//    wBlock,
//    wIdle
//  )
//  wState := MuxLookup(
//    wState,
//    wState,
//    Array(
//      wIdle -> wIdleMux,
//      wReq -> wReqMux,
//      wData -> wDataMux,
//      wB -> wBMux,
//      wBlock -> wBlockMux,
//    )
//  )
//
//  //写地址通道
//  io.dataIn.awvalid := isWReq
//  io.dataIn.awaddr := dmaSrcAddrW
//  io.dataIn.awid := AXIID.U
//  io.dataIn.awlen := dmaDstLenW(7, 0).asUInt()
//  io.dataIn.awsize := 3.U
//  io.dataIn.awburst := 1.U(2.W)
//
//
//  // 写数据通道
//
//  wCnt := Mux(
//    isWIdle ,
//    0.U ,
//    Mux(
//      io.dataOutMMIO.ready && (isWData || isWReq) && io.dataIn.wready,
//      wCnt + 1.U,
//      wCnt
//    )
//
//  )
//
//
//  io.dataIn.wvalid := io.dataOutMMIO.ready && (isWData || isWReq)
//  io.dataIn.wdata := io.dataOutMMIO.data_read
//  io.dataIn.wstrb := 255.U
//  io.dataIn.wlast := wCnt === dmaDstLenW
//
//  // 写应答通道
//  io.dataIn.bready := isWB
//
//
//
//
//  changeState := Mux(
//    changeState,
//    Mux(
//      isWB && io.dataIn.bvalid ,
//      false.B,
//      changeState
//
//    ),
//    Mux(
//      (isReq || isData) && io.dataIn.rlast && dmaEnWR,
//      true.B,
//      changeState
//    )
//  )
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//  val blockDMA = Wire(Bool())
//  blockDMA := (dmaEnWR && !io.dataIn.bvalid && !isWBlock)|| (dmaEn && !io.dataIn.rlast && !isBlock)
//  BoringUtils.addSource(blockDMA, "blockDMA")
//
//
//
//  val DMABuzy = isReq || isData ||isWReq || isWData || isWB
//  BoringUtils.addSource(DMABuzy, "DMABuzy")
//
//
//
//
//
//
//
//
//
//  val addrCnt = RegInit(0.U(addrW.W))
//  addrCnt := Mux(
//    isReq,
//    dmaDstAddrR,
//    Mux(
//      isData && io.dataIn.rvalid && io.dataOutMMIO.ready,
//      addrCnt + 8.U,
//      addrCnt
//    )
//  )
//  io.dataOutMMIO.addr :=Mux(dmaEn,dmaDstAddr, Mux(isWReq || isWData ,dmaDstAddrW,  addrCnt))
//
//  io.dataOutMMIO.wen := isData && io.dataIn.rvalid
//
//  io.dataOutMMIO.rsize := 3.U
//  io.dataOutMMIO.mask := "hff".U
//
//  io.dataOutMMIO.valid := ((isReq || isData) && io.dataIn.rvalid) || ((isWReq || isWData))
//
//  io.dataOutMMIO.data_write := io.dataIn.rdata
//
//
//
//}
