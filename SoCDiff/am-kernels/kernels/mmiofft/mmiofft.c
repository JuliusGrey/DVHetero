#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#define Cycle 9
void CGRAInputTest(void* dataIn){
  CGRAInput(4*Cycle,dataIn,0b101111011010000);
}


uint32_t inputData[9][8] ={
  {2,8,7,6,3,4,5,1},
  {2,8,7,6,3,4,5,1},
  {2,8,7,6,3,4,5,1},
  {2,8,7,6,3,4,5,1},
  {2,8,7,6,3,4,5,1},
  {2,8,7,6,3,4,5,1},
  {2,8,7,6,3,4,5,1},
  {2,8,7,6,3,4,5,1},
  {2,8,7,6,3,4,5,1}
};

uint64_t cfgData[] ={
0x1000000001,
0x2000000002,
0x3000000001,
0x4000000002,
0x5000000002,
0x6000000001,
0x7000000002,
0x8000000001,
0x9000000001,
0xa000000002,
0xb000000001,
0xc000000002,
0xd000000001,
0xe000000002,
0xf000000001,
0x10000000002,
0x1180e710821,
0x11942946342,
0x11a00003188,
0x128d8c529ce,
0x12995ac8439,
0x12a00003192,
0x13046000502,
0x13100038017,
};
int cfgLen = sizeof(cfgData)/sizeof(cfgData[0]);
uint64_t result  __attribute__((__used__));

int main() {




CGRACfg(cfgLen,cfgData);
CGRAInputTest(inputData[0]);

result =  getCgraOut0();

return 0;
}
