package CGRA.element.ALU

import CGRA.parameter.Param._
import chisel3._
import chisel3.util._

//object SignExt {
//  def apply(a: UInt, len: Int) = {
//    val aLen = a.getWidth
//    val signBit = a(aLen - 1)
//    if (aLen >= len) a(len - 1, 0) else Cat(Fill(len - aLen, signBit), a)
//  }
//}


class ALU( elename :String, funsel :Int ,  width :Int) extends Module{
//  println("ALUname is " + elename)
  override val desiredName = elename
  val ops = OpInfo.fuget(funsel)
  val maxNumOperands = ops.map(OpInfo.getOperandNum(_)).max
  val cfgDataWidth = if(hasAcc) log2Ceil(OPC.numOPC) + accCounterW+accInitW else log2Ceil(OPC.numOPC)
  val io = IO(new Bundle {
    //    val en = Input(Bool())
    val cfg = Input(UInt(cfgDataWidth.W))
    val inputs = Input(Vec(maxNumOperands, UInt((width + 1).W)))
    val outputs = Output(Vec(1, UInt((width + 1).W)))
    //    val valid = Output(Bool())
  })

  //同步单元,目前先按两个端口来，后面要是有不是两个端口的再修改把
  val inputsWire = Wire(Vec(maxNumOperands, UInt((width + 1).W)))
  //  val  inputsWireOut = Wire(Vec(maxNumOperands, UInt((width + 1).W)))
//  val inputRst = inputsWire.foldLeft(true.B)((a,b) => a && b(width))
val inputRstArray = ops.map{
  op =>
    op.id.U -> (
      OpInfo.getOperandNum(op) match {
        case 1 => inputsWire(0)(width)
        case 2 => (inputsWire(0)(width) && inputsWire(1)(width))
//        case 3 => (inputsWire(0)(width) && inputsWire(1)(width) && inputsWire(2)(width))
      }
    )
}
  val inputRst = MuxLookup(
    io.cfg(log2Ceil(OPC.numOPC) - 1 , 0),
    false.B,
    inputRstArray
  )
//val inputRst = MuxLookup(
//  io.cfg,
//  false.B,
//  Array(
//    OPC.PASS.id.U -> inputsWire(0)(width),
//    OPC.ADD.id.U -> (inputsWire(0)(width) && inputsWire(1)(width)),
//    OPC.SUB.id.U -> (inputsWire(0)(width) && inputsWire(1)(width)),
//    OPC.MUL.id.U -> (inputsWire(0)(width) && inputsWire(1)(width)),
//    OPC.AND.id.U ->( inputsWire(0)(width) && inputsWire(1)(width)),
//    OPC.OR.id.U -> (inputsWire(0)(width) && inputsWire(1)(width)),
//    OPC.XOR.id.U -> (inputsWire(0)(width) && inputsWire(1)(width)),
//    OPC.SHL.id.U -> (inputsWire(0)(width) && inputsWire(1)(width)),
//    OPC.LSHR.id.U -> (inputsWire(0)(width) && inputsWire(1)(width)),
//    OPC.ASHR.id.U -> (inputsWire(0)(width) && inputsWire(1)(width)),
//    OPC.EQ.id.U -> (inputsWire(0)(width) && inputsWire(1)(width)),
//    OPC.NE.id.U -> (inputsWire(0)(width) && inputsWire(1)(width)),
//    OPC.LE.id.U -> (inputsWire(0)(width) && inputsWire(1)(width)),
//    OPC.LT.id.U -> (inputsWire(0)(width) && inputsWire(1)(width)),
//    OPC.SEL.id.U -> (inputsWire(0)(width) && inputsWire(1)(width) && inputsWire(2)(width))
//  )
//)
  for( i <- 0 until maxNumOperands){
  withReset((inputRst&& !io.inputs(i)(width)) || reset.asBool()){

      inputsWire(i) := RegEnable(io.inputs(i),0.U,io.inputs(i)(width) )
    }
  }


  val en =  inputRst

  val resMap = ops.map { op =>
    (op.id.U -> (
      op match {
//        case OPC.PASS => inputsWire(0)
        case OPC.PASS => 0.U
        case OPC.ADD => Cat(en,(inputsWire(0)(width - 1 , 0) +  inputsWire(1)(width - 1 , 0)))
        case OPC.SUB => Cat(en,(inputsWire(0)(width - 1 , 0) -  inputsWire(1)(width - 1 , 0)))
        case OPC.MUL => {
          val mulins = Module(new Mul(width))
          mulins.io.mulValid := en && io.cfg(log2Ceil(OPC.numOPC) - 1 , 0) === op.id.U
          mulins.io.mulSigned := false.B
          mulins.io.multiplicand := inputsWire(0)(width - 1 , 0)
          mulins.io.multiplier := inputsWire(1)(width - 1 , 0)
          Cat(mulins.io.outValid ,mulins.io.resultLow)
        }
        case OPC.DIV => {
          val divins = Module(new Div(width))
          divins.io.divValid := en && io.cfg(log2Ceil(OPC.numOPC) - 1 , 0) === op.id.U
          divins.io.divSigned := false.B
          divins.io.dividend := inputsWire(0)(width - 1 , 0)
          divins.io.divisor := inputsWire(1)(width - 1 , 0)
          Cat(divins.io.outValid, divins.io.quotient)
        }
        case OPC.AND => Cat(en,(inputsWire(0)(width - 1 , 0) &  inputsWire(1)(width - 1 , 0)))
        case OPC.OR => Cat(en,(inputsWire(0)(width - 1 , 0) |  inputsWire(1)(width - 1 , 0)))
        case OPC.XOR => Cat(en,(inputsWire(0)(width - 1 , 0) ^  inputsWire(1)(width - 1 , 0)))
        case OPC.SHL => Cat(en,(inputsWire(0)(width - 1 , 0) <<  inputsWire(1)(width - 1 , 0)))
        case OPC.LSHR => Cat(en,(inputsWire(0)(width - 1 , 0) >>  inputsWire(1)(width - 1 , 0)))
        case OPC.ASHR => Cat(en,(inputsWire(0)(width - 1 , 0).asSInt >>  inputsWire(1)(width - 1 , 0)))
        case OPC.EQ => Cat(en,(inputsWire(0)(width - 1 , 0) ===  inputsWire(1)(width - 1 , 0)))
        case OPC.NE => Cat(en,(inputsWire(0)(width - 1 , 0) =/=  inputsWire(1)(width - 1 , 0)))
        case OPC.LE => Cat(en,(inputsWire(0)(width - 1 , 0) >=  inputsWire(1)(width - 1 , 0)))
        case OPC.LT => Cat(en,(inputsWire(0)(width - 1 , 0) <  inputsWire(1)(width - 1 , 0)))
        case OPC.SEL => Cat(en && inputsWire(2)(width),Mux(inputsWire(2)(width - 1 , 0)(0),inputsWire(1)(width - 1 , 0),inputsWire(0)(width - 1 , 0)))
        case OPC.ACC => {
          if(hasAcc){
            val accins = Module(new Acc)
            accins.io.counter := io.cfg(log2Ceil(OPC.numOPC) +accCounterW -1 ,log2Ceil(OPC.numOPC))
            accins.io.iniVal := io.cfg(log2Ceil(OPC.numOPC) + accCounterW+accInitW - 1, log2Ceil(OPC.numOPC) +accCounterW)
            accins.io.inputVal :=RegNext( io.inputs(0),0.U)//这个由于只有一个数，不需要同步,但还是需要打一拍
            accins.io.outputVal
          }else{
            0.U
          }
        }
      }
      ))
  }

  io.outputs(0):= MuxLookup(
    io.cfg(log2Ceil(OPC.numOPC) - 1 , 0),
    0.U,
    resMap
  )
//  println("OPC.numOPC is  " + OPC.numOPC)
//  println("log2Ceil(OPC.numOPC) is  " + log2Ceil(OPC.numOPC))
//  println("log2Ceil(OPC.numOPC) - 1  is " + (log2Ceil(OPC.numOPC) - 1 ))

  //  io.outputs(0) := resWire(width - 1 , 0)
  //  io.valid := resWire(width)
}

object AluGen extends App {
  chisel3.Driver.execute(args,() => new ALU("alu0",2,32) )


}
