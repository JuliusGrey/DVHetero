package RISCV.IDU.CtrlUnit

import chisel3.util._
import chisel3._
import RISCV.common.Param._
import RISCV.common.instList._

import scala.collection.mutable.ArrayBuffer
//只有s指令确定，其他都没有，不用手动添加
class memWriteMGen extends RawModule {
  val io = IO(new Bundle {
    val inst = Input(UInt(instW.W))
    val memWriteM = Output(Bool())
  })

  val memWriteBuf: ArrayBuffer[Tuple2[Bool, UInt]] = new ArrayBuffer()
  map_type.map {
    case (k, v) => {
      v match {
        case "S" => memWriteBuf.append((io.inst === k) -> true.B)
        case _ =>
      }
    }
  }

  io.memWriteM := MuxCase(0.U, memWriteBuf)
}
