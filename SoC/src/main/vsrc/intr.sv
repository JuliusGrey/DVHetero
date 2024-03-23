module intr (
  input  intr
);

task getintr(output bit commit);
    commit = intr;
endtask
export "DPI-C" task getintr;
endmodule