package RISCV.common

object Param {
    val instW = 32

    val dataW = 64

    val addrW =32

    val baseAddr = "h0000000080000000"

    val mstatus_ini = "ha00001800"

    val SRAMdataW = 128
    val SRAMaddrW = 6
    val CacheSram = 4


//    def SIMTEST = true
//    def HASDPIC = true

    val AXIID = 0

    val clintLow = "h2000000"
    val clintHigh = "h200BFFF"

    val skiplow = "h80000000"
    val skiphigh = "h8fffffff"

    def SIMTEST = true
    def HASDPIC = true
    val reduceTimeWay = false

    val hasBug = false

}
//修改dpic 修改基本地址 ，修改mmio范围
