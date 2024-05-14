#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#define CGRATEST
// #define MMIOTEST
#define kernelInBatchNum 128
#define kernelInTotalNum 900

#define kernelOutBatchNum 128
#define kernelOutTotalNum 900
// #define kernelInBatchNum 9
// #define kernelInTotalNum 9

// #define kernelOutBatchNum 9
// #define kernelOutTotalNum 9

// #define CFGADDR 0x02010080
// #define DEALYCFG 0x02010088
//kernel 总数 900个
//batch len 250 个

//一个kernel len = 5
//一个bench 50 个 kernel

//

uint32_t inputData[kernelInTotalNum][16] __attribute__((aligned(256))) ={
     [0 ... kernelInTotalNum-1] = {5,5,5,5,5,5,5,5,5,0,0,0,0,0,0,0}
};
uint32_t outputData[904] __attribute__((aligned(256)));

uint64_t cfgData[][4] __attribute__((aligned(256)))={
{0x100000000f,0,0,0},
{0x2000000001,0,0,0},
{0x3000000001,0,0,0},
{0x4000000001,0,0,0},
{0x5000000001,0,0,0},
{0x600000000f,0,0,0},
{0x7000000001,0,0,0},
{0x9000000001,0,0,0},
{0xa000000001,0,0,0},
{0xb000000003,0,0,0},
{0xc000000003,0,0,0},
{0xd00000000f,0,0,0},
{0xe00000000f,0,0,0},
{0xf000000003,0,0,0},
{0x10000000001,0,0,0},
{0x11200000005,0,0,0},
{0x11300000006,0,0,0},
{0x11600000003,0,0,0},
{0x118d80515af,0,0,0},
{0x11908c44b70,0,0,0},
{0x11a00004c40,0,0,0},
{0x1288355d92a,0,0,0},
{0x12936c10003,0,0,0},
{0x12a000c0101,0,0,0},
{0x13108000000,0,0,0},
};

int cfgLen = sizeof(cfgData)/sizeof(cfgData[0]);

#define CGRAWRTest() \
    CGRAWR((void *)inputData[0] , (uint32_t*)(0b111111100010010),kernelInBatchNum,kernelInTotalNum,(void *)outputData , (uint32_t*)(0b100000000000000) , kernelOutBatchNum, kernelOutTotalNum)


int main() {
CGRACfg(cfgLen,cfgData);
CGRAWRTest();
    // for(int j = 0; j < 904; j ++){
    //   printf("%d : %d \n",j,outputData[j ]);
    // }

return 0;
}
