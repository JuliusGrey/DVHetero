#ifndef __VGA_H
#define __VGA_H
#include <stdint.h>

void init_sdl();
static inline void update_screen();
u_int32_t vgainfo();
void vga_update_screen(int syn);
void put_pixel(uint64_t addr, uint64_t data);

#endif // __COMMON_H