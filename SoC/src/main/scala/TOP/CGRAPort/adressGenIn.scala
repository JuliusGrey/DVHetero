package TOP.CGRAPort

import chisel3._
import chisel3.util._
import chisel3.util.experimental.BoringUtils
//这个模块接收来自CGRA端口的输入，想syn端口传递数据
//首先需要生成每一个端口的索引，记录每一个端口的掩码信号
//需要记录当前的索引
//首先需要通过当前的缩影选择一个端口的优先编码器，输出作为低输出端口的索引；注意这个和输出那个不一样的地方是不需要循环
//同时需要第一个输出的索引选择一个优先编码器，作为高输出端口的索引，以上两个为组合逻辑，高位的如果是0就不写入了
//将第二次的输出记录为下次索引的寄存器
//需要一组寄存器记录输入1的数量，用一个计数器控制计数的次数，计数达到输入1的数量除以2之后就复位


class adressGenIn(CGRAPortNum : Int) extends Module{
  val io = IO (new Bundle{
    val validList = Vec(CGRAPortNum,Input(Bool()))
    val update = Input(Bool())
    val clear = Input(Bool())
    val sel1 = Output(UInt(4.W))
    val sel2 = Output(UInt(4.W))
  })

// 由掩码获得一堆valid信号
  val validLists = (0 until CGRAPortNum ).map{
    index => io.validList.takeRight(CGRAPortNum - index)
  }
 //获取所有端口的索引
  val indexAll = (0 until CGRAPortNum ).map{i => i.U}
  val indexLists =  (0 until CGRAPortNum ).map{
    index =>indexAll.takeRight(CGRAPortNum - index)
  }
//给每个端口一个优先编码器
  val porMuxList = (0 until CGRAPortNum).map { i =>
    i.U -> PriorityMux(validLists(i), indexLists(i))
  }
//用于选择的索引
  val indexSelL = Wire(UInt(4.W))
  val indexSelH = Wire(UInt(4.W))
  val indexSelReg = Wire(UInt(4.W))
  withReset(io.clear || reset.asBool()) {
    indexSelReg := RegEnable(indexSelH + 1.U, 0.U, io.update)
  }
//由选择索引得到低地址和高地址
  indexSelL := MuxLookup(
    indexSelReg,
    0.U,
    porMuxList
  )
  indexSelH := MuxLookup(
    indexSelL + 1.U,
    0.U,
    porMuxList
  )

  io.sel1 := indexSelL
  io.sel2 :=indexSelH


}

object adressGenIngen extends App {
  chisel3.Driver.execute(args,() => new adressGenIn(16) )


}
