package CGRA.element.ALU

import chisel3._
import chisel3.util._

class BoothSel(val dataInW:Int) extends Module{
  val io = IO(new Bundle() {
    val y= Input(UInt(3.W))
    val x = Input(UInt(dataInW.W))
    val p = Output(UInt(dataInW.W))
    val c = Output(Bool())
  })
  val selNegative = io.y(2) & (io.y(1) & !io.y(0) | !io.y(1) & io.y(0))
  val selPositive = !io.y(2) & (io.y(1) & !io.y(0) | !io.y(1) & io.y(0))
  val selDoubleNegative = io.y(2) & !io.y(1) & !io.y(0)
  val selDoublePositive = !io.y(2) & io.y(1) & io.y(0)

  io.p :=  Fill(dataInW, selNegative) & (~io.x).asUInt |
    Fill(dataInW, selDoubleNegative) &Cat(~io.x(dataInW - 2,0),true.B) |
    Fill(dataInW, selPositive) & io.x|
    Fill(dataInW, selDoublePositive)  & Cat(io.x(dataInW - 2,0),false.B)

  io.c := selDoubleNegative | selNegative

}

class Mul(val muxW : Int) extends Module{
  val io = IO(new Bundle() {
    val mulValid = Input(Bool())
    val mulSigned = Input(Bool())
    val multiplicand =Input(UInt(muxW.W))
    val multiplier = Input(UInt(muxW.W))
    val outValid = Output(Bool())
    val resultHi = Output(UInt(muxW.W))
    val resultLow = Output(UInt(muxW.W))
  })
  //
  val signalMul = true
  if(signalMul){
    io.outValid :=RegNext( io.mulValid,false.B)
    val res = RegNext(io.multiplicand * io.multiplier,0.U)
    io.resultLow := res(muxW-1,0)
    io.resultHi := DontCare
  }else {
    val idleState :: mulState :: resultState :: Nil = Enum(3)
    val stateReg = RegInit(idleState)
    val isIdle = stateReg === idleState
    val isMul = stateReg === mulState
    val isRes = stateReg === resultState

    val cntMax = muxW / 2
    val cntW = log2Ceil(cntMax + 1)
    val cnt = RegInit(0.U(cntW.W))
    //待会试一下muxW = 64
    //cntW = 6 ， cntMax = 32


    val idleMux = Mux(io.mulValid, mulState, idleState)
    val mulMux = Mux(cnt === cntMax.U, resultState, mulState)
    //  val resultMux = Mux(io.block, resultState,idleState)
    stateReg := MuxLookup(
      stateReg,
      idleState,
      Array(
        idleState -> idleMux,
        mulState -> mulMux,
        resultState -> idleState
      )
    )
    cnt := Mux(isMul, cnt + 1.U, 0.U)


    val multiplierEx = Mux(
      io.mulSigned,
      Cat(Fill(2, io.multiplier(muxW - 1)), io.multiplier, false.B),
      Cat(0.U(2.W), io.multiplier, false.B)
    )
    val multiplicandEx = Mux(
      io.mulSigned,
      Cat(Fill(2, io.multiplicand(muxW - 1)), io.multiplicand),
      Cat(0.U(2.W), io.multiplicand)
    )

    val multiplierReg = RegInit(0.U((muxW + 3).W))
    multiplierReg := Mux(
      isIdle && io.mulValid,
      multiplierEx,
      Mux(
        isMul,
        Cat(0.U(2.W), multiplierReg(muxW + 2, 2)),
        multiplierReg
      )
    )

    val multiplicandReg = RegInit(0.U((muxW * 2 + 4).W))
    multiplicandReg := Mux(
      isIdle && io.mulValid,
      Cat(0.U((muxW + 2).W), multiplicandEx),
      Mux(
        isMul,
        Cat(multiplicandReg(muxW * 2 + 1, 0), 0.U(2.W)),
        multiplicandReg
      )
    )


    val boothIns = Module(new BoothSel(muxW * 2 + 4))
    val resReg = RegInit(0.U((muxW * 2 + 4).W))
    val addIns = Module(new Add(muxW * 2 + 4))

    boothIns.io.y := multiplierReg(2, 0).asUInt()
    boothIns.io.x := multiplicandReg

    addIns.io.a := boothIns.io.p
    addIns.io.cin := boothIns.io.c
    addIns.io.b := resReg
    //  val addRes = Wire(UInt(132.W))
    //  addRes := boothIns.io.p +  boothIns.io.c + resReg


    resReg := Mux(
      isIdle,
      0.U,
      Mux(
        isMul,
        addIns.io.sum,
        resReg
      )
    )

    //    resReg := Mux(
    //       isIdle,
    //      0.U,
    //      Mux(
    //        isMul,
    //        addRes,
    //        resReg
    //      )
    //    )

    io.resultLow := resReg(muxW - 1, 0).asUInt()
    io.resultHi := resReg(muxW * 2 - 1, muxW).asUInt()

    io.outValid := isRes


  }
}