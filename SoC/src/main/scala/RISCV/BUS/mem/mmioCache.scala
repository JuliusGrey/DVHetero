package RISCV.BUS.mem

import chisel3._
import chisel3.util._
import RISCV.common.Interface.cpuRWIO
import RISCV.common.Param.{addrW, dataW}
class mmioCache extends Module{
  val io = IO(new Bundle() {
    val block = Input(Bool())
    val mmioIn = new cpuRWIO(dataW , addrW )
    val mmioOut = Flipped (new cpuRWIO(dataW , addrW ))

  })
  io.mmioIn<>io.mmioOut
  //  val idleSt :: waitSt ::  dataSt ::Nil = Enum(3)
//  val idleMux = Mux(io.mmioIn.valid , waitSt,idleSt)
//  val waitMux = Mux(io.mmioOut.ready,dataSt,waitSt)
//  val dataMux = Mux(io.block, dataSt,idleSt)
//  val state = RegInit(idleSt)
//  state := MuxLookup(
//    state,
//    idleSt,
//    Array(
//      idleSt -> idleMux,
//      waitSt -> waitMux,
//      dataSt -> dataMux
//    )
//  )
//  val isIdle = state === idleSt
//  val isWait = state===waitSt
//  val isData = state === dataSt
//
//  io.mmioOut.valid := isIdle && io.mmioIn.valid
//  io.mmioOut.wen := io.mmioIn.wen
//  io.mmioOut.addr := io.mmioIn.addr
//  io.mmioOut.mask := io.mmioIn.mask
//  io.mmioOut.data_write := io.mmioIn.data_write
//  io.mmioOut.rsize := io.mmioIn.rsize
//
//  io.mmioIn.data_read := RegEnable(io.mmioOut.data_read, 0.U,io.mmioOut.ready)
//  io.mmioIn.ready := isData

}


