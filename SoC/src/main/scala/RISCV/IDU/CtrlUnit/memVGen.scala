package RISCV.IDU.CtrlUnit

import chisel3.util._
import chisel3._
import RISCV.common.Param.instW
import RISCV.common.instList._

import scala.collection.mutable.ArrayBuffer


class memVGen extends Module{
  val io = IO(new Bundle() {
    val inst = Input(UInt(instW.W))
    val valid = Output(Bool())
  })

  val memVBuf: ArrayBuffer[Tuple2[Bool, UInt]] = new ArrayBuffer()
  map_type.map {
    case (k, v) => {
      v match {
        case "S" => memVBuf.append((io.inst === k) -> true.B)
        case _ =>
      }
    }
  }

  memVBuf.append((io.inst === LD) -> true.B )
  memVBuf.append((io.inst === LBU) -> true.B )
  memVBuf.append((io.inst === LH) -> true.B )
  memVBuf.append((io.inst === LW) -> true.B )
  memVBuf.append((io.inst === LD) -> true.B )
  memVBuf.append((io.inst === LHU) -> true.B )
  memVBuf.append((io.inst === LB) -> true.B )
  memVBuf.append((io.inst === LWU) -> true.B )


  io.valid := MuxCase(false.B, memVBuf)

  //  BoringUtils.addSource(MuxCase(false.B, memVBuf),"block" )


}
