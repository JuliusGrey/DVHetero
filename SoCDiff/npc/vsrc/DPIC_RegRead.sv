module   DPIC_RegRead  ( // @[regFile.scala 18:32]
           input[63:0] inst_0,
           input[63:0] inst_1,
           input[63:0] inst_2,
           input[63:0] inst_3,
           input[63:0] inst_4,
           input[63:0] inst_5,
           input[63:0] inst_6,
           input[63:0] inst_7,
           input[63:0] inst_8,
           input[63:0] inst_9,
           input[63:0] inst_10,
           input[63:0] inst_11,
           input[63:0] inst_12,
           input[63:0] inst_13,
           input[63:0] inst_14,
           input[63:0] inst_15,
           input[63:0] inst_16,
           input[63:0] inst_17,
           input[63:0] inst_18,
           input[63:0] inst_19,
           input[63:0] inst_20,
           input[63:0] inst_21,
           input[63:0] inst_22,
           input[63:0] inst_23,
           input[63:0] inst_24,
           input[63:0] inst_25,
           input[63:0] inst_26,
           input[63:0] inst_27,
           input[63:0] inst_28,
           input[63:0] inst_29,
           input[63:0] inst_30,
           input[63:0] inst_31,
           input[63:0] pc
         );
          logic [63:0] regfile [32 :0 ];
          assign regfile[0] = inst_0;
          assign regfile[1] = inst_1;
          assign regfile[2] = inst_2;
          assign regfile[3] = inst_3;
          assign regfile[4] = inst_4;
          assign regfile[5] = inst_5;
          assign regfile[6] = inst_6;
          assign regfile[7] = inst_7;
          assign regfile[8] = inst_8;
          assign regfile[9] = inst_9;
          assign regfile[10] = inst_10;
          assign regfile[11] = inst_11;
          assign regfile[12] = inst_12;
          assign regfile[13] = inst_13;
          assign regfile[14] = inst_14;
          assign regfile[15] = inst_15;
          assign regfile[16] = inst_16;
          assign regfile[17] = inst_17;
          assign regfile[18] = inst_18;
          assign regfile[19] = inst_19;
          assign regfile[19] = inst_19;
          assign regfile[20] = inst_20;
          assign regfile[21] = inst_21;
          assign regfile[22] = inst_22;
          assign regfile[23] = inst_23;
          assign regfile[24] = inst_24;
          assign regfile[25] = inst_25;
          assign regfile[26] = inst_26;
          assign regfile[27] = inst_27;
          assign regfile[28] = inst_28;
          assign regfile[29] = inst_29;
          assign regfile[30] = inst_30;
          assign regfile[31] = inst_31;
          assign regfile[32] = pc;

          import "DPI-C" function void set_gpr_ptr(input logic [63:0] a []);
          initial set_gpr_ptr(regfile);

      endmodule
