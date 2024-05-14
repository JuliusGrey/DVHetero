#include <cstdlib>
#include <cassert>
#include <iostream>
#include <iomanip>
#include <fstream>
#include <vector>
#include "common.h"
#include "device.h"
#include "vga.h"
#include "keyboard.h"
// #include "device.cpp"

// #define CONFIG_MSIZE 0x8000000
// #define CONFIG_MBASE 0x80000000
// #define CONFIG_PC_RESET_OFFSET 0x0
// #define RESET_VECTOR (CONFIG_MBASE + CONFIG_PC_RESET_OFFSET)

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
  // switch (mask) {
  //   case 0b00000001: *(uint8_t  *)addr = data; return;
  //   case 0b00000011: *(uint16_t *)addr = data; return;
  //   case 0b00001111: *(uint32_t *)addr = data; return;
  //   case 0b11111111: *(uint64_t *)addr = data; return;
  //   default :return;
  // }
}
// uint32_t pmem_readins(uint64_t addr){
//     // uint64_t addrOff = addr - 0x80000000;
//     // uint64_t addrRead = addrOff >> 2;
//     // return *(uint32_t *)guest_to_host(addr);
//     // return img[addrRead] return *(uint32_t *)addr;

//     return *(uint32_t *)guest_to_host(addr);
// }
uint64_t pmem_readdata(uint64_t addri){
  uint32_t addr = addri & 0xfffffff8;
     if(addr >= CONFIG_RTC_MMIO && addr <= CONFIG_RTC_MMIO + 8){
      // difftest_skip_ref();
      int offset = addr - CONFIG_RTC_MMIO;
      // u_int32_t read_time( int offset);
      return read_time(offset);
    }else if(addr == CONFIG_I8042_DATA_MMIO){
      // difftest_skip_ref();
      // uint32_t read_key(void);
      return read_key();
    }else if(addr == CONFIG_VGA_CTL_MMIO){
      // difftest_skip_ref();
      // u_int32_t vgainfo();
      return vgainfo();
    }else{
      // difftest_skip_ref();
      return 0;
}}
void pmem_writedata(uint64_t addr,int mask, uint64_t datai){
  uint64_t data;
  if((addr &0x04)==0)data = datai;else data= datai >>32;

    if(addr == CONFIG_SERIAL_MMIO){
      // difftest_skip_ref();
      // printf("data is %ld\n",data);
      printf("%c",(char)data);
    } else if(addr == SYNC_ADDR){
      // difftest_skip_ref();
      // void vga_update_screen(int syn);
      vga_update_screen(data);

    }else if(addr >= CONFIG_FB_ADDR && addr <CONFIG_FB_ADDR_HIGH ){
      // difftest_skip_ref();
      // void put_pixel(uint64_t addr, uint64_t data) ;
      // printf("addr is %x\n",addr);
      // printf("data is %x\n",data);
      put_pixel(addr,data );
    // } else{
    //   difftest_skip_ref();
    }
}
static char *img_file = NULL;




static long load_img() {
  if (img_file == NULL) {
    printf("no image!!!!\n");
    printf("image name is %s!!!!\n",img_file);
    // Log("No image is given. Use the default build-in image.");
    return 0; // built-in image size
  }

  FILE *fp = fopen(img_file, "rb");
  // Assert(fp, "Can not open '%s'", img_file);
  //    while(1)
  //  {
  //     int c = fgetc(fp);
  //     if( feof(fp) )
  //     { 
  //         break ;
  //     }
  //     printf("%c", c);
  //  }

  fseek(fp, 0, SEEK_END);
  long size = ftell(fp);

  // Log("The image is %s, size = %ld", img_file, size);

  fseek(fp, 0, SEEK_SET);
  int ret = fread(guest_to_host(RESET_VECTOR), size, 1, fp);
  assert(ret == 1);
  // printf("  !!!addr is %#x\n",RESET_VECTOR);
  // printf("  !!!ins is %#x\n",pmem_read(RESET_VECTOR));

  fclose(fp);
  return size;
}

long setimage(char *img){
  img_file = img;
  return load_img();
}

void init_isa() {
  /* Load built-in image. */
  memcpy(guest_to_host(RESET_VECTOR), img, sizeof(img));

  /* Initialize this virtual computer system. */
}



