#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#define Cycle 9
void CGRAInputTest(void* dataIn){
  CGRAInput(8*Cycle,dataIn,0b111111100000010);
}

uint32_t inputData[Cycle*2][8] ={
    [0 ... Cycle*2-1] = {1,1,2,4,3,2,3,4}
};


uint64_t cfgData[] ={
0x1000000003,
0x2000000001,
0x3000000001,
0x4000000003,
0x5000000003,
0x6000000003,
0x7000000050,
0x8000000001,
0x11a00070000,
0x1289684de8a,
0x12916426e8c,
0x12a0000a81a,
};
int cfgLen = sizeof(cfgData)/sizeof(cfgData[0]);

uint64_t result  __attribute__((__used__));
uint32_t getCgraOut14(){
    uint32_t* ptr = (uint32_t*)(uintptr_t)(0x02010078 );
    return *ptr;
}

int main() {




CGRACfg(cfgLen,cfgData);
CGRAInputTest(inputData[0]);

result =  getCgraOut14();
// printf("%d ",result);

return 0;
}






