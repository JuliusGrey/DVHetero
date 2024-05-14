package CGRA.element.ALU




import scala.collection.mutable.ListBuffer

/**
 * Operation Code
 */
object OPC extends Enumeration {
  type OPC = Value
  // Operation Code
  val NULL,    // passthrough from in to out
  ADD,     // add
  SUB,     // substrate
  MUL,     // multiply
   DIV,     // divide
//   MOD,     // modulo
  // MIN,
  // NOT,
  AND,
  OR,
  XOR,
  SHL,     // shift left
  LSHR,    // logic shift right
  ASHR,    // arithmetic shift right
  //	  CSHL,    // cyclic shift left
  //	  CSHR,    // cyclic shift right
  EQ,      // equal to
  NE,      // not equal to
  LT,      // less than
  LE,      // less than or equal to
  //		SAT,		 // saturate value to a threshold
  //		MGE,		 // merge two data
  //		SPT,	   // split one data to two
  PASS,
  ACC= Value  // Select

  val numOPC = this.values.size

  def printOPC = {
    this.values.foreach{ op => println(s"$op\t: ${op.id}")}
  }
}

/**
 *  Operation Information
 */
object OpInfo {
  import CGRA.element.ALU.OPC._
//  def String2Int (opName : String) : Int = {
//    opName match {
//      case "PASS" => PASS.id
//      case "ADD" => ADD.id
//      case "SUB" => SUB.id
//      case "MUL" => MUL.id
//      case "PASS" => PASS.id
//      case "PASS" => PASS.id
//      case "PASS" => PASS.id
//      case "PASS" => PASS.id
//      case "PASS" => PASS.id
//      case "PASS" => PASS.id
//      case "PASS" => PASS.id
//      case "PASS" => PASS.id
//      case "PASS" => PASS.id
//      case "PASS" => PASS.id
//      case "PASS" => PASS.id
//      case "PASS" => PASS.id
//
//
//    }
//  }
  val String2IntMap : Map[String,Int] = Map(
  "NULL" -> NULL.id,
  "ADD" -> ADD.id,
  "SUB" -> SUB.id,
  "MUL" -> MUL.id,
  "DIV" -> DIV.id,
  "AND" -> AND.id,
  "OR" -> OR.id,
  "XOR" -> XOR.id,
  "SHL" -> SHL.id,
  "LSHR" -> LSHR.id,
  "ASHR" -> ASHR.id,
  "EQ" -> EQ.id,
  "NE" -> NE.id,
  "LT" -> LT.id,
  "LE" -> LE.id,
  "PASS" -> PASS.id,
  "ACC" -> ACC.id
)


  def fupar( op : List[OPC.OPC]) : Int= {
//    var par= 0
//    op.map{ i => par & 1 <<(i.id)}
//    par
    op.foldLeft(0)((a,b) => {
      a | 1 << (b.id)
    })
  }

  def fuget( op : Int) :ListBuffer[OPC.OPC]= {
    val oplist = new ListBuffer[OPC.OPC]
    for( i <- 0 until OPC.numOPC ){
      if((op & ( 1 << i))> 0){
        oplist.append(OPC(i))
      }
    }
    oplist
  }



  val OpInfoMap: Map[OPC.OPC, List[Int]] = Map(
    // OPC -> List(NumOperands, NumRes, Latency, Operands-Commutative)
    // latency including the register outside ALU
    OPC.NULL -> List(1, 1, 1, 0),
    OPC.ADD  -> List(2, 1, 1, 1),
    OPC.SUB  -> List(2, 1, 1, 0),
    OPC.MUL  -> List(2, 1, 1, 1),
    // OPC.DIV  -> List(2, 1, 1, 0),
    // OPC.MOD  -> List(2, 1, 1, 0),
    // OPC.MIN  -> List(2, 1, 1, 1),
    OPC.AND  -> List(2, 1, 1, 1),
    OPC.OR   -> List(2, 1, 1, 1),
    OPC.XOR  -> List(2, 1, 1, 1),
    OPC.SHL  -> List(2, 1, 1, 0),
    OPC.LSHR -> List(2, 1, 1, 0),
    OPC.ASHR -> List(2, 1, 1, 0),
    //		OPC.CSHL -> List(2, 1, 1, 0),
    //		OPC.CSHR -> List(2, 1, 1, 0),
    OPC.EQ   -> List(2, 1, 1, 1),
    OPC.NE   -> List(2, 1, 1, 1),
    OPC.LT   -> List(2, 1, 1, 0),
    OPC.LE   -> List(2, 1, 1, 0),
    //		OPC.SAT  -> List(2, 1, 1, 0),
    OPC.PASS  -> List(1, 1, 1, 0),
      OPC.ACC  -> List(1, 1, 1, 0)
  )

  def getOperandNum(op: OPC.OPC): Int = {
    OpInfoMap(op)(0)
  }

  def getResNum(op: OPC.OPC): Int = {
    OpInfoMap(op)(1)
  }

  def getLatency(op: OPC.OPC): Int = {
    OpInfoMap(op)(2)
  }

  def isCommutative(op: OPC.OPC): Int = {
    OpInfoMap(op)(3)
  }


  private var width = 32
  private var high = width - 1

  def apply(opWidth: Int) = {
    width = opWidth
    high = width - 1
    this
  }


}
