#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#define CGRATEST
#define Cycle 2
#define MMIOTEST


// #define KernelInLen 8 
// #define KernelOutLen 1
#define TotalKernelNum 19
#define BenchKernelNum 9

#define InBatchLen BenchKernelNum*KernelInLen
#define InTotalLen TotalKernelNum*KernelInLen
#define OutBatchLen BenchKernelNum*KernelOutLen
#define OutTotalLen TotalKernelNum*KernelOutLen

#define kernelInBatchNum 128
#define kernelInTotalNum 1024


// struct _uint256_t {
//     uint64_t bits[4]; 
// }


void CGRAWRTest(uint32_t dataIn[] , uint32_t dataOut[]){
    // printf("in addr is %d\n",(void *)dataIn);
    // printf("out addr is %d\n",(void *)dataOut);
    CGRAWR(dataIn , (uint32_t*)(0b1111111111111111), BenchKernelNum,TotalKernelNum,dataOut , (uint32_t*)(0b1000000 ) , BenchKernelNum, TotalKernelNum);
}

uint32_t inputData[kernelInTotalNum][16] __attribute__((aligned(256)))={
    [0 ... kernelInTotalNum-1] = {10,4,6,13,5,1,11,14,7,2,16,8,9,3,15,12}
};//744

uint32_t inputDataCPU[Cycle][16] ={
    [0 ... Cycle-1] = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16}
};//744
uint32_t outputData[5][8] __attribute__((aligned(256)));

// void pedometer(){
// 	for(int i=0;i<SIZE;i++){
// 		acc_vector[i] = (acceleration_x[i]- acc_avg_x[i]) * (acceleration_x[i]- acc_avg_x[i])+  (acceleration_y[i]- acc_avg_y[i]) * (acceleration_y[i]- acc_avg_y[i])+ (acceleration_z[i]-acc_avg_z[i]) * (acceleration_z[i]-acc_avg_z[i]) ;
// 	}
// }
//acceleration_x 0
//acceleration_y 1
//acceleration_z 2
//acc_avg_x 3
//acc_avg_y 4 
//acc_avg_z 4
void resnet1(uint32_t  inputData[Cycle][16], uint64_t dataOut[Cycle]){
    for(int i = 0; i <Cycle ; i ++){
        dataOut[i] = inputData[i][0] *inputData[i][1] + inputData[i][2]*inputData[i][3] +
                        inputData[i][4] *inputData[i][5] + inputData[i][6]*inputData[i][7] +
                        inputData[i][8] *inputData[i][9] + inputData[i][10]*inputData[i][11] +
                        inputData[i][12] *inputData[i][13] + inputData[i][14]*inputData[i][15] ;
    }

};
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
{0x11896939482,0,0,0},
{0x119a4186103,0,0,0},
{0x11a00000358,0,0,0},
{0x128ccc411a5,0,0,0},
{0x12905882970,0,0,0},
{0x12a000051de,0,0,0},
{0x13007000000,0,0,0}
};

int cfgLen = sizeof(cfgData)/sizeof(cfgData[0]);
#define CGRAWTest() \
    CGRAW(kernelInBatchNum ,kernelInTotalNum,(void *)inputData[0],(0b1111111111111111) , (0b1000000))


// #ifdef CGRATEST
uint32_t result  __attribute__((__used__));
// #endif


uint32_t getCgraOut6(){
    uint32_t* ptr = (uint32_t*)(uintptr_t)(0x02010058 );
    return *ptr;
}

int main() {



#ifdef CGRATEST

CGRACfg(cfgLen,cfgData[0]);
CGRAWTest();
result = getCgraOut6();
printf("result is %d\n" , result );
// memset((void*)0x02010088 , 30 ,1);
// CGRAWRTest(inputData[0],outputData[0]);
// // #ifdef MMIOTEST
// fencei();
// // result = outputData[0];
// for(int i = 0; i < 5; i ++){
//     for(int j = 0 ; j <8 ; j++)
// printf("%d ",outputData[i][j]);
// printf("\n");
// }
// #endif
// fencei();
//  printf("%d ",outputData[0]);
// for(int i = 0; i < Cycle; i ++){
//       printf("%d ",outputData[i]);
//     // printf("\n");
// }
#else 
resnet1(inputDataCPU,outputData);

#endif
// for(int i = 0; i < Cycle; i ++){
//       printf("%d ",outputData[i]);
//     // printf("\n");
// }

return 0;
}
