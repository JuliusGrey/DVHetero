#ifndef __KEYBOARD_H__
#define __KEYBOARD_H__
#include <stdint.h>

void send_key(uint8_t scancode, bool is_keydown);
uint32_t read_key(void);

#endif