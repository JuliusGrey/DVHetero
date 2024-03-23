package TOP.CGRAPort

import chisel3._
import chisel3.util._
//默认riscv比cgra宽
class SynIn(CGRAPortNum : Int , wCGRA:Int , wRISCV : Int) extends Module{
  val innum = (CGRAPortNum*wCGRA +wRISCV - 1) /wRISCV
  val io = IO(new Bundle() {
    val dataIn = Vec(innum, new Bundle() {
      val valid = Input(Bool())
      val ready = Output(Bool())
      val dataIn = Input(UInt(wRISCV.W))

    })
    val dataOut = Output(Vec(CGRAPortNum,UInt((wCGRA + 1).W)))

    val delayen = Input(Bool())
    val delayCycle = Input(UInt(wRISCV.W))
  })




  val inDatasWire = Wire(Vec(innum, UInt(wRISCV.W)))
  val outValidsWire = Wire(Vec(innum, Bool()))
  val ddataRst = outValidsWire.foldLeft(true.B)((a,b) => a && b)

  val delayReg = RegEnable(io.delayCycle(5,0),0.U,io.delayen)
  val delayCnt = Wire(UInt(6.W))
   delayCnt := RegEnable(
     Mux(
       (ddataRst || delayCnt =/= 0.U) && delayReg =/= delayCnt,
       delayCnt + 1.U,
       0.U
     ),
     0.U,
     delayReg=/=0.U
   )

//  val noWait = Mux(
//    delayReg === 0.U,
//    true.B ,
//    Mux(
//      (ddataRst || delayCnt =/= 0.U),
//      false.B,
//      true.B
//    )
//  )
val noWait = delayCnt === 0.U



  withReset(ddataRst || reset.asBool()){
    for (i <- 0 until innum) {
      io.dataIn(i).ready := !outValidsWire(i) && noWait
      inDatasWire(i) := RegEnable(io.dataIn(i).dataIn, 0.U,io.dataIn(i).valid && !outValidsWire(i) && noWait)
      outValidsWire(i) := RegEnable(io.dataIn(i).valid,false.B ,!outValidsWire(i) && io.dataIn(i).valid&&noWait )
    }
  }


  val outValid =  outValidsWire.foldLeft(true.B)((a,b) => a && b)

  val cnt = wRISCV/wCGRA
  for( i <- 0 until CGRAPortNum){
    val index = i/cnt
    val off = i%cnt
    val data = inDatasWire(index)((off + 1)*wCGRA - 1     ,off*wCGRA)
    io.dataOut(i) := Cat(outValid,data)
  }



}
object SynIngen extends App {
  chisel3.Driver.execute(args,() => new SynIn(2,2,2) )


}

