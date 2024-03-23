package RISCV.EXU.ALUOp

import chisel3._
import chisel3.util._
import RISCV.common.Param.{SIMTEST, dataW}

import scala.collection.mutable.ListBuffer
class add(val addW:Int) extends Module {
  val io = IO (new Bundle() {
    val cin = Input(Bool())
    val a = Input(UInt(addW.W))
    val b = Input(UInt(addW.W))
    val sum = Output(UInt(addW.W))
    val cout = Output(Bool())
  })
  if(SIMTEST) {
    val res = Wire(UInt((addW + 1).W ))
    res:=  Cat (false.B,io.a) + Cat (false.B,io.b) +io.cin
    io.sum := res(addW - 1,0)
    io.cout := res(addW)
  }else {
    val gi = (io.a & io.b).asBools()
    val pi = (io.a ^ io.b).asBools()

    val couts = Wire(Vec(addW, Bool()))

    for (i <- 0 until addW) {
      //以i = 3为例
      //此处计算G3+G2P3+G1P3P2+G0P3P2P1 + C0p3p2p1p0
      val gBuf: ListBuffer[Bool] = new ListBuffer()
      val pBuf: ListBuffer[Bool] = new ListBuffer()
      for (j <- 0 to i) {
        //以j=3为例
        //此处计算添加P3P2P1，和G0到buf中
        val pListSub: ListBuffer[Bool] = new ListBuffer()
        pListSub.append(true.B)
        for (k <- 0 until j) { //此处计算P3P2P1
          pListSub.append(pi(i - k))
        }
        pBuf.append(pListSub.reduceLeft(_ && _))
        gBuf.append(gi(i - j))

      }
      gBuf.append(io.cin)
      pBuf.append(
        (0 to i).foldLeft(true.B)((a, b) => a && pi(b))
      )

      couts(i) := (0 to i + 1).foldLeft(false.B)((a, b) => a || (gBuf(b) && pBuf(b)))
      //      reduce( (a,b) => gBuf(a)&&pBuf(a) || gBuf(b)&&pBuf(b) )

    }
    val sumVec = Wire(Vec(addW, Bool()))
    (0 until addW - 1).map {
      i => {
        sumVec(i + 1) := couts(i) ^ pi(i + 1)
      }
    }

    sumVec(0) := io.cin ^ pi(0)

    io.cout := couts(addW - 1)
    io.sum := sumVec.asUInt()

  }
}








object addgen extends App {
  chisel3.Driver.execute(args,() => new add(132) )


}


