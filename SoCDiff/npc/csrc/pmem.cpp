#include <cstdlib>
#include <cassert>
#include <iostream>
#include <iomanip>
#include <fstream>
#include <vector>
#include "common.h"
#include "device.h"

void difftest_skip_ref();

 uint32_t img [] = {
  0xff010113,  // addi
  0xff010113,  // addi
  0xff010113,  //addi
  0xff010113,  //addi
  0xff010113,  //addi
  0xff010113,  //addi
  0x00100073,  // ebreak (used as nemu_trap)
};
static uint8_t pmem[CONFIG_MSIZE]  = {};

uint8_t* guest_to_host(uint64_t paddr) { return pmem + paddr - CONFIG_MBASE; }
uint64_t host_to_guest(uint8_t *haddr) { return haddr - pmem + CONFIG_MBASE; }

void host_write(void *addr, int mask, uint64_t data) {
  for( int i = 0; i < 8 ; i++){
    if(mask>>i & 1){
      *((uint8_t  *)addr + i )= data >> i*8;
    }
  }
}
uint64_t pmem_readdata(uint64_t addri){
  uint32_t addr = addri & 0xfffffff8;
     if(addr >= CONFIG_RTC_MMIO && addr <= CONFIG_RTC_MMIO + 8){
      int offset = addr - CONFIG_RTC_MMIO;
      return read_time(offset);
    }else{
      return 0;
}}
void pmem_writedata(uint64_t addr,int mask, uint64_t datai){
  uint64_t data;
  if((addr &0x04)==0)data = datai;else data= datai >>32;

    if(addr == CONFIG_SERIAL_MMIO){
      printf("%c",(char)data);
    } 
}
static char *img_file = NULL;




static long load_img() {
  if (img_file == NULL) {
    printf("no image!!!!\n");
    return 0; // built-in image size
  }

  FILE *fp = fopen(img_file, "rb");

  fseek(fp, 0, SEEK_END);
  long size = ftell(fp);


  fseek(fp, 0, SEEK_SET);
  int ret = fread(guest_to_host(RESET_VECTOR), size, 1, fp);
  assert(ret == 1);

  fclose(fp);
  return size;
}

long setimage(char *img){
  img_file = img;
  return load_img();
}

void init_isa() {
  memcpy(guest_to_host(RESET_VECTOR), img, sizeof(img));
}



