package RISCV.IDU

import Chisel.{Cat, MuxCase, MuxLookup}
import RISCV.common.Param._
import chisel3._
import chisel3.util.Fill
import RISCV.common.instDe._
import RISCV.common.instList._
import immeList._

import scala.collection.mutable.ArrayBuffer

object immeList {

  object SignExt {
    def apply(a: UInt, len: Int) = {
      val aLen = a.getWidth
      val signBit = a(aLen-1)
      if (aLen >= len) a(len-1,0) else Cat(Fill(len - aLen, signBit), a)
    }
  }

  object ZeroExt {
    def apply(a: UInt, len: Int) = {
      val aLen = a.getWidth
      if (aLen >= len) a(len-1,0) else Cat(0.U((len - aLen).W), a)
    }
  }
  //  def I_Imme(instruction:UInt) : UInt = {
  //     val immeHigh = Cat(VecInit(Seq.fill(52){instruction(11,11)}))
  //    val imme =  Cat( immeHigh,instruction(11,0))
  //    imme
  //  }
  }

  class immeGen extends RawModule {
    val io = IO(new Bundle {
      val inst = Input(UInt(instW.W))
      val imme = Output(UInt(dataW.W))
    })
    val Iimm = SignExt(io.inst(31, 20) ,dataW)
    val Simm = SignExt(Cat(io.inst(31, 25), io.inst(11, 7)) ,dataW)
    val Bimm = SignExt(Cat(io.inst(31), io.inst(7), io.inst(30, 25), io.inst(11, 8), 0.U(1.W)) ,dataW)
    val Uimm = SignExt(Cat(io.inst(31, 12), 0.U(12.W)) ,dataW)
    val Jimm = SignExt(Cat(io.inst(31), io.inst(19, 12), io.inst(20), io.inst(30, 21), 0.U(1.W)) ,dataW)
    //    val Zimm = io.inst(19, 15).zext

    val immeBuf: ArrayBuffer[Tuple2[Bool, UInt]] = new ArrayBuffer()
    map_type.map {
      case (k, v) => {
        v match {
          case "I" => immeBuf.append((io.inst === k) -> Iimm)
          case "J" => immeBuf.append((io.inst === k) -> Jimm)
          case "U" => immeBuf.append((io.inst === k) -> Uimm)
          case "S" => immeBuf.append((io.inst === k) -> Simm)
          case "B" => immeBuf.append((io.inst === k) -> Bimm)
          case _ =>
        }
      }
    }

    immeBuf.append((io.inst === CSRRW) -> Iimm)
    immeBuf.append((io.inst === CSRRS) -> Iimm)
    immeBuf.append((io.inst === CSRRSI) -> Iimm)
    immeBuf.append((io.inst === CSRRCI) -> Iimm)
    immeBuf.append((io.inst === CSRRC) -> Iimm)

    io.imme := MuxCase(0.U, immeBuf)

  }
//}

object addr extends  App{
  Driver.emitVerilog(new immeGen)
}
