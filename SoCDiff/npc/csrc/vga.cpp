#include <SDL2/SDL.h>
#include "common.h"



static SDL_Window *window;
static SDL_Renderer *renderer;
static SDL_Texture *texture;
static uint32_t vmem[SCREEN_W * SCREEN_H];


void init_sdl() {
  SDL_Init(SDL_INIT_VIDEO);
  SDL_CreateWindowAndRenderer(SCREEN_W, SCREEN_H, 0, &window, &renderer);
  SDL_SetWindowTitle(window, "Julius");
  texture = SDL_CreateTexture(renderer, SDL_PIXELFORMAT_ARGB8888,
      SDL_TEXTUREACCESS_STATIC, SCREEN_W, SCREEN_H);
}


static inline void update_screen() {
  SDL_UpdateTexture(texture, NULL, vmem, SCREEN_W * sizeof(uint32_t));
  SDL_RenderClear(renderer);
  SDL_RenderCopy(renderer, texture, NULL, NULL);
  SDL_RenderPresent(renderer);
}


// 
u_int32_t vgainfo(){
  return SCREEN_W<< 16 | SCREEN_H ;
}

void vga_update_screen(int syn) {
  // TODO: call `update_screen()` when the sync register is non-zero,
  // then zero out the sync register
  if(syn==1){
    update_screen();
  }
}

void put_pixel(uint64_t addr, uint64_t data) {
  int addr_dif = addr - CONFIG_FB_ADDR;
  int index = addr_dif/4;
  vmem[index] = (Uint32) data;
  vmem[index + 1] =  data >> 32;
}