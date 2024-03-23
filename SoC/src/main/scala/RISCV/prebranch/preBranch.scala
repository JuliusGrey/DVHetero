package RISCV.prebranch

import chisel3._
import chisel3.util._
import chisel3.util.experimental.BoringUtils
import RISCV.common.Param.addrW

class preBranch extends Module{
  val io = IO(new Bundle {
    val exjump = Input(Bool())
    val ifpc = Input(UInt(addrW.W))
    val expc = Input(UInt(addrW.W))
    val exdpc = Input(UInt(addrW.W))
    val ifdnpc = Output(UInt(addrW.W))
    val ifjump = Output(Bool())
  })
  val cellNum = 16

  val block1 = Wire(Bool())
  block1 := false.B
  BoringUtils.addSink(block1, "block1")
  val block23 = Wire(Bool())
  block23 := false.B
  val blockDMA = Wire(Bool())
  blockDMA := false.B
  BoringUtils.addSink(blockDMA, "block23")
  val block = block1 || block23 ||blockDMA

  //ex阶段写入npc
  val precelList = List.fill(cellNum)(Module(new preCell))
  val pcList = List.fill(cellNum)(Wire(UInt(addrW.W)))
  val vList = List.fill(cellNum)(Wire(Bool()))
  val hitList = List.fill(cellNum)(Wire(Bool()))

  (0 until cellNum).foreach{ i => {
    hitList(i) := io.expc === pcList(i) && vList(i)
  }}
  val hit = hitList.foldLeft(false.B)((b, a) => b || a)

  val cnt = Wire(UInt(4.W))
  cnt := RegEnable( cnt + 1.U , 0.U , (!hit && io.exjump)&& !block)


  (0 until cellNum).map { i => {
    precelList(i).io.jump := io.exjump
    precelList(i).io.dnpcIn := io.exdpc
    precelList(i).io.cen := (hitList(i) || (io.exjump && cnt === i.U)) && !block

    vList(i) := RegEnable(true.B, false.B, !hitList(i)&&io.exjump && cnt === i.U && !block)
    pcList(i) := RegEnable(io.expc,0.U, !hitList(i) &&io.exjump && cnt === i.U && !block )
  }
  }

  //if阶段读取npc

  val hitIfList = List.fill(cellNum)(Wire(Bool()))
  (0 until cellNum).map { i => {
    hitIfList(i) := io.ifpc === pcList(i) && vList(i)
  }
  }
  val hitif = hitIfList.foldLeft(false.B)((b, a) => b || a)

  val cellValidList = precelList.map{
    i => i.io.valid
  }
  io.ifjump :=Mux(
    !hitif,
    false.B,
    Mux1H(
      hitIfList,
      cellValidList
    )
  )

  val dnpcOutList = precelList.map {
    i => i.io.dnpcOut
  }
  io.ifdnpc := Mux(
    !hitif,
    0.U,
    Mux1H(
      hitIfList,
      dnpcOutList
    )
  )

}

object preBranchgen extends App {
  chisel3.Driver.execute(args,() => new preBranch )


}
