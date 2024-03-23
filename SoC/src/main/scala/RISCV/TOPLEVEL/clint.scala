package RISCV.TOPLEVEL

import chisel3._
import chisel3.util._
import chisel3.util.experimental.BoringUtils
import RISCV.common.Interface.cpuRWIO
import RISCV.common.Param.{addrW, clintLow, dataW}
class clint extends Module{
  val io = IO(new Bundle() {
    val  clintIO = new cpuRWIO(dataW, addrW)
  })
  val startTimeCnt = Wire(Bool())
  startTimeCnt := true.B
  BoringUtils.addSink(startTimeCnt, "startTimeCnt")

  val mtimecmp = Wire(UInt(64.W))
  val mtime = Wire(UInt(64.W))

  val mtimecmpOff= "h4000"
  val mtimeOff= "hBFF8"
//  printf("startTimeCnt is %x\n",startTimeCnt)
    mtime := RegEnable(mtime + 1.U, 0.U, startTimeCnt)

  mtimecmp := RegEnable(io.clintIO.data_write , 0.U,io.clintIO.valid &&io.clintIO.wen &&io.clintIO.addr === (clintLow.U +mtimecmpOff.U ))


  io.clintIO.ready := true.B
  io.clintIO.data_read := MuxLookup(
    io.clintIO.addr,
    0.U,
    Array(
      (mtimecmpOff.U + clintLow.U) -> mtimecmp,
      (mtimeOff.U + clintLow.U) -> mtime,
    )
  )

  val intrTimeCnt = Wire(Bool())
  intrTimeCnt := mtime >= mtimecmp && startTimeCnt
  BoringUtils.addSource(intrTimeCnt, "intrTimeCnt")
//  printf("mtimecmp is %x\n", mtimecmp)
//  printf("mtime is %x\n", mtime)
//  printf("intrTimeCnt is %x\n", intrTimeCnt)
// when (startTimeCnt) {
//   printf("mtimecmp is %d\n", mtimecmp)
//   printf("mtime is %d\n", mtime)
//
// }
}
