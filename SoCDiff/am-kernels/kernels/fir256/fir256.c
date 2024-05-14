#include <am.h>
#include <klib.h>
#include <klib-macros.h>

// #define kernelInBatchNum 256
// #define kernelInTotalNum 1024

// #define kernelOutBatchNum 256
// #define kernelOutTotalNum 1024

#define kernelInBatchNum 9
#define kernelInTotalNum 9

#define kernelOutBatchNum 9
#define kernelOutTotalNum 9





uint32_t inputData[kernelInTotalNum][8] __attribute__((aligned(256))) ={
     [0 ... kernelInTotalNum-1] = {1,1,1,1,1,1,1,1}
};


uint32_t outputData[kernelOutTotalNum]__attribute__((aligned(256)));

uint64_t cfgData[][4] __attribute__((aligned(256)))={
{0x1000000003,0,0,0},
{0x2000000003,0,0,0},
{0x3000000003,0,0,0},
{0x4000000003,0,0,0},
{0x5000000003,0,0,0},
{0x6000000003,0,0,0},
{0x7000000003,0,0,0},
{0x8000000003,0,0,0},
{0x9000000001,0,0,0},
{0xa000000001,0,0,0},
{0xb000000001,0,0,0},
{0xc000000001,0,0,0},
{0xd000000001,0,0,0},
{0xe000000001,0,0,0},
{0xf000000001,0,0,0},
{0x11896a39482,0,0,0},
{0x11994186103,0,0,0},
{0x11a00000358,0,0,0},
{0x1200000003d,0,0,0},
{0x12100000028,0,0,0},
{0x1220000007d,0,0,0},
{0x123fffffed4,0,0,0},
{0x12400000015,0,0,0},
{0x12500000096,0,0,0},
{0x12600000177,0,0,0},
{0x127ffffff1f,0,0,0},
{0x128c47330a3,0,0,0},
{0x1298c631803,0,0,0},
{0x12a0000c631,0,0,0},
{0x13007000000,0,0,0},
};
int cfgLen = sizeof(cfgData)/sizeof(cfgData[0]);

#define CGRAWRTest() \
    CGRAWR((void *)inputData[0] , (uint32_t*)(0b1001000011101101),kernelInBatchNum,kernelInTotalNum,(void *)outputData , (uint32_t*)(0b1000000) , kernelOutBatchNum, kernelOutTotalNum)



int main() {
CGRACfg(cfgLen,cfgData);

CGRAWRTest();




}