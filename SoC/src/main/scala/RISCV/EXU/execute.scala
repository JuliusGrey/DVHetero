package RISCV.EXU

import RISCV.IDU.CtrlUnit._
import RISCV.EXU.ALUOp.ALU
import RISCV.IDU.CtrlUnit.{ALUSrc, ResultSrcList}
import RISCV.PIP.hazardPa._
import chisel3._
import chisel3.util.{Cat, MuxLookup}
import RISCV.common.Interface._
import RISCV.common.Param.{addrW, dataW}
import RISCV.IDU.CtrlUnit.ALUOpSel._
import chisel3.util.experimental.BoringUtils


class execute extends Module {
  val io = IO (new Bundle {
    //控制端口
    val AluSrc1 = Input(UInt(2.W))
    val AluSrc2 = Input(UInt(2.W))
    val ALUCtrl = Input(UInt(ALU_Ctrl_W.W))

    val dnpcSrc = Input(Bool())

    val ResultSrc = Input(UInt(2.W))

    val memReadNum = Input(UInt(3.W))

    //来自idu的
    val dataId = Flipped( new de2ex)

    //输出到外部的
    val dataOut = new ex2out

    //branch,用于内部逻辑
    val brTake = Output(Bool())



    //pc npc
    val pc = Input(UInt(addrW.W))
    val snpc = Input(UInt(addrW.W))
    val dnpc = Output(UInt(addrW.W))

    //csr
    val CSRCtrlIf = Flipped( new CSRCtrlInterface)
    val uimm = Input(UInt(5.W))

    //流水线新添加
    val aluResIn  = Input(UInt(dataW.W))
    val forwardA = Input(UInt(2.W))
    val forwardB = Input(UInt(2.W))
    val forwardC = Input(UInt(2.W))//dma 的端口
    val aluRes1 = Input(UInt(dataW.W))
    val aluRes3 = Input(UInt(dataW.W))
  })
//ex
  val res2 = Wire(UInt(dataW.W))
  val dOut1 = MuxLookup(
    io.forwardA,
    io.dataId.dOut1,
    Array(
      FORWARD1 ->io.aluRes1,
      FORWARD2 ->res2,
      FORWARD3 ->io.aluRes3,
    )
  )

  val ALUD1 = MuxLookup(
    io.AluSrc1,
    0.U,
    Array(
      ALUSrc.ALUSrcRd -> dOut1,
      ALUSrc.ALUSrcImm -> io.dataId.imme,
      ALUSrc.ALUSrcPC -> io.pc,
    )
  )


  val dOut2 = MuxLookup(
    io.forwardB,
    io.dataId.dOut2,
    Array(
      FORWARD1 -> io.aluRes1,
      FORWARD2 -> res2,
      FORWARD3 ->io.aluRes3,
    )
  )


  val ALUD2 = MuxLookup(
    io.AluSrc2,
    0.U,
    Array(
      ALUSrc.ALUSrcRd -> dOut2,
      ALUSrc.ALUSrcImm -> io.dataId.imme,
      ALUSrc.ALUSrcPC -> io.pc,
    )
  )
  val ALU = Module(new ALU)
  ALU.io.srcA := ALUD1
  ALU.io.srcB := ALUD2
  ALU.io.ALUCtrl := io.ALUCtrl


  //例化csr
  val csr_ins = Module(new CSR)
  csr_ins.io.csrrwen := io.CSRCtrlIf.csrrwen
  csr_ins.io.csrswen := io.CSRCtrlIf.csrswen
  csr_ins.io.csrrsien := io.CSRCtrlIf.csrrsien
  csr_ins.io.csrrcien := io.CSRCtrlIf.csrrcien
  csr_ins.io.csrrcen := io.CSRCtrlIf.csrrcen
  csr_ins.io.csrrwien := io.CSRCtrlIf.csrrwien
  csr_ins.io.ecall := io.CSRCtrlIf.ecall
//  csr_ins.io.rs1 := io.dataId.dOut1
  csr_ins.io.rs1 := dOut1
  csr_ins.io.imme := io.dataId.imme
  csr_ins.io.pc := io.pc
  csr_ins.io.uimm := io.uimm
  csr_ins.io.mret := io.CSRCtrlIf.mepc2pc



  io.dataOut.ALUResOut := Mux(io.CSRCtrlIf.rfen,csr_ins.io.rd,ALU.io.ALUResult)
  io.dataOut.wdata := dOut2

  val dnpcGenIns = Module(new dnpcGen)
  dnpcGenIns.io.pc := io.pc
  dnpcGenIns.io.imme := io.dataId.imme
//  dnpcGenIns.io.rd := io.dataId.dOut1
  dnpcGenIns.io.rd := dOut1
  dnpcGenIns.io.npcSrc := io.dnpcSrc

  val intrTimeCnt = Wire(Bool())
  intrTimeCnt := false.B
  BoringUtils.addSink(intrTimeCnt, "intrTimeCnt")

  io.dnpc := Mux(io.CSRCtrlIf.ecall || intrTimeCnt,csr_ins.io.mtvec,Mux(io.CSRCtrlIf.mepc2pc,csr_ins.io.mepc,dnpcGenIns.io.dnpc))

  io.brTake := ALU.io.ALUResult(0).asBool()
//  printf("io.CSRCtrlIf.ecall is %x\n" , io.CSRCtrlIf.ecall)
//  printf("intrTimeCnt is %x\n" , intrTimeCnt)
//  printf("csr_ins.io.mtvec is %x\n" , csr_ins.io.mtvec)
//  printf("io.CSRCtrlIf.mepc2pc is %x\n" , io.CSRCtrlIf.mepc2pc)
//  printf("csr_ins.io.mepc is %x\n" , csr_ins.io.mepc)
//  printf("dnpcGenIns.io.dnpc is %x\n" , dnpcGenIns.io.dnpc)




  //wb
  val memData_ins = Module(new memData)
  memData_ins.io.memReadNum := io.memReadNum
  memData_ins.io.rdata := io.dataOut.rdata


  val dinMux = MuxLookup(
    io.ResultSrc,
    io.aluResIn,
    Array(
      ResultSrcList.regFrom_ALU ->io.aluResIn,
      ResultSrcList.regFrom_NPC -> io.snpc,
      ResultSrcList.regFrom_DM -> memData_ins.io.rdata_ext
    )
  )
  res2 := dinMux

  //写入rd
  io.dataId.dIn := dinMux




  //dma
  val dmaLen  = MuxLookup(
    io.forwardC,
    io.dataId.rdDout,
    Array(
      FORWARD1 -> io.aluRes1,
      FORWARD2 -> res2,
      FORWARD3 -> io.aluRes3,
    )
  )
  val dmaCtrl = Wire(UInt((3*dataW).W))
  dmaCtrl := Cat(dmaLen,dOut2,dOut1)
  BoringUtils.addSource(dmaCtrl, "dmaCtrl")


}

object exgen extends App {
  chisel3.Driver.execute(args,() => new execute )


}