package RISCV

import RISCV.EXU.execute
import RISCV.IDU.CtrlUnit.{CtrlUnit, csrCtrl, memVGen}
import RISCV.IDU.CtrlUnit.ResultSrcList.regFrom_ALU
import RISCV.IDU.iDecode
import RISCV.IFU.{ebProbe, iFetch}
import RISCV.TOPLEVEL.missReport
import RISCV.prebranch.preBranch
import RISCV.PIP.PipReg._
import RISCV.PIP.{difftest, hazard, intr, skip}
import chisel3._
import chisel3.util._
import chisel3.util.experimental.BoringUtils
import RISCV.common.Interface.{cpuRWIO, ex2out}
import RISCV.common.Param._
import RISCV.common.instList.{DMAEN,DMAENWR}

import scala.collection.generic.IdleSignalling.abort

class riscv extends Module{
  val io = IO(new Bundle {
    //    val inst = Input(UInt(dataW.W))
    //    val pc = Output(UInt(addrW.W))
    //    val dout = new ex2out
    //
    //    val we = Output(Bool())
    //    val mask = Output(UInt(8.W))
//    val diffport = Output(Bool())
    val instIO = Flipped (new cpuRWIO(dataW, addrW))
    val dataIO = Flipped (new cpuRWIO(dataW, addrW))
  })

  val ifu = Module(new iFetch)
  val idu = Module(new iDecode)
  val exu = Module(new execute)
  val hazardu = Module(new hazard)
  val preBranchIns = Module(new preBranch)

  val block1 = Wire(Bool())
  block1 := false.B
  BoringUtils.addSink(block1, "block1")
  val blockDMA = Wire(Bool())
  blockDMA := false.B
  BoringUtils.addSink(blockDMA, "blockDMA")

  val intrTimeCnt = Wire(Bool())
  intrTimeCnt := false.B
  BoringUtils.addSink(intrTimeCnt, "intrTimeCnt")




  io.instIO.wen := false.B
  io.instIO.valid := true.B && !block1 && !blockDMA
  io.instIO.data_write := 0.U
  io.instIO.mask := 0.U
  val block2 = Wire(Bool())
  block2 := true.B && !io.instIO.ready
  BoringUtils.addSource(block2, "block2")

  val memVGenInst =  Module(new memVGen)




  val ctrl = Module(new CtrlUnit)
  val csrCtrl = Module(new csrCtrl)


  // 开始加流水线
  val pipFlashWire =Wire(Bool())
  val pipBlock  =Wire(Bool())

//  val pipBlockWire = Wire(Bool())

  //一些流水线信号


  ifu.io.instIn := Mux(ifu.io.pc(2), io.instIO.data_read(dataW - 1, instW), io.instIO.data_read(instW - 1, 0))
  io.instIO.addr := ifu.io.pc
  io.instIO.rsize :=  2.U
  //IF2ID流水线
//  val cmdIF2IDWire = Wire(Bool())
//  val pcIF2IDWire =Wire (UInt(addrW.W))
//  val snpcIF2IDWire =Wire (UInt(addrW.W))
  val pipIF2IDWire = Wire( new IF2ID)
  withReset((pipFlashWire && !pipBlock)|| reset.asBool()) {
    val pipIF2IDReg = RegEnable(Cat(
      ifu.io.pc=/= 0.U,
      ifu.io.instOut,
      ifu.io.pc,
      ifu.io.snpc,
      preBranchIns.io.ifjump,
      preBranchIns.io.ifdnpc
    ), 0.U, !(pipBlock || hazardu.io.loadHazard) )

    pipIF2IDWire := pipIF2IDReg.asTypeOf(new IF2ID)

    memVGenInst.io.inst := pipIF2IDWire.inst
    csrCtrl.io.inst := pipIF2IDWire.inst
    ctrl.io.inst := pipIF2IDWire.inst
    idu.io.inst := pipIF2IDWire.inst


//    pcIF2IDWire := pipIF2IDWire.pc
//    snpcIF2IDWire := pipIF2IDWire.snpc
  }
//







//    exu.io.snpc := pipIF2IDWire.snpc

  //ID2EX
//  val cmdID2ExWire = Wire(Bool())
//val rdOutID2ExWire = Wire(UInt(5.W))
//  val memReadNumID2ExWire = Wire(UInt(3.W))
//  val validID2ExWire = Wire(Bool())
//  val maskID2ExWire = Wire(UInt(8.W))
//  val wenID2ExWire = Wire(Bool())
//  val jumpID2ExWire = Wire(Bool())
//  val branchID2ExWire = Wire(Bool())
//  val regEnID2ExWire = Wire(Bool())
//  val resultSrcID2ExWire = Wire(UInt(2.W))
//  val snpcID2ExWire = Wire(UInt(dataW.W))
  val pipID2ExWire = Wire(new ID2EX)
  val pipCSRRegWire = Wire(new CSRID2EX)
  withReset(((pipFlashWire || hazardu.io.loadHazard) && !pipBlock)||reset.asBool()) {
    val pipID2ExReg = RegEnable(Cat(
      pipIF2IDWire.cmd,
      pipIF2IDWire.inst,
      idu.io.dataEx.imme,
      idu.io.dataEx.dOut1,
      idu.io.dataEx.dOut2 ,
      idu.io.dataEx.rdDout,
      pipIF2IDWire.pc,
      pipIF2IDWire.snpc,
      idu.io.rdOut ,
      ctrl.io.CtrlS.ALUCtrl,
      ctrl.io.CtrlS.AluSrc1,
      ctrl.io.CtrlS.AluSrc2,
      ctrl.io.CtrlS.memReadNum,
      memVGenInst.io.valid ,
      ctrl.io.CtrlS.memWriteMask,
      ctrl.io.CtrlS.memWriteM,
      ctrl.io.CtrlS.dnpcSrc,
      ctrl.io.CtrlS.jump,
      ctrl.io.CtrlS.branch,
      ctrl.io.CtrlS.regEn,
      ctrl.io.CtrlS.ResultSrc,
      idu.io.rs1,
      idu.io.rs2,
      ctrl.io.CtrlS.fencei,
      pipIF2IDWire.ifjump,
      pipIF2IDWire.ifdnpc,
      pipIF2IDWire.inst === DMAEN,
      pipIF2IDWire.inst === DMAENWR
    ),0.U,!pipBlock)
//    printf("ctrl.io.CtrlS.jump, is %x\n",ctrl.io.CtrlS.jump)

    pipID2ExWire := pipID2ExReg.asTypeOf( new ID2EX)

    val dmaEn = Wire(Bool())
    dmaEn := pipID2ExWire.dma
    BoringUtils.addSource(dmaEn ,"dmaEn")

    val dmaEnWR = Wire(Bool())
    dmaEnWR := pipID2ExWire.dmaWR
    BoringUtils.addSource(dmaEnWR, "dmaEnWR")

//    cmdID2ExWire := pipID2ExWire.cmd
    exu.io.dataId.imme := pipID2ExWire.imme
    exu.io.dataId.dOut1 := pipID2ExWire.dOut1
    exu.io.dataId.dOut2 := pipID2ExWire.dOut2
    exu.io.dataId.rdDout := pipID2ExWire.rdDout
    exu.io.pc := pipID2ExWire.pc
//    snpcID2ExWire := pipID2ExWire.snpc
//    rdOutID2ExWire := pipID2ExWire.rd
    exu.io.ALUCtrl:= pipID2ExWire.ALUCtrl
    exu.io.AluSrc1 := pipID2ExWire.ALUSrc1
    exu.io.AluSrc2 := pipID2ExWire.ALUSrc2
//    memReadNumID2ExWire := pipID2ExWire.memReadNum
//    validID2ExWire := pipID2ExWire.valid
//    maskID2ExWire := pipID2ExWire.mask
//    wenID2ExWire := pipID2ExWire.memWriteM
    exu.io.dnpcSrc := pipID2ExWire.dnpcSrc
//    jumpID2ExWire := pipID2ExWire.jump
//    branchID2ExWire := pipID2ExWire.branch
//    regEnID2ExWire :=pipID2ExWire.regEn
//    resultSrcID2ExWire := pipID2ExWire.resultSrc


    hazardu.io.rs1IDEX := pipID2ExWire.rs1
    hazardu.io.rs2IDEX := pipID2ExWire.rs2
//    hazardu.io.rdIDEX := pipID2ExWire.rd


    //csrID2EX
    val pipCSRReg = RegEnable(Cat(
      csrCtrl.io.CSRCtrlIf.csrrwen,
      csrCtrl.io.CSRCtrlIf.csrswen,
      csrCtrl.io.CSRCtrlIf.csrrsien,
      csrCtrl.io.CSRCtrlIf.csrrcien,
      csrCtrl.io.CSRCtrlIf.csrrcen,
      csrCtrl.io.CSRCtrlIf.csrrwien,
      csrCtrl.io.CSRCtrlIf.ecall,
      csrCtrl.io.CSRCtrlIf.rfen,
      csrCtrl.io.CSRCtrlIf.mepc2pc,
    ),
      0.U,
      !pipBlock
    )
     pipCSRRegWire := pipCSRReg.asTypeOf(new CSRID2EX)
    exu.io.CSRCtrlIf.csrrwen := pipCSRRegWire.csrrwen
    exu.io.CSRCtrlIf.csrswen := pipCSRRegWire.csrswen
    exu.io.CSRCtrlIf.csrrsien := pipCSRRegWire.csrrsien
    exu.io.CSRCtrlIf.csrrcien := pipCSRRegWire.csrrcien
    exu.io.CSRCtrlIf.csrrcen := pipCSRRegWire.csrrcen
    exu.io.CSRCtrlIf.csrrwien := pipCSRRegWire.csrrwien
    exu.io.CSRCtrlIf.ecall := pipCSRRegWire.ecall
    exu.io.CSRCtrlIf.rfen := pipCSRRegWire.rfen
//    wenID2ExWire := pipID2ExWire.memWriteM || pipCSRRegWire.rfen
    exu.io.CSRCtrlIf.mepc2pc := pipCSRRegWire.mepc2pc
    exu.io.uimm := pipID2ExWire.rs1

  }





  val dnpcTakenWithoutPreB = Wire(Bool())
  //EX2MEM
//  val aluResEX2MEMWire = Wire(UInt(dataW.W))
  val pipEX2MEMReg = RegEnable(Mux(
  (intrTimeCnt && pipID2ExWire.pc =/=0.U),
  1.U,
  Cat(
  pipID2ExWire.cmd,
  pipID2ExWire.inst,
  exu.io.dataOut.ALUResOut,
  exu.io.dataOut.wdata ,
  pipID2ExWire.snpc,
  pipID2ExWire.rd,
  pipID2ExWire.memReadNum,
  pipID2ExWire.valid,
  pipID2ExWire.mask,
  pipID2ExWire.memWriteM,
  pipID2ExWire.regEn|| pipCSRRegWire.rfen,
  pipID2ExWire.resultSrc,
  pipID2ExWire.pc,
    pipID2ExWire.fencei,
    (intrTimeCnt && pipID2ExWire.pc =/=0.U),
    pipID2ExWire.dma || pipID2ExWire.dmaWR,
    exu.io.dnpc,
    dnpcTakenWithoutPreB
  )
) ,0.U, !pipBlock)
  val pipEX2MEMWire = pipEX2MEMReg.asTypeOf(new EX2MEM)
//  aluResEX2MEMWire := pipEX2MEMWire.ALURes
//  io.dataIO.data_write := pipEX2MEMWire.writeDataM
  io.dataIO.mask := pipEX2MEMWire.mask <<pipEX2MEMWire.ALURes(2,0)
  io.dataIO.wen := pipEX2MEMWire.memWriteM
//  io.dataIO.addr :=Mux(  pipEX2MEMWire.memWriteM,Cat(pipEX2MEMWire.ALURes(addrW -1 ,3),0.U(3.W)),pipEX2MEMWire.ALURes)
  io.dataIO.addr :=pipEX2MEMWire.ALURes
  io.dataIO.rsize := pipEX2MEMWire.memReadNum(1,0)
  io.dataIO.data_write:=pipEX2MEMWire.writeDataM << pipEX2MEMWire.ALURes(2,0)*8.U
  io.dataIO.valid := pipEX2MEMWire.valid && !block1 && !blockDMA
  exu.io.aluRes1 := Mux(pipEX2MEMWire.ResultSrc ===regFrom_ALU , pipEX2MEMWire.ALURes,pipEX2MEMWire.snpc)

 val skip =( pipEX2MEMWire.valid &&( pipEX2MEMWire.ALURes < skiplow.U || pipEX2MEMWire.ALURes > skiphigh.U))|| pipEX2MEMWire.fencei
//    val block3 = pipEX2MEMWire.valid && !io.dataIO.ready
  val block3 = Wire(Bool())
  block3 := pipEX2MEMWire.valid && !io.dataIO.ready
  BoringUtils.addSource(block3, "block3")


    val block23 = Wire(Bool())
  block23 := block2 || block3
    BoringUtils.addSource(block23,"block23" )


  //MEM2WB
 val jud = pipEX2MEMWire.pc === 0.U
  val npcsend = Wire(UInt(addrW.W))
  val pipMEM2WBReg =RegEnable(Cat(
    pipEX2MEMWire.cmd,
    pipEX2MEMWire.inst,
    pipEX2MEMWire.ALURes,
    pipEX2MEMWire.rd,
    pipEX2MEMWire.snpc,
    pipEX2MEMWire.memReadNum,
    pipEX2MEMWire.regEn,
    pipEX2MEMWire.ResultSrc,
    npcsend,
    pipEX2MEMWire.intr,
    skip || pipEX2MEMWire.skip
  ),0.U, !pipBlock)
  val pipMEM2WBWire = pipMEM2WBReg.asTypeOf(new MEM2WB)
  npcsend := Mux( jud , pipMEM2WBWire.pc + 4.U , pipEX2MEMWire.pc)
  exu.io.aluResIn := pipMEM2WBWire.ALURes
  idu.io.rdIn := pipMEM2WBWire.rd
  exu.io.snpc := pipMEM2WBWire.snpc
  exu.io.memReadNum := pipMEM2WBWire.memReadNum
  idu.io.regEn := pipMEM2WBWire.regEn
  exu.io.ResultSrc := pipMEM2WBWire.ResultSrc
//  exu.io.dataOut.rdata := io.dataIO.data_read >>pipMEM2WBWire.ALURes(2,0)*8.U
  exu.io.dataOut.rdata :=RegEnable( io.dataIO.data_read , 0.U,!pipBlock)>>pipMEM2WBWire.ALURes(2,0)*8.U
  idu.io.dataEx.dIn := exu.io.dataId.dIn

  idu.io.pc := pipMEM2WBWire.pc
//  exu.io.aluRes2 := 0.U

//  when(pipMEM2WBWire.cmd === true.B){
//    printf("inst is %x\n",  pipMEM2WBWire.inst)
//    printf("pc is %x\n",  pipMEM2WBWire.pc)
//  }
//  io.diffport := pipMEM2WBWire.cmd

  //WB2END
  val pipWB2ENDWire = RegEnable(
    Cat(
      pipMEM2WBWire.rd,
      pipMEM2WBWire.regEn,
      pipMEM2WBWire.inst,
      pipMEM2WBWire.pc
    ),
    0.U,
    !pipBlock
  ).asTypeOf(new WB2END)
  idu.io.rsWB := pipWB2ENDWire.rd

  val x = if (reduceTimeWay) {
    0.U
  } else {
    idu.io.dOutWB
  }
  exu.io.aluRes3 :=  x


  //  val dnpcTake = (pipID2ExWire.branch && exu.io.brTake) || pipID2ExWire.jump || pipCSRRegWire.mepc2pc || pipCSRRegWire.ecall || (intrTimeCnt && pipID2ExWire.pc =/= 0.U) || pipID2ExWire.fencei
   dnpcTakenWithoutPreB := (pipID2ExWire.branch && exu.io.brTake) || pipID2ExWire.jump || pipCSRRegWire.mepc2pc || pipCSRRegWire.ecall
  val jump1 = !dnpcTakenWithoutPreB && pipID2ExWire.ifjump
  val jump2 = dnpcTakenWithoutPreB && (exu.io.dnpc =/= pipID2ExWire.ifdnpc || !pipID2ExWire.ifjump)
  pipFlashWire := jump1 || jump2 || (intrTimeCnt && pipID2ExWire.pc =/= 0.U) || pipID2ExWire.fencei
//  pipFlashWire := dnpcTake
  ifu.io.jump := jump1 || jump2 || (intrTimeCnt && pipID2ExWire.pc =/= 0.U) || pipID2ExWire.fencei || preBranchIns.io.ifjump
  ifu.io.dnpc := Mux(
    intrTimeCnt && pipID2ExWire.pc =/= 0.U,
    exu.io.dnpc,
    Mux(
      pipID2ExWire.fencei,
      pipID2ExWire.snpc,
      Mux(
        jump1,
        pipID2ExWire.snpc,
        Mux(
          jump2,
          exu.io.dnpc,
          preBranchIns.io.ifdnpc
        )
      )
    )
  )
//    Mux( pipID2ExWire.fencei && !intrTimeCnt,  pipID2ExWire.snpc,exu.io.dnpc)

  hazardu.io.regEnEXMEM := pipEX2MEMWire.regEn
  hazardu.io.rdEXMEM := pipEX2MEMWire.rd
  hazardu.io.regEnMEMWB := pipMEM2WBWire.regEn
  hazardu.io.rdMEMWB := pipMEM2WBWire.rd
  hazardu.io.regEnWBEND := pipWB2ENDWire.regEn
  hazardu.io.rdWBEND := pipWB2ENDWire.rd

  exu.io.forwardA := hazardu.io.forwardA
  exu.io.forwardB := hazardu.io.forwardB
  exu.io.forwardC := hazardu.io.forwardC


  hazardu.io.rs1IFID := idu.io.rs1
  hazardu.io.rs2IFID := idu.io.rs2
  hazardu.io.rdIDEX := pipID2ExWire.rd
  hazardu.io.resSrc := pipID2ExWire.resultSrc

  val hazardPCBlock = Wire(Bool())
  hazardPCBlock := hazardu.io.loadHazard
  BoringUtils.addSource(hazardPCBlock, "hazardPCBlock")

  pipBlock := block1 || block23||blockDMA

  val fencei = Wire(Bool())
  fencei := pipID2ExWire.fencei && !intrTimeCnt && !pipBlock
  BoringUtils.addSource(fencei, "updataICache")


//
//  if(HASDPIC) {
    val difftest = Module(new difftest)
    difftest.io.v := pipMEM2WBWire.cmd && !pipBlock
    val intrins = Module(new intr)
    intrins.io.intr := pipMEM2WBWire.intr && !pipBlock
    val Ebpro = Module(new ebProbe)
    Ebpro.io.inst := pipMEM2WBWire.inst
    Ebpro.io.block := pipBlock
    //
    val skipinst = Module(new skip)
    skipinst.io.v := pipMEM2WBWire.skip

//  }

//  if(reduceTimeWay){
    preBranchIns.io.ifpc := ifu.io.pc
    preBranchIns.io.exjump := pipEX2MEMWire.dnpcTakenWithoutPreB
    preBranchIns.io.expc := pipEX2MEMWire.pc
    preBranchIns.io.exdpc := pipEX2MEMWire.dnpc
//  }else {
//    preBranchIns.io.ifpc := ifu.io.pc
//    preBranchIns.io.exjump := dnpcTakenWithoutPreB
//    preBranchIns.io.expc := pipID2ExWire.pc
//    preBranchIns.io.exdpc := exu.io.dnpc
//  }







//val regnt = Wire(UInt(32.W))
//  regnt := RegNext(regnt + 1.U,0.U)
//  when(io.dataIO.addr === "h830000e8".U){printf("cnt is %d\n",regnt)}


//  val prf = Wire(Bool())
//  prf :=
//    pipIF2IDWire.pc === "h300054e8".U(addrW.W) ||
//    pipID2ExWire.pc === "h300054e8".U(addrW.W) ||
//      pipEX2MEMWire.pc === "h300054e8".U(addrW.W) ||
//      pipMEM2WBWire.pc === "h300054e8".U(addrW.W) ||
//      pipWB2ENDWire.pc === "h300054e8".U(addrW.W) ||
//      ifu.io.pc === "h300054e8".U(addrW.W)
//  BoringUtils.addSource(prf, "prf")



//  printf("io.instIO.valid is %x\n",io.instIO.valid)
//  printf("io.instIO.ready is %x\n",io.instIO.ready)
//  printf("io.instIO.data_read is %x\n",io.instIO.data_read)
//  printf("io.instIO.data_write is %x\n",io.instIO.data_write)
//  printf("io.instIO.wen is %x\n",io.instIO.wen)
//  printf("io.instIO.addr is %x\n",io.instIO.addr)
//  printf("io.instIO.rsize is %x\n",io.instIO.rsize)
//  printf("io.instIO.mask is %x\n",io.instIO.mask)
//  printf("io.dataIO.valid is %x\n", io.dataIO.valid)
//  printf("io.dataIO.ready is %x\n", io.dataIO.ready)
//  printf("io.dataIO.data_read is %x\n", io.dataIO.data_read)
//  printf("io.dataIO.data_write is %x\n", io.dataIO.data_write)
//  printf("io.dataIO.wen is %x\n", io.dataIO.wen)
//  printf("io.dataIO.addr is %x\n", io.dataIO.addr)
//  printf("io.dataIO.rsize is %x\n", io.dataIO.rsize)
//  printf("io.dataIO.mask is %x\n", io.dataIO.mask)
//  printf("io.instIO.valid is %d\n",io.instIO.valid)





//when(!(block1 ||block2||block3)) {
//  printf("\n")
//  printf("block is %x\n",(block1 ||block2||block3))
//  printf("block1 is %x\n",(block1))
//  printf("block2 is %x\n",(block2))
//  printf("block3 is %x\n",(block3))
////  printf("if.in is%x\n", Cat(
////    ifu.io.pc =/= 0.U,
////    ifu.io.instOut,
////    ifu.io.pc,
////    ifu.io.snpc))
//
//  when(pipWB2ENDWire.pc === "h300054e8".U(addrW.W)) {
//    printf("csrCtrl.io.CSRCtrlIf.csrrwen is %x\n", csrCtrl.io.CSRCtrlIf.csrrwen)
//    printf("csrCtrl.io.CSRCtrlIf.csrswen is %x\n", csrCtrl.io.CSRCtrlIf.csrswen)
//    printf("csrCtrl.io.CSRCtrlIf.csrrsien is %x\n", csrCtrl.io.CSRCtrlIf.csrrsien)
//    printf("csrCtrl.io.CSRCtrlIf.csrrcien is %x\n", csrCtrl.io.CSRCtrlIf.csrrcien)
//    printf("csrCtrl.io.CSRCtrlIf.csrrcen is %x\n", csrCtrl.io.CSRCtrlIf.csrrcen)
//    printf("csrCtrl.io.CSRCtrlIf.ecall is %x\n", csrCtrl.io.CSRCtrlIf.ecall)
//    printf("csrCtrl.io.CSRCtrlIf.rfen is %x\n", csrCtrl.io.CSRCtrlIf.rfen)
//    printf("csrCtrl.io.CSRCtrlIf.mepc2pc is %x\n", csrCtrl.io.CSRCtrlIf.mepc2pc)
//    printf("if is %x\n", pipIF2IDWire.asUInt())
//    printf(" if.pc is %x\n", pipIF2IDWire.pc)
//    printf(" if.inst is %x\n", pipIF2IDWire.inst)
//    printf(" if.cmd is %x\n", pipIF2IDWire.cmd)
//    printf(" if.snpc is %x\n", pipIF2IDWire.snpc)
//    //
//
//    printf("id is %x\n", pipIF2IDWire.asUInt())
//    printf(" id.cmd is %x\n", pipID2ExWire.cmd)
//    printf(" id.inst is %x\n", pipID2ExWire.inst)
//    printf(" id.imme is %x\n", pipID2ExWire.imme)
//    printf(" id.dOut1 is %x\n", pipID2ExWire.dOut1)
//    printf(" id.dOut2 is %x\n", pipID2ExWire.dOut2)
//    printf(" id.pc is %x\n", pipID2ExWire.pc)
//    printf(" id.snpc is %x\n", pipID2ExWire.snpc)
//    printf(" id.rd is %x\n", pipID2ExWire.rd)
//    printf(" id.ALUCtrl is %x\n", pipID2ExWire.ALUCtrl)
//    printf(" id.ALUSrc1 is %x\n", pipID2ExWire.ALUSrc1)
//    printf(" id.ALUSrc2 is %x\n", pipID2ExWire.ALUSrc2)
//    printf(" id.memReadNum is %x\n", pipID2ExWire.memReadNum)
//    printf(" id.mask is %x\n", pipID2ExWire.mask)
//    printf(" id.memWriteM is %x\n", pipID2ExWire.memWriteM)
//    printf(" id.dnpcSrc is %x\n", pipID2ExWire.dnpcSrc)
//    printf(" id.jump is %x\n", pipID2ExWire.jump)
//    printf(" id.branch is %x\n", pipID2ExWire.branch)
//    printf(" id.regEn is %x\n", pipID2ExWire.regEn)
//    printf(" id.resultSrc is %x\n", pipID2ExWire.resultSrc)
//    printf(" id.rs1 is %x\n", pipID2ExWire.rs1)
//    printf(" id.rs2 is %x\n", pipID2ExWire.rs2)
//
//    printf(" ex.commit is %x\n", pipEX2MEMWire.cmd)
//    printf(" ex.inst is %x\n", pipEX2MEMWire.inst)
//    printf(" ex.ALURes is %x\n", pipEX2MEMWire.ALURes)
//    printf(" ex.snpc is %x\n", pipEX2MEMWire.snpc)
//    printf(" ex.rd is %x\n", pipEX2MEMWire.rd)
//    printf(" ex.memReadNum is %x\n", pipEX2MEMWire.memReadNum)
//    printf(" ex.valid is %x\n", pipEX2MEMWire.valid)
//    printf(" ex.mask is %x\n", pipEX2MEMWire.mask)
//    printf(" ex.memWriteM is %x\n", pipEX2MEMWire.memWriteM)
//    printf(" ex.regEn is %x\n", pipEX2MEMWire.regEn)
//    printf(" ex.ResultSrc is %x\n", pipEX2MEMWire.ResultSrc)
//    printf(" ex.pc is %x\n", pipEX2MEMWire.pc)
//
//    printf(" mem.cmd is %x\n", pipMEM2WBWire.cmd)
//    printf(" mem.inst is %x\n", pipMEM2WBWire.inst)
//    printf(" mem.ALURes %x\n", pipMEM2WBWire.ALURes)
//    printf(" mem.rd %x\n", pipMEM2WBWire.rd)
//    printf(" mem.snpc %x\n", pipMEM2WBWire.snpc)
//    printf(" mem.memReadNum %x\n", pipMEM2WBWire.memReadNum)
//    printf(" mem.regEn %x\n", pipMEM2WBWire.regEn)
//    printf(" mem.ResultSrc %x\n", pipMEM2WBWire.ResultSrc)
//    printf(" mem.pc %x\n", pipMEM2WBWire.pc)
//
//    printf("wb.pc is%x\n", pipWB2ENDWire.pc)
//    printf("wb.inst is%x\n", pipWB2ENDWire.inst)
//    printf("wb.rd is%x\n", pipWB2ENDWire.rd)
//    printf("wb.dOutWB is%x\n", idu.io.dOutWB)
//
//    printf("pipCSRRegWire.rfen is%x\n", pipCSRRegWire.rfen)
//    printf("pipCSRRegWire.ecall is%x\n", pipCSRRegWire.ecall)
//    printf("pipCSRRegWire.mepc2pc is%x\n", pipCSRRegWire.mepc2pc)
//    printf("pipCSRRegWire.csrrwen is%x\n", pipCSRRegWire.csrrwen)
//    printf("pipCSRRegWire.csrswen is%x\n", pipCSRRegWire.csrswen)
//  assert(pipEX2MEMWire.pc =/= "h300054e8".U(addrW.W))
//  }
//
//  printf("pipFlashWire %x\n",pipFlashWire)
//  printf("hazardu.io.loadHazard %x\n",hazardu.io.loadHazard)
//}

//  val missins =  Module(new missReport)
//  missins.io.inst := pipWB2ENDWire.inst
//  missins.io.pc := pipWB2ENDWire.pc
//val mstatus = Wire(Bool())
//  mstatus := false.B
//  BoringUtils.addSink(mstatus, "mstatus")
//  when(!mstatus){
//    printf("if.pc is%x\n", pipIF2IDWire.pc)
//    printf("id.pc is%x\n", pipID2ExWire.pc)
//    printf("ex.pc is%x\n", pipEX2MEMWire.pc)
//    printf("mem.pc is%x\n", pipMEM2WBWire.pc)
//    printf("wb.pc is%x\n", pipWB2ENDWire.pc)
//    assert(mstatus)
//  }
//  assert(pipWB2ENDWire.pc =/= "h8000050c".U)


}


object riscvgen extends App {
  chisel3.Driver.execute(args,() => new riscv )


}