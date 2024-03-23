#include <am.h>
#include <io.h>
#define VGACTL_ADDR 0xa0000100
#define SYNC_ADDR (VGACTL_ADDR + 4)
#define FB_ADDR 0xa1000000

// void __am_gpu_init() {
// }

// void __am_gpu_config(AM_GPU_CONFIG_T *cfg) {
//   *cfg = (AM_GPU_CONFIG_T) {
//     .present = true, .has_accel = false,
//     .width = 0, .height = 0,
//     .vmemsz = 0
//   };
// }
void dmaGpu(void* src , void* dst , int len){
  int rem_addr = 4096 - ((uint64_t)src % 4096);
  int num64 = len/2;
  int rem_len = rem_addr/8;
  if(num64 > rem_len){
    asm volatile (".insn r 0x7b, 6, 6,%0,%1,%2"::"r"(rem_len - 1),"r"(src ),"r"(dst) );
    src += rem_addr;
    dst += rem_addr;
    num64 -= rem_len;
  }
  while(num64 > 256){
    asm volatile (".insn r 0x7b, 6, 6,%0,%1,%2"::"r"(255),"r"(src ),"r"(dst) );
    src = src + 2048;
    dst = dst + 2048;
    num64 = num64 -256;
  }
  asm volatile (".insn r 0x7b, 6, 6,%0,%1,%2"::"r"(num64 - 1),"r"(src ),"r"(dst) );
}
void __am_gpu_init() {
//   uint32_t vgamsg = inl(VGACTL_ADDR);
//  int i;
//   int w = (int)(vgamsg >> 16);  // TODO: get the correct width
//  int h = (int)(vgamsg & 0x0000ffff);  // TODO: get the correct height
//   uint32_t *fb = (uint32_t *)(uintptr_t)FB_ADDR;
//   for (i = 0; i < w * h; i ++) {fb[i] = i;};
//   outl(SYNC_ADDR, 1);
}

void __am_gpu_config(AM_GPU_CONFIG_T *cfg) {
  uint32_t vgamsg = inl(VGACTL_ADDR);
  *cfg = (AM_GPU_CONFIG_T) {
    .present = true, .has_accel = false,
    .width = (int)(vgamsg >> 16), .height = (int)(vgamsg & 0x0000ffff),
    .vmemsz = 0
  };
}

void __am_gpu_fbdraw(AM_GPU_FBDRAW_T *ctl) {


    // if(wight == 400){
    //   putstr("come to here");
    // }
    // int x = ctl->x;
    // int y = ctl ->y;
    // int w = ctl ->w;
    // int h = ctl ->h;
    // if(w == 4){
    //   putch('a');
    // }




    //  uint32_t vgamsg = inl(VGACTL_ADDR);
    // int wight = (int)(vgamsg >> 16);  // TODO: get the correct width
    // uint32_t *color_buf = ctl ->pixels;
    // uint32_t *rowbase = (uint32_t *)(uintptr_t)(FB_ADDR );
    // if(wight == ctl ->w && ctl->x == 0 ){
    //   dmaGpu(color_buf  ,rowbase+ ( ctl ->y)*wight, ctl ->w * ctl->h);
    // }else{
    // for(int i = 0; i < ctl ->h; i ++){
    //   dmaGpu(color_buf + i*ctl ->w ,rowbase+ (i + ctl ->y)*wight + ctl->x, ctl ->w );
    // }}

     uint32_t vgamsg = inl(VGACTL_ADDR);
    int wight = (int)(vgamsg >> 16);  // TODO: get the correct width
    // if(wight == 400){
    //   putstr("come to here");
    // }
    int x = ctl->x;
    int y = ctl ->y;
        int w = ctl ->w;
    int h = ctl ->h;
    uint32_t *color_buf = ctl ->pixels;
    for(int i = 0; i < h ; i ++){
      // uint32_t *rowbase = (uint32_t *)(uintptr_t)(FB_ADDR + (y + i)*wight + x);
      uint32_t *rowbase = (uint32_t *)(uintptr_t)(FB_ADDR );
      for( int j = 0 ; j < w ; j ++){
        // rowbase[j] = color_buf[i*w + j];
        rowbase[(i + y)*wight + x + j] = color_buf[i*w + j];
      }
    }





  if (ctl->sync) {
    outl(SYNC_ADDR, 1);
  }
}

void __am_gpu_status(AM_GPU_STATUS_T *status) {
  status->ready = true;
}
