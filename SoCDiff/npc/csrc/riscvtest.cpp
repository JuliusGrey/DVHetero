
// #include "Vriscv.h"
// #include "verilated_dpi.h"
// // #include "verilated_vcd_c.h"
// #include "Vriscv__Dpi.h"
#include "VTopOK.h"
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "axi4.hpp"
#include "axi4_mem.hpp"
// #include "axi4_xbar.hpp"
// #include "mmio_mem.hpp"
// #include "uartlite.hpp"
#include "verilated_vcd_c.h"
#include "device.h"

#define WAVEGEN
#define DIFFCPU
// #define DIFFACC
// #define CGRATEST
// extern void getabort(svBit* abor) ;
// vluint64_t main_time = 0;  //initial 仿真时间

// static svBit abor = 0;



// double sc_time_stamp()
//  {
//      return main_time;
//  }


 
//  static const uint32_t img [] = {
//   0xff010113,  // addi
//   0xff010113,  // addi
//   0xff010113,  //addi
//   0x00100073,  // ebreak (used as nemu_trap)
// };

// uint32_t pmem_read(uint64_t addr){
//     uint64_t addrOff = addr - 0x80000000;
//     uint64_t addrRead = addrOff >> 2;
//     return img[addrRead];
// }
axi4_mem <32,64,256,4> mem(4096l*1024*1024);
void connect_wire(axi4_ptr <32,64,4> &data,axi4_ptr <32,256,4> &cgra,VTopOK *top) {
    // connect
    // mmio
    // aw   
    // inst.awaddr     = &(top->io_imaster_awaddr);
    // inst.awburst    = &(top->io_imaster_awburst);
    // inst.awid       = &(top->io_imaster_awid);
    // inst.awlen      = &(top->io_imaster_awlen);
    // inst.awready    = &(top->io_imaster_awready);
    // inst.awsize     = &(top->io_imaster_awsize);
    // inst.awvalid    = &(top->io_imaster_awvalid);
    // // w
    // inst.wdata      = &(top->io_imaster_wdata);
    // inst.wlast      = &(top->io_imaster_wlast);
    // inst.wready     = &(top->io_imaster_wready);
    // inst.wstrb      = &(top->io_imaster_wstrb);
    // inst.wvalid     = &(top->io_imaster_wvalid);
    // // b
    // inst.bid        = &(top->io_imaster_bid);
    // inst.bready     = &(top->io_imaster_bready);
    // inst.bresp      = &(top->io_imaster_bresp);
    // inst.bvalid     = &(top->io_imaster_bvalid);
    // // ar
    // inst.araddr     = &(top->io_imaster_araddr);
    // inst.arburst    = &(top->io_imaster_arburst);
    // inst.arid       = &(top->io_imaster_arid);
    // inst.arlen      = &(top->io_imaster_arlen);
    // inst.arready    = &(top->io_imaster_arready);
    // inst.arsize     = &(top->io_imaster_arsize);
    // inst.arvalid    = &(top->io_imaster_arvalid);
    // // r
    // inst.rdata      = &(top->io_imaster_rdata);
    // inst.rid        = &(top->io_imaster_rid);
    // inst.rlast      = &(top->io_imaster_rlast);
    // inst.rready     = &(top->io_imaster_rready);
    // inst.rresp      = &(top->io_imaster_rresp);
    // inst.rvalid     = &(top->io_imaster_rvalid);
    // mem

        // aw   
    data.awaddr     = &(top->io_cupIO_awaddr);
    data.awburst    = &(top->io_cupIO_awburst);
    data.awid       = &(top->io_cupIO_awid);
    data.awlen      = &(top->io_cupIO_awlen);
    data.awready    = &(top->io_cupIO_awready);
    data.awsize     = &(top->io_cupIO_awsize);
    data.awvalid    = &(top->io_cupIO_awvalid);
    // w
    data.wdata      = &(top->io_cupIO_wdata);
    data.wlast      = &(top->io_cupIO_wlast);
    data.wready     = &(top->io_cupIO_wready);
    data.wstrb      = &(top->io_cupIO_wstrb);
    data.wvalid     = &(top->io_cupIO_wvalid);
    // b
    data.bid        = &(top->io_cupIO_bid);
    data.bready     = &(top->io_cupIO_bready);
    data.bresp      = &(top->io_cupIO_bresp);
    data.bvalid     = &(top->io_cupIO_bvalid);
    // ar
    data.araddr     = &(top->io_cupIO_araddr);
    data.arburst    = &(top->io_cupIO_arburst);
    data.arid       = &(top->io_cupIO_arid);
    data.arlen      = &(top->io_cupIO_arlen);
    data.arready    = &(top->io_cupIO_arready);
    data.arsize     = &(top->io_cupIO_arsize);
    data.arvalid    = &(top->io_cupIO_arvalid);
    // r
    data.rdata      = &(top->io_cupIO_rdata);
    data.rid        = &(top->io_cupIO_rid);
    data.rlast      = &(top->io_cupIO_rlast);
    data.rready     = &(top->io_cupIO_rready);
    data.rresp      = &(top->io_cupIO_rresp);
    data.rvalid     = &(top->io_cupIO_rvalid);



          // aw   
    cgra.awaddr     = &(top->io_CGRAIO_awaddr);
    cgra.awburst    = &(top->io_CGRAIO_awburst);
    cgra.awid       = &(top->io_CGRAIO_awid);
    cgra.awlen      = &(top->io_CGRAIO_awlen);
    cgra.awready    = &(top->io_CGRAIO_awready);
    cgra.awsize     = &(top->io_CGRAIO_awsize);
    cgra.awvalid    = &(top->io_CGRAIO_awvalid);
    // w
    cgra.wdata      = &(top->io_CGRAIO_wdata);
    cgra.wlast      = &(top->io_CGRAIO_wlast);
    cgra.wready     = &(top->io_CGRAIO_wready);
    cgra.wstrb      = &(top->io_CGRAIO_wstrb);
    cgra.wvalid     = &(top->io_CGRAIO_wvalid);
    // b
    cgra.bid        = &(top->io_CGRAIO_bid);
    cgra.bready     = &(top->io_CGRAIO_bready);
    cgra.bresp      = &(top->io_CGRAIO_bresp);
    cgra.bvalid     = &(top->io_CGRAIO_bvalid);
    // ar
    cgra.araddr     = &(top->io_CGRAIO_araddr);
    cgra.arburst    = &(top->io_CGRAIO_arburst);
    cgra.arid       = &(top->io_CGRAIO_arid);
    cgra.arlen      = &(top->io_CGRAIO_arlen);
    cgra.arready    = &(top->io_CGRAIO_arready);
    cgra.arsize     = &(top->io_CGRAIO_arsize);
    cgra.arvalid    = &(top->io_CGRAIO_arvalid);
    // r
    cgra.rdata      = &(top->io_CGRAIO_rdata);
    cgra.rid        = &(top->io_CGRAIO_rid);
    cgra.rlast      = &(top->io_CGRAIO_rlast);
    cgra.rready     = &(top->io_CGRAIO_rready);
    cgra.rresp      = &(top->io_CGRAIO_rresp);
    cgra.rvalid     = &(top->io_CGRAIO_rvalid);
  
  
}


int main(int argc,char **argv)
{   printf("begin");


     Verilated::commandArgs(argc,argv);
     #if defined(WAVEGEN)
     Verilated::traceEverOn(true); //导出vcd波形需要加此语句
     
 
     VerilatedVcdC* tfp = new VerilatedVcdC(); 
     #endif
     VTopOK *top = new VTopOK("top");
      #if defined(WAVEGEN)
     top->trace(tfp, 0);
     tfp->open("wave.vcd");
     Verilated::traceEverOn(true); //导出vcd波形需要加此语句
     #endif
    // void init_isa();
    // init_isa();
//     long setimage(char *img);
//     long img_size =  setimage(argv[1]);
//     void init_difftest(char *ref_so_file, long img_size, int port) ;
//     init_difftest(argv[2],img_size,0);
//     printf("out of image!!!\n");
 
    //  VerilatedVcdC* tfp = new VerilatedVcdC(); //导出vcd波形需要加此语句
    // uint32_t pmem_read(uint64_t addr);
    //  Vriscv *top = new Vriscv("top");
    //  top -> reset = 1;
    

    
printf("ref_so_file is  %s\n",argv[2]);

     top->eval();
     long setimage(char *img);
    //  char file1[50];
    //  strcpy(file1, argv[1]);
    long img_size =  setimage(argv[1]);




    // axi4_ptr <32,64,4> inst_ptr;
    axi4_ptr <32,64,4> data_ptr;
    axi4_ptr <32,256,4> cgra_ptr;
    connect_wire(data_ptr,cgra_ptr,top);


    // axi4_ref <32,64,4> inst_ref(inst_ptr);
    // axi4     <32,64,4> inst_sigs;
    // axi4_ref <32,64,4> inst_sigs_ref(inst_sigs);

    axi4_ref <32,64,4> data_ref(data_ptr);
    axi4     <32,64,4> data_sigs;
    axi4_ref <32,64,4> data_sigs_ref(data_sigs);


    axi4_ref <32,256,4> cgra_ref(cgra_ptr);
    axi4     <32,256,4> cgra_sigs;
    axi4_ref <32,256,4> cgra_sigs_ref(cgra_sigs);


    // printf("data_ref port is %d\n",data_ref.awid);
    

        




    mem.load_binary(argv[1],0x80000000);

        svBit diffcommit = 0;
        svBit abor = 0;
        svBit skip = 0;
        int time = 0;
        bool diff = false;
        top -> reset = 1;
        top -> clock = 0;
        time =  time +1;
        top->eval();
     #if defined(WAVEGEN)
         tfp->dump(time);
     #endif
        top -> clock = 1;
        time =  time +1;
        top->eval();
     #if defined(WAVEGEN)
         tfp->dump(time);
     #endif
        top -> clock = 0;
        time =  time +1;
        top->eval();
     #if defined(WAVEGEN)
         tfp->dump(time);
     #endif
        top -> clock = 1;
        time =  time +1;

        top -> reset = 0;
        top->eval();
     #if defined(WAVEGEN)
         tfp->dump(time);
     #endif
        // void dump_gpr() ;
        // dump_gpr() ;
        // init_difftest(file2,img_size,0);
        // while(1){
        //   void difftest_step2() ;
        //   difftest_step2() ;
        // }


        // void init_device(void) ;
        init_device() ;
            void init_difftest(char *ref_so_file, long img_size, int port) ;
    // char file2[50];
    //  strcpy(file2, argv[2]);
     init_difftest(argv[2],img_size,0);

        // bool skip = false;
        // bool skip_delay = false;
        static int cnt  = 0;
        int diffcnt = 0;
        bool acc_diff= false; //diff_accelerator
        while(!abor &&  !diff &&diffcnt <1200 && !acc_diff ){
          // printf("diffcnt is %d\n",diffcnt);
          // printf("\n\nround %d\n",cnt);
          cnt ++;
        //   if(top -> io_cupIO_arlen >10){printf("len is %d\n",top -> io_cupIO_arlen);}
          // if(cnt == 30) abort();
            svScope a = svGetScopeFromName("top.TopOK.riscvIns.Ebpro");
            svSetScope(a);
            top -> getabort(&abor);
            // top -> io_instIO_data_read = pmem_readdata(top ->io_instIO_addr);
            // printf("pc is %#lx\n",top ->io_instIO_addr);
            // printf("ins is %#lx\n",pmem_readins(top -> io_pc));
            svSetScope(svGetScopeFromName("top.TopOK.riscvIns.difftest"));
            top ->difftestcommit(&diffcommit);

            svSetScope(svGetScopeFromName("top.TopOK.riscvIns.skipinst"));
            top -> getskip(&skip);

            top->eval();
            // skip_delay = skip;
            #ifndef CGRATEST
            top -> io_mmio_ready = 1;
            if(top -> io_mmio_valid){
            if(top->io_mmio_wen){
              void pmem_writedata(uint64_t addr,int mask, uint64_t data);
            //   printf("assr is %#lx\n",top->io_mmio_addr);
            //   printf("data is %#lx\n",top->io_mmio_data_write);
              pmem_writedata(top->io_mmio_addr,top->io_mmio_mask,top->io_mmio_data_write);
            }else{
              uint64_t pmem_readdata(uint64_t addri);
             top->io_mmio_data_read =  pmem_readdata(top->io_mmio_addr);
            }} 
            #endif
            // inst_sigs.update_input(inst_ref);
            data_sigs.update_input(data_ref);
            cgra_sigs.update_input(cgra_ref);
            top -> clock = 0;
            time =  time +1;
            top->eval();
         #if defined(WAVEGEN)
         tfp->dump(time);
     #endif
            top -> clock = 1;
            time =  time +1;
            top->eval();

            // mem.beat(inst_sigs_ref);
            mem.beat(data_sigs_ref);
            mem.beat2(cgra_sigs_ref);
            //  inst_sigs.update_output(inst_ref);
             data_sigs.update_output(data_ref);
             cgra_sigs.update_output(cgra_ref);

            top->eval();
                     #if defined(WAVEGEN)
         tfp->dump(time);
     #endif
            #ifdef DIFFCPU
            void difftest_skip_ref();
            
            void difftest_step(bool & abor) ;
            if(diffcommit){
              if(skip) difftest_skip_ref();
              diffcnt = 0;
            difftest_step(diff);}
                poll_event();
            diffcnt ++;
            #endif

            #ifdef DIFFACC
            void accelerator_check_start();
            bool accelerator_check_data();
            accelerator_check_start();
            acc_diff = !accelerator_check_data();
            #endif

        }
        
        printf("abor is %d\n",abor);
        printf("diffcnt is %d\n",diffcnt);
        printf("cnt is %d\n",cnt);
        printf("stop\n");
        top->final();
        #if defined(WAVEGEN)
        tfp ->close();
        #endif
        delete top;
     return 0;
}