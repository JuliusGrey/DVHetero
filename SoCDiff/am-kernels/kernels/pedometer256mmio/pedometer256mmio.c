#include <am.h>
#include <klib.h>
#include <klib-macros.h>
// #define MMIOTEST
// #define kernelInBatchNum 256
// #define kernelInTotalNum 1024

// #define kernelOutBatchNum 256
// #define kernelOutTotalNum 1024
#define kernelInBatchNum 128
#define kernelInTotalNum 128

#define kernelOutBatchNum 128
#define kernelOutTotalNum 128





uint32_t inputData[kernelInTotalNum][8] __attribute__((aligned(256))) ={
     [0 ... kernelInTotalNum-1] = {5,6,2,4,3,1,0,0}
};


uint32_t outputData[kernelOutTotalNum]__attribute__((aligned(256)));

uint64_t cfgData[][4] __attribute__((aligned(256)))={
{0x1000000003,0,0,0},
{0x2000000002,0,0,0},
{0x3000000001,0,0,0},
{0x4000000003,0,0,0},
{0x5000000003,0,0,0},
{0x6000000002,0,0,0},
// {0x6000000001,0,0,0},
{0x700000000f,0,0,0},
{0x8000000002,0,0,0},
{0xd000000001,0,0,0},
{0x11800300000,0,0,0},
{0x11970000000,0,0,0},
{0x1281f58d197,0,0,0},
{0x12958cd3714,0,0,0},
{0x12a0000180b,0,0,0},
{0x13105000000,0,0,0},
};
int cfgLen = sizeof(cfgData)/sizeof(cfgData[0]);

#define CGRAWRTest() \
    CGRAWR((void *)inputData[0] , (uint32_t*)(0b1001001000101100),kernelInBatchNum,kernelInTotalNum,(void *)outputData , (uint32_t*)(0b100000000000000) , kernelOutBatchNum, kernelOutTotalNum)

#define CGRAWTest() \
    CGRAW(kernelInBatchNum ,kernelInTotalNum,(void *)inputData[0],(0b1001001000101100) , (0b100000000000000))


int main() {
CGRACfg(cfgLen,cfgData);
# ifdef MMIOTEST
CGRAWTest();
// printf("%d\n",getCgraOut(0));
#else
CGRAWRTest();
#endif



}