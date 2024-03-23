#include <stdio.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <fstream>
#include <cassert>
#include "common.h"
#include "verilated_dpi.h"
#include "VTopOK__Dpi.h"


typedef struct {
  uint64_t gpr[32];
  uint64_t pc;
} CPU_state;

// static riscv64_CPU_state cpu;

uint64_t *cpu_gpr = NULL;
extern "C" void set_gpr_ptr(const svOpenArrayHandle r) {
  cpu_gpr = (uint64_t *)(((VerilatedDpiOpenVar*)r)->datap());
}


//diff_accelerator
uint64_t * accelerator_in = NULL;
uint64_t * accelerator_db = NULL;

extern "C" void get_accelerator_in(const svOpenArrayHandle ins) {
  accelerator_in = (uint64_t *)(((VerilatedDpiOpenVar*)ins)->datap());
  // accelerator_out = (uint64_t *)(((VerilatedDpiOpenVar*)outs)->datap());
}

extern "C" void get_accelerator_db(const svOpenArrayHandle in) {
  accelerator_db = (uint64_t *)(((VerilatedDpiOpenVar*)in)->datap());
  // accelerator_out = (uint64_t *)(((VerilatedDpiOpenVar*)outs)->datap());
}

bool accelerator_db_valid  = false;
uint32_t accelerator_db_data;



void dump_gpr() {
        int i;
        for (i = 0; i < 33; i++) {
            printf("gpr[%d] = 0x%lx\n", i, cpu_gpr[i]);
        }
}

typedef uint64_t paddr_t;
enum { DIFFTEST_TO_DUT, DIFFTEST_TO_REF };

void (*ref_difftest_memcpy)(paddr_t addr, void *buf, size_t n, bool direction) = NULL;
void (*ref_difftest_regcpy)(void *dut, bool direction) = NULL;


void (*ref_difftest_exec)(uint64_t n) = NULL;
void (*ref_difftest_raise_intr)(uint64_t NO) = NULL;




static bool is_skip_ref = false;
static int skip_dut_nr_inst = 0;

void difftest_skip_ref() {
  is_skip_ref = true;
  skip_dut_nr_inst = 0;
}

void difftest_skip_dut(int nr_ref, int nr_dut) {
  skip_dut_nr_inst += nr_dut;

  while (nr_ref -- > 0) {
    ref_difftest_exec(1);
  }
}

void init_difftest(char *ref_so_file, long img_size, int port) {
  assert(ref_so_file != NULL);

  void *handle;
  handle = dlopen(ref_so_file, RTLD_LAZY);
  assert(handle);

  ref_difftest_memcpy = (void(*)(paddr_t, void *, size_t , bool))dlsym(handle, "difftest_memcpy");
  assert(ref_difftest_memcpy);

  ref_difftest_regcpy =  (void(*)(void *, bool ))dlsym(handle, "difftest_regcpy");
  assert(ref_difftest_regcpy);






  ref_difftest_exec = (void(*)(uint64_t))dlsym(handle, "difftest_exec");
  assert(ref_difftest_exec);

  ref_difftest_raise_intr = (void(*)(uint64_t))dlsym(handle, "difftest_raise_intr");
  assert(ref_difftest_raise_intr);

  void (*ref_difftest_init)(int) = (void (*)(int))dlsym(handle, "difftest_init");
  assert(ref_difftest_init);


  ref_difftest_init(port);
  uint8_t* guest_to_host(uint64_t paddr);
  ref_difftest_memcpy(RESET_VECTOR, guest_to_host(RESET_VECTOR), img_size, DIFFTEST_TO_REF);
      CPU_state * test = (CPU_state *)cpu_gpr;
      test -> pc = 0x80000000;
      ref_difftest_regcpy(test, true);
}


bool isa_difftest_checkregs() {
CPU_state test;
ref_difftest_regcpy(&test ,false);
for(int i = 0; i < 32; i++){
    if(test.gpr[i] != cpu_gpr[i]){
        printf("diff index is %d\n",i);
        printf("%s:\t%#lx\t%s:\t%#lx\n","ref",test.gpr[i],"dut",cpu_gpr[i]);
        return false;
    }
}
  return true;
}

void dump_ref() {
        CPU_state test;
        ref_difftest_regcpy(&test ,false);
        int i;
        printf("ref:\n");
        for (i = 0; i < 32; i++) {
          // printf("%d\n",i);
            printf("gpr[%d] = 0x%lx\n", i, test.gpr[i]);
        }
}
static void checkregs(bool & abor) {
  if (!isa_difftest_checkregs()) {
    abor = true;
    dump_gpr();
    dump_ref();
  }
}


void difftest_step(bool & abor) {
  if (is_skip_ref) {
    ref_difftest_regcpy(cpu_gpr, true);
    is_skip_ref = false;
    return;
  }

  ref_difftest_exec(1);

  checkregs(abor);
  
}


void difftest_step2() {

  ref_difftest_exec(1);
}


void accelerator_check_start(){
  if(accelerator_in[0] >> 32 == 1){
    uint32_t sub5 = accelerator_in[13]-accelerator_in[0];
    // uint32_t mul6 = sub5*sub5;
    // uint32_t sub11 =accelerator_in[5]-accelerator_in[1];
    // uint32_t mul12 = sub11*sub11;
    accelerator_db_valid = true;
    accelerator_db_data = sub5;
    // printf("indata is %d\n",accelerator_in[0]);
      //  uint32_t*dataIn =   accelerator_in;
      // uint32_t add9 = accelerator_in[0]*accelerator_in[1] + accelerator_in[2]*accelerator_in[3]  ;
      // uint32_t add10 = accelerator_in[4]*accelerator_in[5] + accelerator_in[6]*accelerator_in[7]  ; 
      // uint32_t add11 = accelerator_in[8]*accelerator_in[9] + accelerator_in[10]*accelerator_in[11] ; 
      // uint32_t add12 = accelerator_in[12]*accelerator_in[13] + accelerator_in[14]*accelerator_in[15] ; 
      // uint32_t add13 = add10;
      //   uint32_t add14 = add11 ;
      //   uint32_t mul15 = add13 ;
      //   uint32_t mul16 = add14 ;
      //   uint32_t mul17 = add13;
      //   uint32_t mul18 = add14 ;
      //   uint32_t add19 = mul15 + mul16;
      //   uint32_t add20 = mul17 +mul18;
      //   uint32_t mul21 = add19;
      //   uint32_t mul22 = add20;
      //   uint32_t mul23 = add19;
      //   uint32_t mul24 = add20 ;
      //   uint32_t add25 = mul21 + mul22;
      //   uint32_t add26 = mul23 + mul24;
      //   uint32_t add27 = add9+add25;
      //   uint32_t add28 = add26 + add12;
      //   uint32_t out1 = add27;
      //   uint32_t out2 = add28;
      //   // printf("out1 %d\n", out1);
      //   accelerator_out_data[0] = out1;
      //   accelerator_out_data[1] = out2;
      //   accelerator_out_valid[0] = true;
      //   accelerator_out_valid[1] = true;
     }
     return ;
}

//false 表示有問題
bool accelerator_check_data(){
  int i;
  for(i= 0 ; i < 1 ; i ++ ){
    if(accelerator_db[i] >> 32 == 1){
      if(accelerator_db_valid){
        if((accelerator_db[i] & 0x00000000ffffffff) == accelerator_db_data){
          accelerator_db_valid = false;
        } else {
          printf("the mark %d is not match\n",i);
          printf("ref %d \n",accelerator_db_data);
          printf("dut %d \n",accelerator_db[i] & 0x00000000ffffffff);
          return false;
        }
      }else{
        printf("the output has no input\n");
        return false;
      }
    }
  }
  return true;
}

