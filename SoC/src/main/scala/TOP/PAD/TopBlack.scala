package TOP.PAD

import RISCV.common.Interface.AXIIO
import chisel3._

class TopBlack extends BlackBox {
  val io = IO(new Bundle() {
    //    val imaster = new AXIIO
    val dmaster = new AXIIO

    //val instIO = Flipped(new cpuRWIO(dataW, addrW))
    //    val dataIO = Flipped(new cpuRWIO(dataW, addrW))
  })
}
