#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#define CGRATEST
#define Cycle 1024 - 7
#define ionum Cycle+7


#define KernelInLen 4
#define KernelOutLen 1

// #define TotalKernelNum 2
// #define BenchKernelNum 1
#define TotalKernelNum 1017
#define BenchKernelNum 256/ KernelInLen

#define InBatchLen BenchKernelNum*KernelInLen
#define InTotalLen TotalKernelNum*KernelInLen
#define OutBatchLen BenchKernelNum*KernelOutLen
#define OutTotalLen TotalKernelNum*KernelOutLen



void CGRAWRTest(uint32_t dataIn[] , uint64_t dataOut[]){
    CGRAWR(dataIn , (uint32_t*)(0b111111100000010), InBatchLen,InTotalLen,dataOut , (uint32_t*)(0b100000000000000 ) , OutBatchLen, OutTotalLen);
}

uint32_t inputData[Cycle][8] ={
    [0 ... Cycle - 1] = {1,1,1,1,1,1,1,1}
};

uint64_t outputData[Cycle];

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




uint32_t coefficients[8]  = {
    025, 150, 375, -225, 050, 075, -300, 125};
uint32_t input[ionum] ={
  [0 ... ionum-1] = 1
};
int output[ionum];
void fir(uint32_t input[ionum],uint32_t coefficients[8] ) 
{
  int i, j;
  for (j = 8-1; j < ionum; ++j)
  {
    int sum = 0;
    for (i = 0; i < 8; ++i)
    {
      sum += input[j - i] * coefficients[i];
    }
    output[j] = sum;
  }
}

uint64_t result  __attribute__((__used__));
int main() {
// #ifdef CGRATEST
// CGRACfg(cfgLen,cfgData);

// CGRAWRTest(inputData[0],outputData);
// // fencei();
// // result  = outputData[0];
// fencei();
// for(int i = 0 ; i < Cycle ; i ++){
//     printf("%d ",outputData[i]);
// }
// #else 
fir(input,coefficients) ;
// for(int i = 0 ; i < Cycle ; i ++){
//     printf("%d ",output[i + 7] );
// }
// #endif

}
