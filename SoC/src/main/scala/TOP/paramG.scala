package TOP

import chisel3._

object paramG {
  def cgraColNum = 16
  def cgraRawNum = 4
  def cgraInPort = 16
  def cgraOutPort = 16
  def cgraWidth = 32
  def riscvWidth = 64
  def synInNum = (cgraInPort*cgraWidth +riscvWidth - 1) /riscvWidth
  def cgraBaseAddr = "h02010000".U
  def cgraOutAddr ="h02010000".U +  (synInNum*riscvWidth/8 ).U
  def cgraFC = true
  def memIP = true


  def addrIn256 = "h02010000".U
  val addrOut256 = "h02010040".U
  def addrCfg256 = "h02010080".U
  def addrDelay256 = "h02010090".U



  def addrCGRAbase = addrIn256
  def addrCGRAHigh = "h020100a0".U

  val clintLow = "h2000000"
  val clintHigh = "h200BFFF"

}
