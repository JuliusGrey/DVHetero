package RISCV.IDU.CtrlUnit

import chisel3._
import chisel3.util.MuxCase
import RISCV.common.Param._
import RISCV.common.instList._
import dnpcSrcList._

import scala.collection.mutable.ArrayBuffer
//只有跳转指令会被添加进来，需要啥加啥，需要手动添加
//b形的都是dst + pc ，b形的不用手动添加，别的得添加，指令实现里有dnpc的
object dnpcSrcList{
  val rd = 0.B
  val pc = 1.B
}
class dnpcSrcGen extends RawModule {
  val io = IO(new Bundle {
    val inst = Input(UInt(instW.W))
    val dnpcSrc = Output(Bool())
  })

  val dnpcSrcList: ArrayBuffer[Tuple2[Bool, Bool]] = new ArrayBuffer()
  map_type.map {
    case (k, v) => {
      v match {
        case "B" => dnpcSrcList.append((io.inst === k) -> pc)
        case _ =>
      }
    }
  }
  dnpcSrcList.append((io.inst ===JAL) -> pc)
  dnpcSrcList.append((io.inst ===JALR) -> rd)

  io.dnpcSrc := MuxCase( false.B,dnpcSrcList)


}
//需要哪条指令添哪条，需要手动添加,目前来说只有j
class jumpGen extends RawModule {
  val io = IO(new Bundle {
    val inst = Input(UInt(instW.W))
    val jump = Output(Bool())
  })
  //  val regSel = Cat(getOp(io.inst),getF3(io.inst))

  val  jumpList : Array[Tuple2[Bool,Bool]]  = Array(
    (io.inst ===JAL) -> true.B,
    (io.inst ===JALR) -> true.B
  )

  io.jump := MuxCase( false.B,jumpList)


}
//只有B，不需要手动添加
class branchGen extends RawModule {
  val io = IO(new Bundle {
    val inst = Input(UInt(instW.W))
    val branch = Output(Bool())
  })
  //  val regSel = Cat(getOp(io.inst),getF3(io.inst))

  val branchBuf: ArrayBuffer[Tuple2[Bool, Bool]] = new ArrayBuffer()
  map_type.map {
    case (k, v) => {
      v match {
        case "B" => branchBuf.append((io.inst === k) -> true.B)
        case _ =>
      }
    }
  }

  io.branch := MuxCase( false.B,branchBuf)


}
