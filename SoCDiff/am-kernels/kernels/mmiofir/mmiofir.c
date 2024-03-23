#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#define Cycle 9
void CGRAInputTest(void* dataIn){
  CGRAInput(4*Cycle,dataIn,0b111111100000010);
}

uint32_t inputData[Cycle][8] ={
    [0 ... Cycle - 1] = {1,1,1,1,1,1,1,1}
};

uint64_t cfgData[] ={
0x1000000003,
0x2000000003,
0x3000000003,
0x4000000003,
0x5000000003,
0x6000000003,
0x8000000003,
0x9000000001,
0xa000000001,
0xb000000001,
0xc000000001,
0xd000000003,
0xe000000001,
0xf000000001,
0x10000000001,
0x1140000007d,
0x1180a052450,
0x1191731af41,
0x11a0000184d,
0x120ffffff1f,
0x121fffffed4,
0x12200000096,
0x12300000177,
0x12400000028,
0x1250000003d,
0x12700000015,
0x12802852d2e,
0x1298c631868,
0x12a000cc031,
0x13103000000,
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








