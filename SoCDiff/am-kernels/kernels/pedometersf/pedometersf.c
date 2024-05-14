#include <am.h>
#include <klib.h>
#include <klib-macros.h>
// #define CGRATEST
#define Cycle 1024
#define MMIOTEST

#define KernelInLen 3 
#define KernelOutLen 1
#define TotalKernelNum 1024
#define BenchKernelNum 252/ KernelInLen

#define InBatchLen BenchKernelNum*KernelInLen
#define InTotalLen TotalKernelNum*KernelInLen
#define OutBatchLen BenchKernelNum*KernelOutLen
#define OutTotalLen TotalKernelNum*KernelOutLen




void CGRAWRTest(uint32_t dataIn[] , uint64_t dataOut[]){
    CGRAWR(dataIn , (uint32_t*)(0b1010000011000101), InBatchLen,InTotalLen,dataOut , (uint32_t*)(0b0000000100000000 ) ,OutBatchLen, OutTotalLen);
}



uint32_t inputData[Cycle][6] ={
    [0 ... Cycle-1] ={1,2,3,4,5,6}
};
uint64_t outputData[Cycle];

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
void pedometer(uint32_t  inputData[Cycle][6], uint64_t dataOut[Cycle]){
    for(int i = 0; i <Cycle ; i ++){
        dataOut[i] = (inputData[i][4]- inputData[i][0]) * (inputData[i][4]- inputData[i][0])+  (inputData[i][5]- inputData[i][1]) * (inputData[i][5]- inputData[i][1])+ (inputData[i][2]-inputData[i][3]) * (inputData[i][2]-inputData[i][3]) ;
    }

};
uint64_t cfgData[] ={
0x90c000000,
0xd00020080,
0x1100020080,
0x1500028080,
0x1900028080,
0x1d00020042,
0x2100020010,
0x2500008083,
0x2920004098,
0x2d00008280,
0x3100008280,
0x3500008280,
0x3900008080,
0x3d00008080,
0x4100008000,
0x4500020000,
0x4d00000008,
0x5100010060,
0x5500020300,
0x5900020300,
0x5d00020300,
0x6100020000,
0x6500020000,
0x6903020000,
0x6d00020000,
0x6e00000006,
0x71000a0000,
0x75000a0000,
0x79000a0000,
0x7d00080002,
0x8100080060,
0x8500000103,
0x8900000003,
0xa600000001,
0xa900000300,
0xad00000106,
0xb100000050,
0xca00000004,
0xe900000008,
0xed00010060,
0xf100020000,
0xf200000001,
0xf500020300,
0xf900020300,
0xfd00020300,
0x10100020300,
0x10500020300,
0x10900000102,
0x10d00000050,
0x13500000008,
0x17000000002,
0x17800000001,
0x19c00000003,
0x1cc00000001,
0x1d400000003,
0x1f800000002,
0x23000000003,
0x25000000001,
};

int cfgLen = sizeof(cfgData)/sizeof(cfgData[0]);

#ifdef CGRATEST
uint64_t result  __attribute__((__used__));
#endif

int main() {



#ifdef CGRATEST

CGRACfg(cfgLen,cfgData);
// memset((void*)0x02010088 , 30 ,1);
CGRAWRTest(inputData[0],outputData);
// #ifdef MMIOTEST
// fencei();
// result = outputData[0];
// #endif
// fencei();
// for(int i = 0; i < Cycle; i ++){
//       printf("%d ",outputData[i]);
//     // printf("\n");
// }
#else 
pedometer(inputData,outputData);

#endif

return 0;
}
