package RISCV.prebranch

import chisel3._
import chisel3.util._
import RISCV.common.Param.addrW

import scala.Array

class preCell extends Module{
  val io = IO(new Bundle {
    val cen = Input(Bool())
    val jump =  Input(Bool())
    val dnpcIn =  Input(UInt(addrW.W))
    val dnpcOut =Output(UInt(addrW.W))
    val valid =Output(Bool())
  })
  val dnpcReg = RegEnable(io.dnpcIn,0.U,io.cen && io.jump)

  val sntaken :: ntaken :: taken :: staken::Nil = Enum(4)
//  val state = RegInit(sntaken)
//  val nTakenV = io.npc === io.pc + 4.U
  val takenV = dnpcReg === io.dnpcIn
  val sntMux = Mux(takenV,ntaken,sntaken)
  val ntMux = Mux(takenV,taken,sntaken)
  val tMux = Mux(takenV,staken,ntaken)
  val stMux = Mux(takenV,staken,taken)
  val stateWire = Wire(UInt(2.W))
   stateWire := RegEnable(
    MuxLookup(
      stateWire,
      sntaken,
      Array(
        sntaken -> sntMux,
        ntaken -> ntMux,
        taken -> tMux,
        staken -> stMux
      )
    ),
     sntaken,
     io.cen
  )

  io.dnpcOut := dnpcReg
  io.valid := stateWire === taken ||  stateWire === staken

}
