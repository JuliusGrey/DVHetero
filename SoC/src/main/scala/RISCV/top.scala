//import RISCV.BUS.AXI4
//import RISCV.BUS.mem.mmioCache
//import RISCV.TOPLEVEL.{arbCpu2Cache, clint}
//import chisel3.{printf, _}
//import chisel3._
//import chisel3.util._
//import chisel3.util.experimental.BoringUtils
//import RISCV.common.Interface.{AXIIO, AXIInterface, SignalRAM, cpuRWIO}
//import RISCV.common.Param.{SRAMaddrW, SRAMdataW, addrW, dataW}
//class top extends Module{
//  val io = IO(new Bundle() {
//    val instIO = Flipped(new cpuRWIO(dataW, addrW))
//    val dataIO = Flipped(new cpuRWIO(dataW, addrW))
////    val intr = Output(Bool())
////val instIO = Flipped(new cpuRWIO(dataW, addrW))
////    val dataIO = Flipped(new cpuRWIO(dataW, addrW))
//  })
//
//  val riscvIns = Module(new riscv)
//  val arbCpu2CacheIns = Module(new arbCpu2Cache)
//  val  clintIns = Module(new clint)
//  riscvIns.io.instIO <> io.instIO
//  riscvIns.io.dataIO <> arbCpu2CacheIns.io.arbIn
//  arbCpu2CacheIns.io.arbMMIO<>io.dataIO
//  arbCpu2CacheIns.io.arbClint <> clintIns.io.clintIO
//
//
//  val intrTimeCnt = Wire(Bool())
//  intrTimeCnt := false.B
//  BoringUtils.addSink(intrTimeCnt, "intrTimeCnt")
//
////  io.intr := intrTimeCnt
//
//
//  val block2 = Wire(Bool())
//  block2 := false.B
//  BoringUtils.addSink(block2, "block2")
//  val block3 = Wire(Bool())
//  block3 := false.B
//  BoringUtils.addSink(block3, "block3")
//}
//
//object topgen extends App {
//
//  chisel3.Driver.execute(args,() => new top )
//
//
//}
