package RISCV.IDU.CtrlUnit

import chisel3.util.MuxCase
import chisel3._
import RISCV.common.Param._
import RISCV.common.instList._

import scala.collection.mutable.ArrayBuffer

//U，IjR都有   SB没有  不用手动添加
class regEnGen extends RawModule {
  val io = IO(new Bundle {
    val inst = Input(UInt(instW.W))
    val regEn = Output(Bool())
  })
//  val regSel = Cat(getOp(io.inst),getF3(io.inst))

//  val regList : Array[Tuple2[Bool,Bool]]  = Array(
//    (io.inst ===ADDI) -> true.B,
//    (io.inst ===AUIPC) -> true.B,
//    (io.inst ===LUI) -> true.B,
//    (io.inst ===JAL) -> true.B,
//    (io.inst ===JALR) -> true.B,
//    (io.inst ===LD) -> true.B,
//    (io.inst ===LBU) -> true.B,
//  )

  val regList: ArrayBuffer[Tuple2[Bool, Bool]] = new ArrayBuffer()
  map_type.map {
    case (k, v) => {
      v match {
        case "U" => regList.append((io.inst === k) -> true.B)
        case "I" => regList.append((io.inst === k) -> true.B)
        case "J" => regList.append((io.inst === k) -> true.B)
        case "R" => regList.append((io.inst === k) -> true.B)
        case "S" => regList.append((io.inst === k) -> false.B)
        case "B" => regList.append((io.inst === k) -> false.B)
        case _ =>
      }
    }
  }

  io.regEn := MuxCase( false.B,regList)

}
