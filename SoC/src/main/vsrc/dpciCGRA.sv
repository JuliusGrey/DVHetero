module   dpicCGRA  (
           input[32:0] ins_0,
           input[32:0] ins_1,
           input[32:0] ins_2,
           input[32:0] ins_3,
           input[32:0] ins_4,
           input[32:0] ins_5,
           input[32:0] ins_6,
           input[32:0] ins_7,
           input[32:0] ins_8,
           input[32:0] ins_9,
           input[32:0] ins_10,
           input[32:0] ins_11,
           input[32:0] ins_12,
           input[32:0] ins_13,
           input[32:0] ins_14,
           input[32:0] ins_15
         );
          logic [32:0] inReg [15 :0 ];
          assign inReg[0] = ins_0;
          assign inReg[1] = ins_1;
          assign inReg[2] = ins_2;
          assign inReg[3] = ins_3;
          assign inReg[4] = ins_4;
          assign inReg[5] = ins_5;
          assign inReg[6] = ins_6;
          assign inReg[7] = ins_7;
          assign inReg[8] = ins_8;
          assign inReg[9] = ins_9;
          assign inReg[10] = ins_10;
          assign inReg[11] = ins_11;
          assign inReg[12] = ins_12;
          assign inReg[13] = ins_13;
          assign inReg[14] = ins_14;
          assign inReg[15] = ins_15;


          import "DPI-C" function void get_accelerator_in(input logic [33:0] ins [] );
          initial get_accelerator_in(inReg);

      endmodule

module   dpicDebug  (
           input[32:0] in
         );
          logic [32:0] inReg [0:0 ];
          assign inReg[0] = ins;

          import "DPI-C" function void get_accelerator_db(input logic [33:0] in [] ;
          initial get_accelerator(inReg);

      endmodule

