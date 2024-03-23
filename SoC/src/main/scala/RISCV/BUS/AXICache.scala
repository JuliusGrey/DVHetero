package RISCV.BUS

import chisel3._
import chisel3.util._
import RISCV.common.Interface.{AXIIO, cacheIO}
import RISCV.common.Param.{AXIID, addrW, dataW}

class AXICache extends Module{
  val io = IO (new Bundle {

    val axiIO = new AXIIO
    val cache =  Flipped(new cacheIO(dataW,addrW))

  } )
  def AXI_BURST_TYPE_INCR = 1.U(2.W)


  //  val w_state_addr = DontCare
  //  val w_state_write = DontCare
  //  val w_state_resp = DontCare
  //  val r_state_addr = DontCare
  //  val r_state_read = DontCare

  //读状态机
  val r_idle :: r_req :: r_data :: Nil = Enum(3)
  val rd_state = RegInit(r_idle)
  val r_idle_st = Mux(io.cache.ar_valid_o ,r_req,r_idle)
  val r_req_st = Mux(
    io.axiIO.arready,
    Mux(
      io.axiIO.rlast && io.axiIO.rvalid,
      r_idle,
      r_data
    ),
    r_req)
  val r_data_st  = Mux(io.axiIO.rlast&& io.axiIO.rvalid,r_idle,r_data)

  rd_state := MuxLookup(
    rd_state,
    rd_state,
    Array(
      r_idle -> r_idle_st,
      r_req -> r_req_st,
      r_data -> r_data_st
    )
  )
  val isReq = rd_state === r_req
  val isData = rd_state === r_data


  io.cache.r_last_i := io.axiIO.rlast
  io.cache.r_valid_i := io.axiIO.rvalid
  io.cache.r_data_i := io.axiIO.rdata




  //读地址通道
  io.axiIO.arvalid:= isReq
  io.axiIO.araddr := Mux(isReq, io.cache.ar_addr_o, 0.U)
  io.axiIO.arid := Mux(isReq, AXIID.U, 0.U)
  io.axiIO.arlen :=Mux(isReq,  io.cache.ar_len_o,0.U)
  io.axiIO.arsize := Mux(isReq, 3.U, 0.U)
  io.axiIO.arburst := Mux(isReq, AXI_BURST_TYPE_INCR, 0.U)

  //读数据通道
  io.axiIO.rready:=  isData ||isReq
  //  io.data_read_o := io.axi.axi_r_data_i




  //写状态 目前不需要burst，
  val w_idle :: w_req :: w_data :: w_b ::Nil = Enum(4)
  val valid_c = Cat(io.axiIO.awready,io.axiIO.wready)
  val w_state = RegInit(w_idle)
  val w_idle_st = Mux(io.cache.w_valid_o , w_req,w_idle)
  val w_req_st = MuxLookup(
    valid_c,
    w_req,
    Array(
      "b10".U(2.W) -> w_data,
      "b11".U(2.W) -> w_b
    )
  )
  val w_data_st = Mux(io.axiIO.wready,w_b,w_data)
  val w_b_st = Mux(io.axiIO.bvalid, w_idle, w_b)
  w_state := MuxLookup(
    w_state,
    w_state,
    Array(
      w_idle -> w_idle_st,
      w_req -> w_req_st,
      w_data -> w_data_st,
      w_b -> w_b_st
    )
  )

  val isWIdle = w_state === w_idle
  val isWReq = w_state === w_req
  val isWData = w_state === w_data
  val isWB = w_state === w_b

  io.cache.w_ready_i := io.axiIO.bvalid &&isWB

  //  val w_state_addr = w_state===w_req
  //  val w_state_write = w_state === w_req || w_state === w_data
  //  val w_state_resp = w_state === w_b








  //写地址通道
  io.axiIO.awvalid := isWReq
  io.axiIO.awaddr := Mux(isWReq, io.cache.w_addr_o, 0.U)
  io.axiIO.awid := Mux(isWReq, AXIID.U, 0.U)
  io.axiIO.awlen := 0.U
  io.axiIO.awsize := io.cache.wsize
  io.axiIO.awburst := AXI_BURST_TYPE_INCR


  // 写数据通道
  io.axiIO.wvalid := isWReq || isWData
  io.axiIO.wdata := Mux(isWReq || isWData, io.cache.w_data_o, 0.U)
  io.axiIO.wstrb := Mux(isWReq || isWData, io.cache.w_mask_o, 0.U)
  io.axiIO.wlast :=isWReq || isWData

  // 写应答通道
  io.axiIO.bready := isWB






}
//object AXIgen extends App {
//  chisel3.Driver.execute(args,() => new AXI4(64,32,64,32,4,1) )
//}
