#ifndef __DEVICE_H__
#define __DEVICE_H__
#include <stdint.h>

u_int32_t read_time( int offset);
void init_device(void) ;
void poll_event() ;

#endif