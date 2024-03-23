#include <am.h>
#include <io.h>
#include <klib.h>

static Context* (*user_handler)(Event, Context*) = NULL;

Context* __am_irq_handle(Context *c) {
  if (user_handler) {
    // printf("c->mcause is %d\n",c->mcause);
    // printf("c ->gpr[17] is %d\n",c ->gpr[17]);
    Event ev = {0};
    switch (c->mcause) {
      case 11 : 
           switch(c ->gpr[17]){
            //  case 1:ev.event = EVENT_SYSCALL;break; 
             case -1:ev.event = EVENT_YIELD;c -> mepc +=4 ;break; 
             default: ev.event = EVENT_SYSCALL;c -> mepc +=4 ;break; }
           break;
      default: ev.event = EVENT_ERROR; break;
    }
    // printf("c->mcause is %d\n",c->mcause);
    // printf("ev.event is %d\n",ev.event);

    c = user_handler(ev, c);
    assert(c != NULL);
  }

  return c;
}

extern void __am_asm_trap(void);

bool cte_init(Context*(*handler)(Event, Context*)) {
  // initialize exception entry
  asm volatile("csrw mtvec, %0" : : "r"(__am_asm_trap));

  // register event handler
  user_handler = handler;

  return true;
}

// Context *kcontext(Area kstack, void (*entry)(void *), void *arg) {
//   return NULL;
// }

void yield() {
  asm volatile("li a7, -1; ecall");
}

// bool ienabled() {
//   return false;
// }

// void iset(bool enable) {
// }