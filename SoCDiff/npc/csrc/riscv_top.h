#include "Vtop.h"
 #include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "axi4.hpp"
#include "axi4_mem.hpp"
#include "axi4_xbar.hpp"
#include "mmio_mem.hpp"
#include "uartlite.hpp"
// #include "verilated_dpi.h"
// #include "Vriscv__Dpi.h"


// typedef struct {
//   uint64_t gpr[32];
//   uint64_t pc;
// } riscv64_CPU_state;


// uint64_t *cpu_gpr = NULL;
// riscv64_CPU_state *test = (iscv64_CPU_state *)cpu_gpr;
// extern "C" void set_gpr_ptr(const svOpenArrayHandle r) {
//   (uint64_t *)cpu_gpr = (uint64_t *)(((VerilatedDpiOpenVar*)r)->datap());
// }
axi4_mem <32,64,4> mem(4096l*1024*1024);
void connect_wire(axi4_ptr <32,64,4> &memptr, Vtop *top) {
    // connect
    // mmio
    // aw   
    memptr.awaddr     = &(top->io_master_awaddr);
    memptr.awburst    = &(top->io_master_awburst);
    memptr.awid       = &(top->io_master_awid);
    memptr.awlen      = &(top->io_master_awlen);
    memptr.awready    = &(top->io_master_awready);
    memptr.awsize     = &(top->io_master_awsize);
    memptr.awvalid    = &(top->io_master_awvalid);
    // w
    memptr.wdata      = &(top->io_master_wdata);
    memptr.wlast      = &(top->io_master_wlast);
    memptr.wready     = &(top->io_master_wready);
    memptr.wstrb      = &(top->io_master_wstrb);
    memptr.wvalid     = &(top->io_master_wvalid);
    // b
    memptr.bid        = &(top->io_master_bid);
    memptr.bready     = &(top->io_master_bready);
    memptr.bresp      = &(top->io_master_bresp);
    memptr.bvalid     = &(top->io_master_bvalid);
    // ar
    memptr.araddr     = &(top->io_master_araddr);
    memptr.arburst    = &(top->io_master_arburst);
    memptr.arid       = &(top->io_master_arid);
    memptr.arlen      = &(top->io_master_arlen);
    memptr.arready    = &(top->io_master_arready);
    memptr.arsize     = &(top->io_master_arsize);
    memptr.arvalid    = &(top->io_master_arvalid);
    // r
    memptr.rdata      = &(top->io_master_rdata);
    memptr.rid        = &(top->io_master_rid);
    memptr.rlast      = &(top->io_master_rlast);
    memptr.rready     = &(top->io_master_rready);
    memptr.rresp      = &(top->io_master_rresp);
    memptr.rvalid     = &(top->io_master_rvalid);
    // mem
  
}



class riscv_top {
    uint64_t time = 0;
    Vtop *top;
    svBit abor = 0;
    svBit diffcommit = 0;
    bool diff = false;

public:
     riscv_top(){
        top = new Vtop("top");
        top->eval();
     }

     void riscv_rst(){
        abor = 0;
        time = 0;
        top -> reset = 1;
        top -> clock = 0;
        top->eval();
        time =  time +1;
        top -> clock = 1;
        top->eval();
        time =  time +1;
        top -> reset = 0;
     }
      
      void riscv_exeonce(){
        top -> clock = 0;
        top->eval();
        time =  time +1;
        top -> clock = 1;
        top->eval();
        time =  time +1;
      }

      // void dump_gpr() {
      //   int i;
      //   for (i = 0; i < 32; i++) {
      //       printf("gpr[%d] = 0x%lx\n", i, cpu_gpr[i]);
      //   }
      //   }

      void riscv_exe(){


        top->eval();
     long setimage(char *img);
     char file1[50];
     strcpy(file1, "binlib/amtest-riscv64-nemu.bin");
    long img_size =  setimage(file1);
        void init_difftest(char *ref_so_file, long img_size, int port) ;
    char file2[50];
     strcpy(file2, "./riscv64-nemu-interpreter-so");
     init_difftest(file2,img_size,0);


    axi4_ptr <32,64,4> mem_ptr;
    connect_wire(mem_ptr,top);


    axi4_ref <32,64,4> mem_ref(mem_ptr);
    axi4     <32,64,4> mem_sigs;
    axi4_ref <32,64,4> mem_sigs_ref(mem_sigs);

    mem.load_binary("binlib/amtest-riscv64-nemu.bin",0x80000000);

    
        riscv_rst();
        // void dump_gpr() ;
        // dump_gpr() ;
        // init_difftest(file2,img_size,0);
        // while(1){
        //   void difftest_step2() ;
        //   difftest_step2() ;
        // }


        // void init_device(void) ;
        // init_device() ;

        // bool skip = false;
        // bool skip_delay = false;
        int cnt  = 0;
        int diffcnt = 0;
        while(!abor &&  !diff &&diffcnt !=150){
          printf("\n\nround %d\n",cnt);
          cnt ++;
            svSetScope(svGetScopeFromName("top.top.riscvIns.ebProbe"));
            top -> getabort(&abor);
            // top -> io_instIO_data_read = pmem_readdata(top ->io_instIO_addr);
            // printf("pc is %#lx\n",top ->io_instIO_addr);
            // printf("ins is %#lx\n",pmem_readins(top -> io_pc));
            svSetScope(svGetScopeFromName("top.top.riscvIns.difftest"));
            top ->difftestcommit(&diffcommit);

            top->eval();
            // skip_delay = skip;
            // if(top -> io_dataIO_valid){
            //   if(top->io_dataIO_addr >= 0x80000000 && top->io_dataIO_addr <= 0x87ffffff)skip = false ;else skip = true;
            // if(top->io_dataIO_wen){
            //   pmem_writedata(top->io_dataIO_addr,top->io_dataIO_mask,top->io_dataIO_data_write);
            // }else{
            //  top->io_dataIO_data_read =  pmem_readdata(top->io_dataIO_addr);
            // }} else{
            //   skip = false;
            // }
            mem_sigs.update_input(mem_ref);
            riscv_exeonce();
            mem.beat(mem_sigs_ref);
             mem_sigs.update_output(mem_ref);

            
            // printf("800001a8 is %lx\n",pmem_readdata(0x800001a8));
            top->eval();
                    // printf("addr is %#lx\n",top -> io_pc);
        // printf("ins is %#x\n",pmem_read(top -> io_pc));


        
            // void difftest_skip_ref() ;
            // if(skip_delay){
            //   difftest_skip_ref();
            // }
            void difftest_step(bool & abor) ;
            if(diffcommit){
              diffcnt = 0;
              // printf("come to here!\n");
            difftest_step(diff);}

            // // void dump_gpr() ;
            // // dump_gpr();
            //     extern void poll_event(void);
            //     poll_event();
            diffcnt ++;
        }
        
        printf("abor is %d\n",abor);
        printf("diffcnt is %d\n",diffcnt);
        printf("stop\n");
        top->final();
      }

      Vtop * gettop(){
        return top;
      }

      ~riscv_top(){
        delete top;
     }

};


