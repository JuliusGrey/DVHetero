module ebProbe (
  input block ,
  input [31:0] inst
);

bit  inst_wire = inst== 32'h00100073 ;
task getabort(output bit abor);
    abor = inst_wire && !block;
endtask
export "DPI-C" task getabort;
endmodule