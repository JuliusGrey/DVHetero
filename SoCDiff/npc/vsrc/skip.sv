module skip (
  input  v
);

task getskip(output bit commit);
    commit = v;
endtask
export "DPI-C" task getskip;
endmodule