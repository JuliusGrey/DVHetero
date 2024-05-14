#include <stdlib.h>
#include <sys/time.h>

#include <SDL2/SDL.h>
#include "common.h"
#include "keyboard.h"

// #define CONFIG_RTC_MMIO 0xa0000048
// #define CONFIG_SERIAL_MMIO 0xa00003f8
// #define CONFIG_I8042_DATA_MMIO 0xa0000060
// #define CONFIG_VGA_CTL_MMIO 0xa0000100


static uint64_t boot_time = 0;
static  u_int32_t rtc[2];

u_int64_t read_time( int offset){
    if(offset == 0){
        struct timeval tv;
        gettimeofday(&tv, NULL);
        if(boot_time == 0) boot_time = (u_int64_t)tv.tv_sec * 1000000 + (u_int64_t)tv.tv_usec;
        u_int64_t sys_time = (u_int64_t)tv.tv_sec * 1000000 + (u_int64_t)tv.tv_usec - boot_time;
        rtc[0] = (u_int32_t)sys_time;
        rtc[1] = sys_time >> 32;
        // printf("us is %ld\n",tv.tv_usec);
        // printf("s is %ld\n",tv.tv_sec);
        // printf("sys_time is %ld\n",*(u_int64_t*)rtc);
    }

    if(offset == 0){
        return rtc[0];
    }else if(offset == 4) {
        return rtc[1] << 32;
    } else {
        return 0;
    }
}


void init_device(void) {
    void init_sdl() ;
  init_sdl();
}

// void send_key(uint8_t scancode, bool is_keydown);
void poll_event() {
  SDL_Event event;
  while (SDL_PollEvent(&event)) {
    switch (event.type) {
    //   case SDL_QUIT: set_abort();

                     // If a key was pressed
      case SDL_KEYDOWN:
      case SDL_KEYUP: {
                        uint8_t k = event.key.keysym.scancode;
                        bool is_keydown = (event.key.type == SDL_KEYDOWN);
                        send_key(k, is_keydown);
                        break;
                      }
      default: break;
    }
  }
}