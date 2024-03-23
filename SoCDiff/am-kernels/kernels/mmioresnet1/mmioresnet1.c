#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#define Cycle 9

void CGRAInputTest(void* dataIn){
  CGRAInput(8*Cycle,dataIn,0b1111111111111111);
}
uint32_t inputData[Cycle][16] ={
    [0 ... Cycle-1] = {10,4,6,13,5,1,11,14,7,2,16,8,9,3,15,12}
};//744
uint64_t cfgData[] ={
0x1000000003,
0x2000000003,
0x3000000003,
0x4000000003,
0x5000000003,
0x6000000003,
0x7000000003,
0x8000000003,
0x9000000001,
0xa000000001,
0xb000000001,
0xc000000001,
0xd000000001,
0xe000000001,
0xf000000001,
0x11896939482,
0x119a4186103,
0x11a00000358,
0x128ccc411a5,
0x12905882970,
0x12a000051de,
0x13007000000,
};

int cfgLen = sizeof(cfgData)/sizeof(cfgData[0]);

uint64_t result  __attribute__((__used__));

uint32_t getCgraOut6(){
    uint32_t* ptr = (uint32_t*)(uintptr_t)(0x02010058 );
    return *ptr;
}

int main() {




CGRACfg(cfgLen,cfgData);
CGRAInputTest(inputData[0]);

result =  getCgraOut6();
// printf("%d ",result);

return 0;
}







