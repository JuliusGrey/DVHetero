package RISCV.BUS

import chisel3._
import chisel3.util._
import RISCV.common.Interface.{AXIIO, cpuRWIO}
import RISCV.common.Param.{AXIID, addrW, dataW}

class AXI4 extends Module{
  val io = IO (new Bundle {

    val axiIO = new AXIIO(64,32,4)
    val vrIO = new cpuRWIO(dataW , addrW )
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
  val r_idle_st = Mux(io.vrIO.valid && !io.vrIO.wen,r_req,r_idle)
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
  val isReq = rd_state===r_req
  val isData = rd_state === r_data

  //读地址通道
  io.axiIO.arvalid := isReq
  io.axiIO.araddr := Mux(isReq, io.vrIO.addr, 0.U)
  io.axiIO.arid := Mux(isReq, AXIID.U, 0.U)
  io.axiIO.arlen := 0.U
  io.axiIO.arsize := io.vrIO.rsize
  io.axiIO.arburst := Mux(isReq, AXI_BURST_TYPE_INCR, 0.U)

  //读数据通道
  io.axiIO.rready := isData ||isReq
  io.vrIO.data_read := io.axiIO.rdata

  val rReady = ((isReq && io.axiIO.arready) || isData) &&io.axiIO.rlast&& io.axiIO.rvalid


  //写状态 目前不需要burst，
  val w_idle :: w_req :: w_data :: w_b ::Nil = Enum(4)
  val valid_c = Cat(io.axiIO.awready,io.axiIO.wready)
  val w_state = RegInit(w_idle)
  val w_idle_st = Mux(io.vrIO.wen && io.vrIO.valid , w_req,w_idle)
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

  val isWReq = w_state===w_req
  val w_state_write = w_state === w_req || w_state === w_data
  val w_state_resp = w_state === w_b
  val wReady = (w_state_resp && io.axiIO.bvalid)



  io.vrIO.ready := rReady || wReady




  //写地址通道
  io.axiIO.awvalid:= isWReq
  io.axiIO.awaddr :=Mux(isWReq, io.vrIO.addr,0.U)
  io.axiIO.awid :=Mux(isWReq, AXIID.U,0.U)
  io.axiIO.awlen := 0.U
  io.axiIO.awsize := io.vrIO.rsize
  io.axiIO.awburst := AXI_BURST_TYPE_INCR


  // 写数据通道
  io.axiIO.wvalid := w_state_write
  io.axiIO.wdata :=Mux(w_state_write, io.vrIO.data_write,0.U)
  io.axiIO.wstrb:=Mux(w_state_write, io.vrIO.mask,0.U)
  io.axiIO.wlast := w_state_write

  // 写应答通道
  io.axiIO.bready := w_state_resp

//  printf("axi.vrIO.valid%d\n",io.vrIO.valid)
//  printf("axi.vrIO.wen %d\n",io.vrIO.wen)




}
object AXIgen extends App {
  chisel3.Driver.execute(args,() => new AXI4 )
}
