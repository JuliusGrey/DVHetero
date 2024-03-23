package RISCV.EXU

import Chisel.{Cat, RegEnable}
import chisel3._
import chisel3.util.{Mux1H, MuxLookup}
import RISCV.common.Param._
import RISCV.IDU.CtrlUnit.CsrCode._
import chisel3.util.experimental.BoringUtils


class CSR extends Module {
  val io = IO(new Bundle {
    val csrrwen = Input(Bool())
    val csrswen = Input(Bool())
    val csrrsien = Input(Bool())
    val csrrcien = Input(Bool())
    val csrrcen = Input(Bool())
    val csrrwien = Input(Bool())
    val ecall = Input(Bool())

    val rs1 = Input(UInt(dataW.W))
    //    val rs2 = Input(UInt(8.W))
    val imme =  Input(UInt(12.W))
    val pc = Input(UInt(dataW.W))
    val uimm = Input(UInt(5.W))

    val rd = Output(UInt(dataW.W))
    val mtvec = Output(UInt(dataW.W))
    val mepc = Output(UInt(dataW.W))

    val mret =  Input(Bool())
  })
  val intrTimeCnt = Wire(Bool())
  intrTimeCnt := false.B
  BoringUtils.addSink(intrTimeCnt, "intrTimeCnt")

  val intr = intrTimeCnt && io.pc =/= 0.U





  val block1 = Wire(Bool())
  block1 := false.B
  BoringUtils.addSink(block1, "block1")
  val block23 = Wire(Bool())
  block23 := false.B
  BoringUtils.addSink(block23, "block23")
  val blockDMA = Wire(Bool())
  blockDMA := false.B
  BoringUtils.addSink(blockDMA, "blockDMA")


  val csren = io.csrrwen ||io.csrswen|| io.csrrcen || io.csrrsien || io.csrrcien || io.csrrwien
  val sel1H = Cat(io.csrrwien , io.csrrcien,io.csrrsien,io.csrrcen,io.csrswen,io.csrrwen)
  val uimmext = Cat(0.U((dataW - 5).W),io.uimm)

  val mepcen = io.ecall || (csren  &&  io.imme === mepc) ||intr
  val mepcins = Wire(UInt(dataW.W))
  val mepcval = Mux(
    io.ecall || intr,
    io.pc,
    Mux1H(
      sel1H,
      Array(
        io.rs1,
        io.rs1| mepcins,
        (~io.rs1).asUInt() & mepcins,
        uimmext |mepcins,
        (~uimmext).asUInt() & mepcins,
        uimmext
      )
    )
  )
  mepcins:= RegEnable(mepcval ,0.U, mepcen && !(block1 || block23||blockDMA))

  val mcauseen = io.ecall || (csren &&  io.imme === mcause) || intr
  val mcauseins =Wire(UInt(dataW.W))
  val mcauseval = Mux(
    intr,
    "h8000000000000007".U(64.W),
    Mux(
      io.ecall,
      11.U,
    Mux1H(
      sel1H,
      Array(
        io.rs1,
        io.rs1 | mcauseins,
        (~io.rs1).asUInt() & mcauseins,
        uimmext | mcauseins,
        (~uimmext).asUInt() & mcauseins,
        uimmext
      )
    ))
  )
  mcauseins := RegEnable(mcauseval , 0.U,mcauseen&& !(block1 || block23||blockDMA))

  val mtvecen =  (csren &&  io.imme === mtvec)
  val mtvecins = Wire(UInt(dataW.W))
  val mtvecval = Mux1H(
    sel1H,
    Array(
      io.rs1,
      io.rs1 | mtvecins,
      (~io.rs1).asUInt() & mtvecins,
      uimmext | mtvecins,
      (~uimmext).asUInt() & mtvecins,
      uimmext
    )
  )
  mtvecins := RegEnable(mtvecval ,0.U, mtvecen&& !(block1 || block23 || intr||blockDMA))

  val mstatusen  =  (csren &&  io.imme === mstatus ) || io.ecall||intr  || io.mret
  val mstatusins = Wire(UInt(dataW.W))
  val mstatusval =
    Mux(
    io.ecall|| intr,
    Cat(mstatusins(dataW - 1, 8),mstatusins(3),mstatusins(6,4),false.B,mstatusins(2,0) ),
      Mux(
        io.mret,
        Cat(mstatusins(dataW - 1, 8), true.B, mstatusins(6, 4), mstatusins(7), mstatusins(2, 0)),
    Mux1H(
    sel1H,
    Array(
      io.rs1,
      io.rs1 | mstatusins,
      (~io.rs1).asUInt() & mstatusins,
      uimmext | mstatusins,
      (~uimmext).asUInt() & mstatusins,
      uimmext
    )
  )))
//  val mstatusval = Mux(
//    io.mret,
//    Cat(mstatusins(dataW - 1, 8), true.B, mstatusins(6, 4), mstatusins(7), mstatusins(2, 0)),
//    Mux(
//      io.ecall || intr,
//      Cat(mstatusins(dataW - 1, 8), mstatusins(3), mstatusins(6, 4), false.B, mstatusins(2, 0)),
//      Mux1H(
//        sel1H,
//        Array(
//          io.rs1,
//          io.rs1 | mstatusins,
//          (~io.rs1).asUInt() & mstatusins,
//          uimmext | mstatusins,
//          (~uimmext).asUInt() & mstatusins,
//          uimmext
//        )
//      )))
  mstatusins := RegEnable(mstatusval ,"ha00001800".U(dataW.W) ,mstatusen&& !(block1 || block23||blockDMA))

  val miecen =  (csren &&  io.imme === mie )
  val mieins = Wire(UInt(dataW.W))
  val mieval = Mux1H(
    sel1H,
    Array(
      io.rs1,
      io.rs1 | mieins,
      (~io.rs1).asUInt() & mieins,
      uimmext | mieins,
      (~uimmext).asUInt() & mieins,
      uimmext
    )
  )
  mieins := RegEnable(mieval,0.U,miecen && !(block1 || block23|| intr||blockDMA))

  val mipcen = (csren &&  io.imme === mip ) ||intr
  val mipins = Wire(UInt(dataW.W))
  val mipval = Mux(
    intr,
    Cat(mipins(dataW - 1,8),true.B,mipins(6,0)),
    Mux1H(
    sel1H,
    Array(
      io.rs1,
      io.rs1 | mipins,
      (~io.rs1).asUInt() & mipins,
      uimmext | mipins,
      (~uimmext).asUInt() & mipins,
      uimmext
    )
  ))
  mipins := RegEnable(mipval, 0.U,mipcen && !(block1 || block23||blockDMA)  )

//  val mhartidcen = (csren && io.imme === mhartid) || intr
//  val mhartidins = Wire(UInt(dataW.W))
//  val mhartidval = Mux1H(
//    sel1H,
//    Array(
//      io.rs1,
//      io.rs1 | mhartidins,
//      (~io.rs1).asUInt() & mhartidins,
//      uimmext | mhartidins,
//      (~uimmext).asUInt() & mhartidins,
//      uimmext
//    )
//  )
//  mhartidins := RegEnable(mhartidval, 0.U, mhartidcen && !(block1 || block23))
//
//  val mscratchcen = (csren && io.imme === mscratch) || intr
//  val mscratchins = Wire(UInt(dataW.W))
//  val mscratchval = Mux1H(
//    sel1H,
//    Array(
//      io.rs1,
//      io.rs1 | mscratchins,
//      (~io.rs1).asUInt() & mscratchins,
//      uimmext | mscratchins,
//      (~uimmext).asUInt() & mscratchins,
//      uimmext
//    )
//  )
//  mscratchins := RegEnable(mscratchval, 0.U, mscratchcen && !(block1 || block23))
//  printf("in csr\n")
//  printf("sel1H is %x\n",sel1H)
//  printf("io.imme is %x\n",io.imme)
//  printf("io.rs1%x\n",io.rs1)
//  printf("mstatus is %x\n",mstatusins)
//  printf("mieins is %x\n",mieins)

  io.rd := MuxLookup(
    io.imme,
    0.U,
    Array(
      mepc -> mepcins,
      mcause -> mcauseins,
      mtvec -> mtvecins,
      mstatus -> mstatusins,
      mie -> mieins,
      mip -> mipins,
//      mhartid -> mhartidins,
//      mscratch -> mscratchins,
    )

  )

  io.mtvec := mtvecins
  io.mepc := mepcins

  val startTimeCnt = Wire(Bool())
  startTimeCnt := mieins(7) && mstatusins(3)
  BoringUtils.addSource(startTimeCnt, "startTimeCnt")

//  when(mstatus === io.imme){
//    printf("sel1H is %x\n",sel1H)
//    printf("sel1H is %x\n",sel1H)
//    printf("io.rs1 is %x\n",io.rs1)
//    printf("uimmext is %x\n",uimmext)
//    printf("mstatusins is %x\n",mstatusins)
//  }
//  printf("sel1H is %x\n",sel1H)
//  printf("mieins is %x\n",mieins)
//  printf("mieins is %x\n",mieins)
//  printf("dut mcause is %x\n",mcauseins)
//val mstatuss = Wire(Bool())
//  BoringUtils.addSource(mstatuss, "mstatus")


}
