module difftest (
  input  v
);

task difftestcommit(output bit commit);
    commit = v;
endtask
export "DPI-C" task difftestcommit;
endmodule
