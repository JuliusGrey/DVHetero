#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#define CGRATEST
#define row_size 3
#define col_size 8
// #define Cycle row_size*row_size

#define kernelInBatchNum 256
#define kernelInTotalNum 32*32*8

#define kernelOutBatchNum 32
#define kernelOutTotalNum 32*32

// #define kernelInBatchNum 18
// #define kernelInTotalNum 18

// #define kernelOutBatchNum 9
// #define kernelOutTotalNum 9





uint32_t inputData[kernelInTotalNum][8] __attribute__((aligned(256))) ={
     [0 ... kernelInTotalNum-1] = {1,1,2,4,3,2,3,4}
};


uint32_t outputData[kernelOutTotalNum]__attribute__((aligned(256)));

uint64_t cfgData[][4] __attribute__((aligned(256)))={
{0x1000000003,0,0,0},
{0x2000000001,0,0,0},
{0x3000000003,0,0,0},
{0x4000000003,0,0,0},
{0x5000000003,0,0,0},
{0x600000000f,0,0,0},
{0x700000000f,0,0,0},
{0x8000000001,0,0,0},
{0xa000000110,0,0,0},
// {0xa000000050,0,0,0},
{0xd000000001,0,0,0},
{0xe00000000f,0,0,0},
{0x1180e8001a0,0,0,0},
{0x119e0000000,0,0,0},
{0x128a014ba6d,0,0,0},
{0x129862a8b8c,0,0,0},
{0x12a0000a800,0,0,0},
{0x13102000000,0,0,0},
};
int cfgLen = sizeof(cfgData)/sizeof(cfgData[0]);

#define CGRAWRTest() \
    CGRAWR((void *)inputData[0] , (uint32_t*)(0b111111100000010),kernelInBatchNum,kernelInTotalNum,(void *)outputData , (uint32_t*)(0b100000000000000) , kernelOutBatchNum, kernelOutTotalNum)



int main() {
CGRACfg(cfgLen,cfgData);

CGRAWRTest();
fencei();
printf(" %d" , outputData[kernelOutTotalNum - 1]);


}