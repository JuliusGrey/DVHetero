package RISCV.IFU

import chisel3._
import chisel3.util._
import RISCV.common.Param.instW
// class ebProbe  extends BlackBox with HasBlackBoxInline {
//   val io = IO(new Bundle {
//     val inst = Input(UInt(instW.W))
//   })
//   setInline("ebProbe.v",
//     s"""|module ebProbe (
//        |  input [31:0] inst
//        |);

//        |bit  inst_wire = inst== 32'h00100073 ;
//        |task getabort(output bit abor);
//        |    abor = inst_wire;
//        |endtask
//        |export "DPI-C" task getabort;
//        |endmodule
//      """.stripMargin)

// }

class ebProbe  extends BlackBox {
  val io = IO(new Bundle {
    val block= Input(Bool())
    val inst = Input(UInt(instW.W))
  })

}
