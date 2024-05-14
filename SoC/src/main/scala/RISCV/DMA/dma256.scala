package RISCV.DMA
import RISCV.common.Interface.{AXIIO, cpuRWIO}
import RISCV.common.Param.{AXIID, dataW}
import TOP.paramG._
import chisel3.util._
import chisel3._
import chisel3.util.experimental.BoringUtils
class dma256 extends Module{
  val io = IO(new Bundle {
  val dataAXI = new AXIIO(256,32,4)
  val dataCGRA = Flipped (new cpuRWIO(256 , 32 ))

    val cgraInOver8 = Input(Bool())
    val cgraOutOver8 = Input(Bool())

    val cgraOutSignle = Input(Bool())
    val batchOver = Output(Bool())
    val batchLastNum = Output(UInt(4.W))
})

  val dmaEnOut = Wire(Bool())
  dmaEnOut := false.B
  BoringUtils.addSink(dmaEnOut, "dmaEn")
  val dmaEn = RegNext(dmaEnOut)//需要将dmaEn延迟一拍，这样over8才能起作用
  val dmaEnL = RegNext(dmaEn)
  val dmaStart = dmaEn && !dmaEnL

  val dmaEnWROut = Wire(Bool())
  dmaEnWROut := false.B
  BoringUtils.addSink(dmaEnWROut, "dmaEnWR")
  val dmaEnWR = RegNext(dmaEnWROut)
  val dmaEnWRL = RegNext(dmaEnWR)
  val dmaWRStart = dmaEnWR && !dmaEnWRL

  val dmaCtrl = Wire(UInt((dataW * 3).W))
  dmaCtrl := 0.U
  BoringUtils.addSink(dmaCtrl, "dmaCtrl")

  val block2 = Wire(Bool())
  block2 := false.B
  BoringUtils.addSink(block2, "block2")
  val block3 = Wire(Bool())
  block3 := false.B
  BoringUtils.addSink(block3, "block3")
  val block = block2 | block3


  /*dmaWR的解析*/
  val dmaAXIAddrR = dmaCtrl(dataW / 2 - 1, 0).asUInt()
  val dmaAXIAddrW = dmaCtrl(dataW - 1, dataW / 2).asUInt()
  val dmaLenR = dmaCtrl(5 * dataW / 2 - 1, dataW * 2).asUInt()
  val dmaLenW = dmaCtrl(3 * dataW - 1, 5 * dataW / 2).asUInt()
  val dmaNumRBatch = dmaLenR(dataW / 4 - 1, 0).asUInt()
  val dmaNumRTotal = dmaLenR(dataW / 2 - 1, dataW / 4).asUInt()
  val dmaNumWBatch = dmaLenW(dataW / 4 - 1, 0).asUInt()
  val dmaNumWTotal = dmaLenW(dataW / 2 - 1, dataW / 4).asUInt()
  val dmaLenRBatch = Mux(io.cgraInOver8,dmaNumRBatch<<1,dmaNumRBatch).asUInt()
  val dmaLenRTotal = Mux(io.cgraInOver8,dmaNumRTotal<<1,dmaNumRTotal).asUInt()
  val dmaLenWBatch = Mux(io.cgraOutOver8,dmaNumWBatch<<1,dmaNumWBatch).asUInt()
  val dmaLenWTotal = Mux(io.cgraOutOver8,dmaNumWTotal<<1,dmaNumWTotal).asUInt()



 /*单向dma解析*/
  val oneDiAXIAddr = dmaCtrl(dataW/2 - 1, 0).asUInt()
  val judgeType = dmaCtrl( dataW - 1,  dataW/2).asUInt() //0 : cfg ; 1: mem -> CGRA ; 2 :CGRA ->mem
  val cfgLen = dmaCtrl(3 * dataW - 1, dataW * 2).asUInt()
  val oneDiNumBatch = dmaCtrl(dataW * 2 + dataW / 4 - 1, dataW * 2).asUInt()
  val oneDiNumTotal = dmaCtrl(5 * dataW/2 + dataW / 4 - 1, 5 * dataW/2).asUInt()
  val oneDiLenBatchMem2CGAR = Mux(io.cgraInOver8, oneDiNumBatch << 1, oneDiNumBatch).asUInt()
  val oneDiLenTotalMem2CGAR = Mux(io.cgraInOver8, oneDiNumTotal << 1, oneDiNumTotal).asUInt()
  val oneDiLenBatchCGAR2Mem = Mux(io.cgraOutOver8, oneDiNumBatch << 1, oneDiNumBatch).asUInt()
  val oneDiLenTotalCGRA2Mem = Mux(io.cgraOutOver8, oneDiNumTotal << 1, oneDiNumTotal).asUInt()

  val isCfg = judgeType === 0.U && dmaEn
  val isMem2CGRA = judgeType === 1.U && dmaEn
  val isCGRA2Mem = judgeType === 2.U && dmaEn




  /*1.cfg*/
  val cfgIdle :: cfgReq :: cfgData  ::cfgBlock :: Nil = Enum(4)
  val cfgState = RegInit(cfgIdle)
  val isCfgIdle = cfgState === cfgIdle
  val isCfgReq = cfgState === cfgReq
  val isCfgData = cfgState === cfgData
  val isCfgBlock = cfgState === cfgBlock
  val cfgIdleMux = Mux( isCfg && dmaEnOut,cfgReq,cfgIdle)
  val cfgReqMux = Mux(
    io.dataAXI.arready,
    Mux(
      io.dataAXI.rlast,
        cfgBlock,
      cfgData
    ),
    cfgReq
  )
  val cfgDataMux = Mux(
    io.dataAXI.rlast,
      cfgBlock,
    cfgData
  )
  val cfgBlockMux = Mux(
    block,
    cfgBlock,
    cfgIdle
  )
  cfgState := MuxLookup(
    cfgState ,
    cfgState,
    Array(
      cfgIdle -> cfgIdleMux,
      cfgReq -> cfgReqMux,
      cfgData -> cfgDataMux,
      cfgBlock -> cfgBlockMux,
    )
  )





  /*2.mem2cgra and cgra2mem*/
  val changeState = Wire(Bool())


  /*2.1 mem2cgra*/

  val rLenBatch = Mux(dmaEnWR, dmaLenRBatch, oneDiLenBatchMem2CGAR)
  val rLenTotal = Mux(dmaEnWR , dmaLenRTotal , oneDiLenTotalMem2CGAR)
  val rAddrBase = Mux(dmaEnWR, dmaAXIAddrR, oneDiAXIAddr)

  val dmaLenRResi = Wire(UInt((dataW / 4).W))
  val dmaLenRResiReg = RegEnable(
      Mux(
        (dmaStart && isMem2CGRA) || dmaWRStart,
        rLenTotal,
        Mux(
          dmaLenRResi > rLenBatch,
          dmaLenRResi - rLenBatch,
          0.U
        ),
      ),
    0.U((dataW / 4).W),
    (dmaStart && isMem2CGRA) || dmaWRStart || (io.dataAXI.arvalid && io.dataAXI.arready && !isCfg)
  )
  dmaLenRResi := dmaLenRResiReg

  val dmaRAddr = Wire(UInt((dataW / 2).W))
  val dmaRAddrReg = RegEnable(
      Mux(
        (dmaStart&&isMem2CGRA) ||dmaWRStart ,
        rAddrBase,
        Mux(
          dmaLenRResi > rLenBatch,
          dmaRAddr + (rLenBatch << 5),
          0.U
        ),
      ),
    0.U((dataW / 2).W),
    (dmaStart&&isMem2CGRA) ||dmaWRStart || (io.dataAXI.arvalid && io.dataAXI.arready&& !isCfg)
  )
  dmaRAddr := dmaRAddrReg

  val rIdle :: rReq :: rData ::rBlock  :: Nil = Enum(4)
  val rState = RegInit(rIdle)
  val isIdleR = rState === rIdle
  val isReqR = rState === rReq
  val isDataR = rState === rData
  val isBlockR = rState === rBlock


  val rIdleMux = Mux(
   ( dmaEnWROut && dmaEnWR && (!dmaEnWRL || (dmaLenRResi =/= 0.U)) && !changeState)||(dmaEnOut && isMem2CGRA && (!dmaEnL || (dmaLenRResi =/= 0.U))),//需要原来没有dmaEnOut，后来加上了。由于dmaen事emaenout延迟一拍，所以阻塞结束后dmaen依然是1，状态会变；需要加一个dmaEnOut，该信号和外部阻塞同步
    rReq, rIdle)
  assert(!(dmaLenRResi =/= 0.U && !(dmaEnWR || isMem2CGRA)))
  val rReqMux = Mux(
    io.dataAXI.arready ,
    Mux(
      io.dataAXI.rlast,
      Mux(
        isMem2CGRA && (dmaLenRResi === 0.U),
        rBlock,
        rIdle
      ),
      rData
    ),
    rReq
  )
  val rDataMux = Mux(
    io.dataAXI.rlast,
    Mux(
      isMem2CGRA && (dmaLenRResi === 0.U),
      rBlock,
      rIdle
    ), rData)
  val rBlockMux = Mux(
    block ,
    rBlock,
    rIdle
  )

  rState := MuxLookup(
    rState,
    rState,
    Array(
      rIdle -> rIdleMux,
      rReq -> rReqMux,
      rData -> rDataMux,
      rBlock -> rBlockMux,
    )
  )




  /* 2.2 cgra2mem*/
  val wLenBatch = Mux(dmaEnWR,dmaLenWBatch,oneDiLenBatchCGAR2Mem)
  val wLenTotal = Mux(dmaEnWR,dmaLenWTotal,oneDiLenTotalCGRA2Mem)
  val wAddrBase = Mux(dmaEnWR,dmaAXIAddrW,oneDiAXIAddr)

  val dmaLenWResi = Wire(UInt((dataW / 4).W))
  val dmaLenWResiReg = RegEnable(
      Mux(
        (isCGRA2Mem&&dmaStart)||dmaWRStart,
        wLenTotal,
        Mux(
          dmaLenWResi > wLenBatch,
          dmaLenWResi - wLenBatch,
          0.U
        )
      ),
    0.U,
    (isCGRA2Mem&&dmaStart)||dmaWRStart || (io.dataAXI.wlast && io.dataAXI.wready && io.dataAXI.wvalid)
  )
  dmaLenWResi := dmaLenWResiReg


  val wLen = Mux(
    dmaLenWResi > wLenBatch,
    wLenBatch,
    dmaLenWResi
  )
  val sigLastNum = wLen(2, 0).asUInt()
  val sigWLen = (wLen >> 3).asUInt() + Mux(sigLastNum === 0.U, 0.U, 1.U)






  val dmaWAddr = Wire(UInt((dataW / 2).W))
  val dmaWAddrReg = RegEnable(
    Mux(
      dmaWRStart||(isCGRA2Mem&&dmaStart),
      wAddrBase,
      Mux(
        dmaLenWResi > wLenBatch,
        dmaWAddr + (Mux(
          io.cgraOutSignle,
          sigWLen,
          wLenBatch
        ) << 5),
        0.U
      )
    ),
    0.U,
    (isCGRA2Mem&&dmaStart)||dmaWRStart || (io.dataAXI.awvalid && io.dataAXI.awready)
  )
  dmaWAddr := dmaWAddrReg


  val wIdle :: wReq :: wData :: wB :: wBlock :: Nil = Enum(5)
  val wState = RegInit(wIdle)
  val isWIdle = wState === wIdle
  val isWReq = wState === wReq
  val isWData = wState === wData
  val isWB = wState === wB
  val isWBlock = wState === wBlock

  val wIdleMux = Mux(
    (dmaEnWROut&& dmaEnWR && changeState)||(dmaEnOut && isCGRA2Mem &&(!dmaEnL || (dmaLenWResi =/=0.U))),//
    wReq,
    wIdle
  )



  val wReqMux = Mux(
    io.dataAXI.awready,
    Mux(
      io.dataAXI.wready && io.dataAXI.wlast && io.dataCGRA.ready,
      wB,
      wData,
    ),
    wReq
  )
  val wDataMux = Mux(io.dataAXI.wready && io.dataAXI.wlast && io.dataCGRA.ready,wB,wData)
  val wBMux = Mux(
    io.dataAXI.bvalid,
    Mux(
      dmaLenWResi === 0.U ,
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

  val wCnt = RegInit(0.U(9.W))
  wCnt := Mux(
    isWIdle,
    0.U,
    Mux(
      io.dataCGRA.ready && (isWData || isWReq) && io.dataAXI.wready,
      wCnt + 1.U,
      wCnt
    )
  )


  changeState := RegEnable(
    Mux(
      isWB && io.dataAXI.bvalid,
      false.B,
      true.B
    ),
    false.B,
    (((isReqR || isDataR) && io.dataAXI.rlast) || (isWB && io.dataAXI.bvalid)) && dmaEnWR
  )



  //cfg
  io.dataAXI.arvalid := isCfgReq || isReqR
  io.dataAXI.araddr := Mux(isCfg, oneDiAXIAddr, dmaRAddr)
  io.dataAXI.arid := AXIID.U
  io.dataAXI.arlen := Mux(
    isCfg,
    cfgLen(7, 0).asUInt(),
    Mux(
      dmaLenRResi > rLenBatch,
      rLenBatch,
      dmaLenRResi
    )
  ) -1.U
  io.dataAXI.arsize := 5.U
  io.dataAXI.arburst := 1.U(2.W)
  io.dataAXI.rready := (isCfgReq || isCfgData||isReqR || isDataR) && io.dataCGRA.ready


  io.dataAXI.awvalid := isWReq
  io.dataAXI.awaddr := dmaWAddr
  io.dataAXI.awid := AXIID.U
  io.dataAXI.awlen := Mux(
    io.cgraOutSignle,
    sigWLen,
    wLen
  ) - 1.U
  io.dataAXI.awsize := 5.U
  io.dataAXI.awburst := 1.U
  io.dataAXI.wvalid := io.dataCGRA.ready && (isWData || isWReq)
  io.dataAXI.wdata := io.dataCGRA.data_read
  io.dataAXI.wstrb := "hffffffff".U


//  io.dataAXI.wlast := (isWData||isWReq) &&  wCnt === Mux(
//      io.cgraOutSignle,
//      sigWLen,
//  Mux(
//    dmaLenWResi > wLenBatch,
//    wLenBatch,
//    dmaLenWResi
//  )) - 1.U

  io.dataAXI.wlast := (isWData || isWReq) && wCnt === Mux(
    io.cgraOutSignle,
    sigWLen,
    wLen) - 1.U

//  wLen


  io.dataAXI.bready := isWB

  io.dataCGRA.addr :=  Mux(
    isCfg,
    addrCfg256,
    Mux(
      isDataR || isReqR,
      addrIn256,
      Mux(
        isWReq || isWData,
        addrOut256,
        0.U
      )
    )
  )
  io.dataCGRA.wen := (isCfgReq || isCfgData|| isDataR || isReqR) && io.dataAXI.rvalid
  io.dataCGRA.rsize := DontCare
  io.dataCGRA.mask := DontCare
  io.dataCGRA.valid := ((isCfgReq || isCfgData || isDataR || isReqR) && io.dataAXI.rvalid)||((isWReq || isWData) && io.dataAXI.wready)
  io.dataCGRA.data_write := io.dataAXI.rdata

  io.batchOver := io.dataAXI.wlast
  io.batchLastNum := sigLastNum


  val blockDMA = Wire(Bool())
  blockDMA := (dmaEnWR || dmaEn) && (!isCfgBlock && !isWBlock && !isBlockR)
  BoringUtils.addSource(blockDMA, "blockDMA")
    assert(!(dmaEnWR && isBlockR))


  //mem2cgra
  //val awvalidW = isWReq
  //  val awaddrW = dmaWBaseAddr
  //  val awidW = AXIID.U
  //  val awLen = Mux(
  //    io.cgraOutSignle,
  //    sigWLen,
  //    wLen
  //  ) - 1.U
  //  val awsizeW = 5.U
  //  val awburstW = 1.U(2.W)
  //  val wvalidW = io.dataCGRA.ready && (isWData || isWReq)
  //  val wdataW = io.dataCGRA.data_read
  //  val wstrbW = "hffffffff".U
  //  val wlastW = isWData && io.dataCGRA.ready && wCnt === Mux(
  //    dmaLenWResi > wLenBatch,
  //    wLenBatch,
  //    dmaLenWResi
  //  ) -1.U
  //  val breadyW = isWB
  //
  //
  //  val ioAddrW = addrOut256
  //  val ioEnW = false.B
  //  val ioRsizeW = DontCare
  //  val ioMaskW = DontCare
  //  val ioValidW = (isWReq || isWData) && io.dataAXI.wready
  //
  //  val batchOver  = io.dataAXI.wlast
  //  val batchLastNum = sigLastNum


  //cgra2mem
//  val arvalidR = isReqR
//  val araddrR = dmaRBaseAddr
//  val aridR = AXIID.U
//  val arlenR = Mux(dmaLenRResi > rLenBatch, rLenBatch, dmaLenRResi) - 1.U
//  val arsizeR = 5.U
//  val arburstR = 1.U(2.W)
//  val rreadyR = (isReqR || isDataR) && io.dataCGRA.ready
//
//  val ioAddrR = addrIn256
//  val ioEnR = isDataR && io.dataAXI.rvalid
//  val ioRsizeR = DontCare
//  val ioMaskR = DontCare
//  val ioValidR = (isReqR || isDataR) && io.dataAXI.rvalid
//  val ioDataWR = io.dataAXI.rdata

}

object dma256Gen extends App {

    chisel3.Driver.execute(args,() => new dma256 )



}
