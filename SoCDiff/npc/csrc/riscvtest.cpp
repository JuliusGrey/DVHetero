
#include "VTopOK.h"
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "axi4.hpp"
#include "axi4_mem.hpp"
#include "axi4_xbar.hpp"
#include "mmio_mem.hpp"
#include "uartlite.hpp"
#include "verilated_vcd_c.h"
#include "device.h"

#define WAVEGEN
// #define DIFFCPU
// #define DIFFACC
axi4_mem <32,64,4> mem(4096l*1024*1024);
void connect_wire(axi4_ptr <32,64,4> &data,VTopOK *top) {

        // aw   
    data.awaddr     = &(top->io_dmaster_awaddr);
    data.awburst    = &(top->io_dmaster_awburst);
    data.awid       = &(top->io_dmaster_awid);
    data.awlen      = &(top->io_dmaster_awlen);
    data.awready    = &(top->io_dmaster_awready);
    data.awsize     = &(top->io_dmaster_awsize);
    data.awvalid    = &(top->io_dmaster_awvalid);
    // w
    data.wdata      = &(top->io_dmaster_wdata);
    data.wlast      = &(top->io_dmaster_wlast);
    data.wready     = &(top->io_dmaster_wready);
    data.wstrb      = &(top->io_dmaster_wstrb);
    data.wvalid     = &(top->io_dmaster_wvalid);
    // b
    data.bid        = &(top->io_dmaster_bid);
    data.bready     = &(top->io_dmaster_bready);
    data.bresp      = &(top->io_dmaster_bresp);
    data.bvalid     = &(top->io_dmaster_bvalid);
    // ar
    data.araddr     = &(top->io_dmaster_araddr);
    data.arburst    = &(top->io_dmaster_arburst);
    data.arid       = &(top->io_dmaster_arid);
    data.arlen      = &(top->io_dmaster_arlen);
    data.arready    = &(top->io_dmaster_arready);
    data.arsize     = &(top->io_dmaster_arsize);
    data.arvalid    = &(top->io_dmaster_arvalid);
    // r
    data.rdata      = &(top->io_dmaster_rdata);
    data.rid        = &(top->io_dmaster_rid);
    data.rlast      = &(top->io_dmaster_rlast);
    data.rready     = &(top->io_dmaster_rready);
    data.rresp      = &(top->io_dmaster_rresp);
    data.rvalid     = &(top->io_dmaster_rvalid);
  
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
    

    
printf("ref_so_file is  %s\n",argv[2]);

     top->eval();
     long setimage(char *img);
    long img_size =  setimage(argv[1]);




    axi4_ptr <32,64,4> data_ptr;
    connect_wire(data_ptr,top);


    axi4_ref <32,64,4> data_ref(data_ptr);
    axi4     <32,64,4> data_sigs;
    axi4_ref <32,64,4> data_sigs_ref(data_sigs);


    

        




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
            void init_difftest(char *ref_so_file, long img_size, int port) ;
     init_difftest(argv[2],img_size,0);

        int cnt  = 0;
        int diffcnt = 0;
        bool acc_diff= false; //diff_accelerator
        while(!abor &&  !diff &&diffcnt !=300 && !acc_diff ){
          cnt ++;
            svSetScope(svGetScopeFromName("top.TopOK.riscvIns.Ebpro"));
            top -> getabort(&abor);
            svSetScope(svGetScopeFromName("top.TopOK.riscvIns.difftest"));
            top ->difftestcommit(&diffcommit);

            svSetScope(svGetScopeFromName("top.TopOK.riscvIns.skipinst"));
            top -> getskip(&skip);

            top->eval();
            #ifndef CGRATEST
            top -> io_mmio_ready = 1;
            if(top -> io_mmio_valid){
            if(top->io_mmio_wen){
              void pmem_writedata(uint64_t addr,int mask, uint64_t data);
              pmem_writedata(top->io_mmio_addr,top->io_mmio_mask,top->io_mmio_data_write);
            }else{
              uint64_t pmem_readdata(uint64_t addri);
             top->io_mmio_data_read =  pmem_readdata(top->io_mmio_addr);
            }} 
            #endif
            data_sigs.update_input(data_ref);
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
            mem.beat(data_sigs_ref);
             data_sigs.update_output(data_ref);

            top->eval();
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