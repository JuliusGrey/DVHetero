module Queue(
  input         clock,
  input         reset,
  output        io_enq_ready,
  input         io_enq_valid,
  input  [31:0] io_enq_bits,
  input         io_deq_ready,
  output        io_deq_valid,
  output [31:0] io_deq_bits
);


reg [8:0] wptr;
reg [8:0] rptr;

wire[7:0] waddr = wptr[7:0];
wire[7:0] raddr = rptr[7:0];

wire whigh = wptr[8];
wire rhigh = wptr[8];

wire full = waddr == raddr && whigh != rhigh;
wire empty = waddr == raddr && whigh == rhigh;
reg emptyreg;
always@(posedge clock)
if(reset) emptyreg <= 0; else emptyreg <= empty;
wire emptyover = emptyreg && !empty;

wire unExcept;

assign io_enq_ready = !full;
assign io_deq_valid = (!(empty || emptyreg)) && !unExcept;

wire enq = io_enq_ready && io_enq_valid;
wire deq = io_deq_ready && io_deq_valid;
 assign unExcept =   (wptr == rptr + 1) && enq;

always @(posedge clock )
if (reset)
    begin
        wptr <= 0;
        rptr <= 0;
    end
else begin
        if(enq) rptr <= rptr + 1; else rptr <= rptr;
        if(deq) wptr <= wptr + 1; else wptr <= wptr;
    end

 wire sram2rREn = emptyover|deq;
 wire[7:0] sram2RAddr = emptyover ? raddr : raddr + 1;

dsram dsram(
    .AA(sram2RAddr),
    .AB(waddr),
    .CENA(sram2rREn),
    .CENB(enq),
    .CLKA(clock),
    .CLKB(clock),
    .QA(io_deq_bits),
    .DB(io_enq_bits)
);
//当queue里有一个数据，并且同时读写的时候，就会出错 需要单独处理 ,这个解决方法也简单，这个时候只要不让读就好了,加一个unExcept

//整体思路：第一次写入数据的时候直接将数据读出来，读指针不变，读出后当前数据有效；随后每次读的时候用地址加1读数据；就用俩指针判断


endmodule