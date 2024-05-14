package RISCV.common

import Param._
import Chisel._

object Interface {
  class de2ex extends Bundle {
    val imme = Output(UInt(dataW.W))
    val dOut1 = Output(UInt(dataW.W))
    val dOut2 = Output(UInt(dataW.W))
    val dIn = Input(UInt(dataW.W))
    val rdDout = Output(UInt(dataW.W))//用于dma的第三个寄存器值
    //    val imme  =  Output(UInt(instW.W))
  }

  class ex2out extends Bundle {
    val ALUResOut = Output(UInt(dataW.W))
    val wdata = Output(UInt(dataW.W))
    val rdata = Input(UInt(dataW.W))
    //    val we = Output(Bool())
    //    val imme  =  Output(UInt(instW.W))
  }

  class CSRCtrlInterface extends Bundle {
    val csrrwen = Output(Bool())
    val csrswen = Output(Bool())
    val csrrsien = Output(Bool())
    val csrrcien = Output(Bool())
    val csrrcen = Output(Bool())
    val csrrwien = Output(Bool())
    val ecall = Output(Bool())
    val rfen = Output(Bool())
    val mepc2pc = Output(Bool())
  }

  class AXIInterface(
                      val RW_DATA_WIDTH :Int,
                      val RW_ADDR_WIDTH : Int,
                      val AXI_DATA_WIDTH:Int,
                      val AXI_ADDR_WIDTH :Int,
                      val AXI_ID_WIDTH :Int,
                      val AXI_USER_WIDTH :Int
                    ) extends Bundle {
    val  AXI_STRB_WIDTH = AXI_DATA_WIDTH/8
    // Advanced eXtensible Interface
    val axi_aw_ready_i  = Input(Bool())
    val axi_aw_valid_o  = Output(Bool())
    val axi_aw_addr_o = Output(UInt(AXI_ADDR_WIDTH.W))
    val axi_aw_prot_o = Output(UInt(3.W))
    val axi_aw_id_o = Output(UInt(AXI_ID_WIDTH.W)) // id
    val axi_aw_user_o = Output(UInt(AXI_USER_WIDTH.W))
    val axi_aw_len_o = Output(UInt(8.W))// 0
    val axi_aw_size_o = Output(UInt(4.W))
    val axi_aw_burst_o = Output(UInt(2.W))
    val axi_aw_lock_o = Output(Bool())
    val axi_aw_cache_o = Output(UInt(4.W))
    val axi_aw_qos_o = Output(UInt(4.W))
    val axi_aw_region_o = Output(UInt(4.W))

    val axi_w_ready_i = Input(Bool())
    val axi_w_valid_o = Output(Bool())
    val axi_w_data_o = Output(UInt(AXI_DATA_WIDTH.W))
    val axi_w_strb_o = Output(UInt(AXI_STRB_WIDTH.W))
    val axi_w_last_o = Output(Bool())
    val axi_w_user_o = Output(UInt(AXI_USER_WIDTH.W))

    val axi_b_ready_o = Output(Bool())
    val  axi_b_valid_i = Input(Bool())
    val axi_b_resp_i = Input(UInt(2.W))
    val axi_b_id_i = Input(UInt(AXI_ID_WIDTH.W))
    val axi_b_user_i = Input(UInt(AXI_USER_WIDTH))

    val axi_ar_ready_i = Input(Bool())
    val axi_ar_valid_o = Output(Bool())
    val axi_ar_addr_o = Output(UInt(AXI_ADDR_WIDTH.W))
    val axi_ar_prot_o = Output(UInt(3.W))
    val axi_ar_id_o = Output(UInt(AXI_ID_WIDTH.W))
    val axi_ar_user_o = Output(UInt(AXI_USER_WIDTH.W))
    val axi_ar_len_o = Output(UInt(8.W))
    val axi_ar_size_o = Output(UInt(3.W))
    val axi_ar_burst_o = Output(UInt(2.W))
    val axi_ar_lock_o = Output(Bool())
    val axi_ar_cache_o = Output(UInt(4.W))
    val axi_ar_qos_o = Output(UInt(4.W))
    //    val axi_ar_region_o = Output(UInt(4.W))

    val axi_r_ready_o = Output(Bool())
    val axi_r_valid_i = Input(Bool())
    val axi_r_resp_i = Input(UInt(2.W))
    val axi_r_data_i = Input(UInt(AXI_DATA_WIDTH.W))
    val axi_r_last_i = Input(Bool())
    val axi_r_id_i = Input(UInt(AXI_ID_WIDTH.W))
    val axi_r_user_i = Input(UInt(AXI_USER_WIDTH.W))
  }

  class SignalRAM(
                   val dataW: Int,
                   val addrW: Int
                 ) extends Bundle {
    val cen = Output(Bool())
    val wen = Output(Bool())
    val wdata = Output(UInt(dataW.W))
    val addr = Output(UInt(addrW.W))
    val wmask = Output(UInt(dataW.W))

    val rdata = Input(UInt(dataW.W))
  }

  class cpuRWIO(
                 val dataW: Int,
                 val addrW: Int
               ) extends Bundle {
    val valid = Input(Bool())
    val ready = Output(Bool())

    val data_read = Output(UInt(dataW.W))
    val data_write = Input((UInt(dataW.W)))
    val wen = Input(Bool())
    val addr = Input(UInt(addrW.W))
    val rsize = Input(UInt(2.W))

    val mask = Input(UInt(8.W))

  }

  class cacheIIO(
                  val AXI_DATA_WIDTH :Int,
                  val AXI_ADDR_WIDTH : Int
                )extends Bundle{
    //    val ar_ready_i = Input(Bool())
    val ar_valid_o = Output(Bool())
    val ar_addr_o = Output(UInt(AXI_ADDR_WIDTH.W))
    val ar_len_o = Output(UInt(8.W))
//    val ar_size_o = Output(UInt(3.W))

    //    val r_ready_o = Output(Bool())
    val r_valid_i = Input(Bool())
    val r_data_i = Input(UInt(AXI_DATA_WIDTH.W))
    val r_last_i = Input(Bool())

  }

  class cacheDIO(
                  val AXI_ADDR_WIDTH :Int,
                  val AXI_DATA_WIDTH : Int
                )extends Bundle{
    //    val ar_ready_i = Input(Bool())
    val ar_valid_o = Output(Bool())
    val ar_addr_o = Output(UInt(AXI_ADDR_WIDTH.W))
    val ar_len_o = Output(UInt(8.W))
//    val ar_size_o = Output(UInt(3.W))

    //    val r_ready_o = Output(Bool())
    val r_valid_i = Input(Bool())
    val r_data_i = Input(UInt(AXI_DATA_WIDTH.W))
    val r_last_i = Input(Bool())

    //暂时如果是写直通的话，端口其实就很简单了


    val w_valid_o = Output(Bool())
    val w_ready_i = Input(Bool())

    val w_data_o = Output((UInt(dataW.W)))
    val w_addr_o = Output(UInt(addrW.W))

    val w_mask_o = Output(UInt(8.W))




  }


  class AXIIO (val dataW :Int, val addrW :Int , val idW :Int)  extends Bundle {

    val awready = Input(Bool())
    val awvalid = Output(Bool())
    val awid = Output(UInt(idW.W))
    val awaddr = Output(UInt(addrW.W))
    val awlen = Output(UInt(8.W))
    val awsize = Output(UInt(3.W))
    val awburst = Output(UInt(2.W))

    val wready = Input(Bool())
    val wvalid = Output(Bool())
    val wdata = Output(UInt(dataW.W))
    val wstrb = Output(UInt((dataW/8).W))
    val wlast = Output(Bool())

    val bready = Output(Bool())
    val bvalid = Input(Bool())
    val bid = Input(UInt(idW.W))
    val bresp = Input(UInt(2.W))

    val arready = Input(Bool())
    val arvalid = Output(Bool())
    val arid = Output(UInt(idW.W))
    val araddr = Output(UInt(addrW.W))
    val arlen = Output(UInt(8.W))
    val arsize = Output(UInt(3.W))
    val arburst = Output(UInt(2.W))

    val rready = Output(Bool())
    val rvalid = Input(Bool())
    val rid = Input(UInt(idW.W))
    val rresp = Input(UInt(2.W))
    val rdata = Input(UInt(dataW.W))
    val rlast = Input(Bool())


  }

  class cacheIO(val AXI_DATA_WIDTH: Int,
                 val AXI_ADDR_WIDTH: Int

               ) extends Bundle {
    //    val ar_ready_i = Input(Bool())
    val ar_valid_o = Output(Bool())
    val ar_addr_o = Output(UInt(AXI_ADDR_WIDTH.W))
    val ar_len_o = Output(UInt(8.W))
    //    val ar_size_o = Output(UInt(3.W))

    //    val r_ready_o = Output(Bool())
    val r_valid_i = Input(Bool())
    val r_data_i = Input(UInt(AXI_DATA_WIDTH.W))
    val r_last_i = Input(Bool())

    //暂时如果是写直通的话，端口其实就很简单了


    val w_valid_o = Output(Bool())
    val w_ready_i = Input(Bool())

    val w_data_o = Output((UInt(dataW.W)))
    val w_addr_o = Output(UInt(addrW.W))

    val w_mask_o = Output(UInt(8.W))
    val wsize = Output(UInt(2.W))


  }




}
