#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#define Cycle 9
void CGRAInputTest(void* dataIn){
  CGRAInput(5*Cycle,dataIn,0b111111100010010);
}
uint32_t inputData[9][10] ={
  {3,4,2,2,4,3,2,3,4,0},
  {3,4,2,2,4,3,2,3,4,0},
  {3,4,2,2,4,3,2,3,4,0},
  {3,4,2,2,4,3,2,3,4,0},
  {3,4,2,2,4,3,2,3,4,0},
  {3,4,2,2,4,3,2,3,4,0},
  {3,4,2,2,4,3,2,3,4,0},
  {3,4,2,2,4,3,2,3,4,0},
  {3,4,2,2,4,3,2,3,4,0}
};
uint64_t cfgData[] ={
0x1000000001,
0x2000000001,
0x3000000001,
0x4000000001,
0x5000000001,
0x6000000003,
0x7000000001,
0x8000000003,
0xb000000001,
0xd000000003,
0xe000000001,
0x11400000006,
0x11800e03400,
0x119100c0000,
0x11a0000000f,
0x12500000005,
0x12700000003,
0x128a710dd8b,
0x12988552da3,
0x12a000ac130,
0x13103000000,
};
int cfgLen = sizeof(cfgData)/sizeof(cfgData[0]);
uint64_t result  __attribute__((__used__));
uint32_t getCgraOut8(){
    uint32_t* ptr = (uint32_t*)(uintptr_t)(0x02010060 );
    return *ptr;
}
uint32_t getCgraOut14(){
    uint32_t* ptr = (uint32_t*)(uintptr_t)(0x02010078 );
    return *ptr;
}

int main() {




CGRACfg(cfgLen,cfgData);
CGRAInputTest(inputData[0]);

result =   getCgraOut14();
// printf("%d\n",result);

return 0;
}

