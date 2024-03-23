#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#define Cycle 9
void CGRAInputTest(void* dataIn){
  CGRAInput(3*Cycle,dataIn,0b1001001000101100);
}

uint32_t inputData[Cycle][6] ={
    [0 ... Cycle-1] ={4,1,2,6,5,3}
};

uint64_t cfgData[] ={
0x1000000003,
0x2000000002,
0x3000000001,
0x4000000003,
0x5000000001,
0x6000000003,
0x7000000002,
0x8000000002,
0x11a00030000,
0x128ed08d197,
0x12958cd371b,
0x12a000028ad,
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
// printf("%d \n",result);

return 0;
}


