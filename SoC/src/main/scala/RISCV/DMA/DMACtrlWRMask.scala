package RISCV.DMA

import chisel3.util._
import chisel3._
import chisel3.util.experimental.BoringUtils
import RISCV.common.Interface.{AXIIO, cpuRWIO}
import RISCV.common.Param.{AXIID, addrW, dataW}
import TOP.paramG.{cgraBaseAddr, cgraOutAddr, cgraOutPort, cgraWidth}
//这块先这样吧，测试正确而时候再改一下配置地址和写数据地址，现在先单次测试
class DMACtrl extends Module{
  val io = IO(new Bundle {
    val dataIn = new AXIIO(64,32,4)
    val dataOutMMIO = Flipped (new cpuRWIO(dataW , addrW ))
  })

  val dmaCtrl = Wire(UInt((dataW*3 ).W))
  dmaCtrl := 0.U
  BoringUtils.addSink(dmaCtrl, "dmaCtrl")
  val dmaEn = Wire(Bool())
  dmaEn := false.B
  BoringUtils.addSink(dmaEn, "dmaEn")


  val dmaEnWR = Wire(Bool())
  dmaEnWR := false.B
  BoringUtils.addSink(dmaEnWR, "dmaEnWR")
  val dmaEnWRL = RegNext(dmaEnWR)
  val dmaWRStart = dmaEnWR&& !dmaEnWRL


  val block2 = Wire(Bool())
  block2 := false.B
  BoringUtils.addSink(block2, "block2")

  val block3 = Wire(Bool())
  block3 := false.B
  BoringUtils.addSink(block3, "block3")

  val block = block2 | block3



  val dmaAXIAddrR = dmaCtrl( dataW/2 - 1,0 ).asUInt()
  val dmaAXIAddrW = dmaCtrl( dataW - 1,dataW/2 ).asUInt()
//  val dmaDstAddrR = dmaCtrl( 3*dataW/2- 1,dataW ).asUInt()
  val dmaCGRAOutMask = dmaCtrl( 2*dataW - 1,3*dataW/2 ).asUInt()
  val dmaLenR = dmaCtrl( 5*dataW/2 - 1,dataW*2 ).asUInt()
  val dmaLenW = dmaCtrl( 3*dataW - 1,5*dataW/2 ).asUInt()

  val dmaLenRBatch = dmaLenR(dataW/4 - 1 , 0)
  val dmaLenRTotal = dmaLenR(dataW/2 - 1 , dataW/4)

  val dmaLenWBatch = dmaLenW(dataW / 4 - 1, 0)
  val dmaLenWTotal = dmaLenW(dataW / 2 - 1, dataW / 4)





  val dmaAXIAddr = dmaCtrl(dataW - 1, 0).asUInt()
  val dmaDstAddr = dmaCtrl(2 * dataW - 1, dataW).asUInt()
  val dmaLen = dmaCtrl(3 * dataW - 1, dataW * 2).asUInt()
  val isCfg = dmaDstAddr === "h02010080".U

  val CGRAOutAddr = Wire(UInt((dataW/2).W))

  val portArbitInst = Module(new adresPortArbit(cgraOutPort))
  val addrList = (0 until cgraOutPort).map { i => cgraOutAddr + (i * cgraWidth/8).U }
  for(i <- 0 until cgraOutPort){
    portArbitInst.io.in(i).valid := dmaCGRAOutMask(i)
    portArbitInst.io.in(i).address := addrList(i)
  }
  portArbitInst.io.dmaEn := dmaEnWR
  CGRAOutAddr := portArbitInst.io.out











  val changeState = RegInit(false.B)//用来表示由读数据的状态切换到写数据的状态


  val dmaLenRResiWire = Wire(UInt((dataW / 4).W))
  val dmaLenRResi = RegNext(
    Mux(
      dmaWRStart,
      dmaLenRTotal,
      Mux(
        io.dataIn.arvalid && io.dataIn.arready,
        Mux(
          dmaLenRResiWire > dmaLenRBatch,
          dmaLenRResiWire - dmaLenRBatch,
          0.U
        ),
        dmaLenRResiWire
      )
    ),0.U((dataW / 4).W)
  )
  dmaLenRResiWire := dmaLenRResi

  //read
  val rIdle :: rReq :: rData  ::rBlock :: Nil = Enum(4)
  val rState = RegInit(rIdle)
  val dmaNext = RegNext(dmaEn || dmaEnWR)
  val idleMux = Mux((dmaEn || dmaEnWR)&& (!dmaNext || (dmaLenRResiWire =/= 0.U)) && !changeState,rReq,rIdle)
  val reqMux = Mux(
    io.dataIn.arready && !changeState,
    Mux(
      io.dataIn.rlast,
      Mux(
        block && dmaEn,
        rBlock,
        rIdle),
      rData
    ),
    rReq
  )
  val dataMux = Mux(
    io.dataIn.rlast ,
    Mux(
      block&& dmaEn,
      rBlock,
      rIdle),
    rData)
  val blockMux = Mux(
    block&& dmaEn,
    rBlock,
    rIdle
  )

  rState := MuxLookup(
    rState,
    rState,
    Array(
      rIdle -> idleMux,
      rReq -> reqMux,
      rData -> dataMux,
      rBlock -> blockMux,
    )
  )

  val isIdle = rState === rIdle
  val isReq = rState === rReq
  val isData = rState === rData
  val isBlock = rState === rBlock


//  val dmaLenRResi = Mux(
//    dmaWRStart,
//    dmaLenR,
//    Mux(
//      io.dataIn.rlast,
//      0.U,
//      Mux(
//        io.dataIn.rready && io.dataIn.rvalid,
//        dmaLenRResi > 8.U,
//
//        dmaLenRResi
//      )
//    )
//  )


  val dmaRBaseAddrWire = Wire(UInt((dataW/2).W))

  val dmaRBaseAddr = RegNext(
    Mux(
      dmaWRStart,
      dmaAXIAddrR,
      Mux(
        io.dataIn.arvalid && io.dataIn.arready,
        Mux(
          dmaLenRResiWire > dmaLenRBatch,
          dmaRBaseAddrWire + (dmaLenRBatch<<3),
          0.U
        ),
        dmaRBaseAddrWire
      )
    ),0.U
  )
  dmaRBaseAddrWire := dmaRBaseAddr


    io.dataIn.arvalid := isReq
  io.dataIn.araddr := Mux(dmaEn,dmaAXIAddr,  dmaRBaseAddrWire)
  io.dataIn.arid := AXIID.U
  io.dataIn.arlen :=Mux(dmaEn, dmaLen(7, 0).asUInt(),
    Mux(
      dmaLenRResiWire > dmaLenRBatch,
      dmaLenRBatch,
      dmaLenRResiWire
    )) - 1.U
  io.dataIn.arsize := 3.U
  io.dataIn.arburst := 1.U(2.W)

  io.dataIn.rready := (isReq || isData) && io.dataOutMMIO.ready








  //write
  val dmaLenWResiWire = Wire(UInt((dataW / 4).W))
  val dmaLenWResi = RegNext(
    Mux(
      dmaWRStart,
      dmaLenWTotal,
      Mux(
//        io.dataIn.awvalid && io.dataIn.awready,//这里要改的
        io.dataIn.wlast,
        Mux(
          dmaLenWResiWire > dmaLenWBatch,
          dmaLenWResiWire - dmaLenWBatch,
          0.U
        ),
        dmaLenWResiWire
      )
    ),0.U
  )
  dmaLenWResiWire := dmaLenWResi

  val firstBatch = dmaLenWResi === dmaLenWTotal




  val wCnt  = RegInit(0.U(9.W))
  val wIdle :: wReq :: wData :: wB::wBlock :: Nil = Enum(5)
  val wState = RegInit(wIdle)


  val isWIdle = wState === wIdle
  val isWReq = wState === wReq
  val isWData = wState === wData
  val isWB = wState === wB
  val isWBlock = wState === wBlock

  val wIdleMux = Mux(
    dmaEnWR && changeState ,
    wReq,
    wIdle
  )
  val wReqMux = MuxLookup(
    Cat(io.dataIn.awready ,  io.dataIn.wready && io.dataIn.wlast && io.dataOutMMIO.ready),
    wReq,
    Array(
      "b10".U(2.W) -> wData,
      "b11".U(2.W) -> wB
    )
  )
  val wDataMux = Mux(io.dataIn.wready && io.dataIn.wlast && io.dataOutMMIO.ready,wB,wData)
  val wBMux = Mux(
    io.dataIn.bvalid,
    Mux(
      block && dmaLenWResiWire === 0.U,
      wBlock,
      wIdle),
    wB)
  val wBlockMux = Mux(
    block,
    wBlock,
    wIdle
  )
  wState := MuxLookup(
    wState,
    wState,
    Array(
      wIdle -> wIdleMux,
      wReq -> wReqMux,
      wData -> wDataMux,
      wB -> wBMux,
      wBlock -> wBlockMux,
    )
  )





  val dmaWBaseAddrWire = Wire(UInt((dataW / 2).W))

  val dmaWBaseAddr = RegNext(
    Mux(
      dmaWRStart,
      dmaAXIAddrW,
      Mux(
        io.dataIn.awvalid && io.dataIn.awready,
        Mux(
          dmaLenWResiWire > dmaLenWBatch,
          dmaWBaseAddrWire + (dmaLenWBatch << 3),
          0.U
        ),
        dmaWBaseAddrWire
      )
    ),0.U
  )
  dmaWBaseAddrWire := dmaWBaseAddr




  //写地址通道
  io.dataIn.awvalid := isWReq
  io.dataIn.awaddr := dmaWBaseAddrWire
  io.dataIn.awid := AXIID.U
  io.dataIn.awlen := Mux(
    dmaLenWResiWire > dmaLenWBatch,
    dmaLenWBatch,
    dmaLenWResiWire
  )-1.U
  io.dataIn.awsize := 3.U
  io.dataIn.awburst := 1.U(2.W)


  // 写数据通道

  wCnt := Mux(
    isWIdle ,
    0.U ,
    Mux(
      io.dataOutMMIO.ready && (isWData || isWReq) && io.dataIn.wready,
      wCnt + 1.U,
      wCnt
    )

  )


  io.dataIn.wvalid := io.dataOutMMIO.ready && (isWData || isWReq)
  io.dataIn.wdata := io.dataOutMMIO.data_read
  io.dataIn.wstrb := 255.U
  io.dataIn.wlast := isWData &&io.dataOutMMIO.ready&&wCnt === Mux(
    dmaLenWResiWire > dmaLenWBatch,
    dmaLenWBatch,
    dmaLenWResiWire
  ) - 1.U
  dontTouch(dmaLenWBatch)
  dontTouch(dmaLenWResiWire)
  dontTouch(dmaLenW)




  // 写应答通道
  io.dataIn.bready := isWB

  portArbitInst.io.regEn :=  (io.dataOutMMIO.ready && (isWData || isWReq) && io.dataIn.wready)
  portArbitInst.io.batchBgein :=(firstBatch && isWIdle && dmaEnWR && changeState)

  io.dataOutMMIO.ready && (isWData || isWReq)


  changeState := Mux(
    changeState,
    Mux(
      isWB && io.dataIn.bvalid ,
      false.B,
      changeState

    ),
    Mux(
      (isReq || isData) && io.dataIn.rlast && dmaEnWR,
      true.B,
      changeState
    )
  )














  val blockDMA = Wire(Bool())
  blockDMA := (dmaEnWR && !io.dataIn.bvalid && !isWBlock)|| (dmaEn && !io.dataIn.rlast && !isBlock) || dmaLenRResiWire =/= 0.U|| dmaLenWResiWire =/= 0.U
  BoringUtils.addSource(blockDMA, "blockDMA")
//这个是用来阻塞所有寄存器的


  val DMABuzy = isReq || isData ||isWReq || isWData || isWB
  BoringUtils.addSource(DMABuzy, "DMABuzy")









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

  io.dataOutMMIO.addr :=Mux(dmaEn && isCfg,dmaDstAddr, Mux(isWReq || isWData ,CGRAOutAddr,  cgraBaseAddr))

  io.dataOutMMIO.wen := isData && io.dataIn.rvalid

  io.dataOutMMIO.rsize := 3.U
  io.dataOutMMIO.mask := "hff".U

  io.dataOutMMIO.valid := ((isReq || isData) && io.dataIn.rvalid) || ((isWReq || isWData))
//pigfly 这里有问题，如果w状态中，axi模拟器没有ready会怎样
  io.dataOutMMIO.data_write := io.dataIn.rdata



}


