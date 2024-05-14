#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#define CGRATEST
#define row_size 32
#define col_size 32
// #define Cycle row_size*row_size

// #define Cycle 32*8
// #define KernelInLen 32 
// #define KernelOutLen 1
// #define TotalKernelNum 32
// #define BenchKernelNum 256/ KernelInLen

// #define InBatchLen BenchKernelNum*KernelInLen
// #define InTotalLen TotalKernelNum*KernelInLen
// #define OutBatchLen BenchKernelNum*KernelOutLen
// #define OutTotalLen TotalKernelNum*KernelOutLen

// void CGRAWRTest(uint32_t dataIn[] , uint64_t dataOut[]){
//     CGRAWR(dataIn , (uint32_t*)(0b111111100000010),InBatchLen,InTotalLen,dataOut , (uint32_t*)(0b100000000000000 ) , OutBatchLen, OutTotalLen);
// }


// #define rownum_helper(x) (x * 2)
// #define rownum rownum_helper(Cycle)

// uint32_t inputData[rownum][8] ={
//     [0 ... rownum-1] = {1,1,2,4,3,2,3,4}
// };


// uint64_t outputData[Cycle];


// uint64_t cfgData[] ={
// 0x1000000003,
// 0x2000000001,
// 0x3000000001,
// 0x4000000003,
// 0x5000000003,
// 0x6000000003,
// 0x7000000110,
// 0x8000000001,
// 0x11a00070000,
// 0x1289684de8a,
// 0x12916426e8c,
// 0x12a0000a81a,
// };

// int cfgLen = sizeof(cfgData)/sizeof(cfgData[0]);



uint32_t m1[row_size][col_size]={
    [0 ... row_size-1] = {1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10,1,2}
}; 
uint32_t m2[col_size][row_size]={
    [0 ... col_size-1] = {1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10,1,2}
}; 

// uint32_t m2_1[8][1]={
//     {1},
//     {2},
//     {3},
//     {4},
//     {1},
//     {2},
//     {3},
//     {4},
    
// }; 
// uint32_t m2_2[8][2]={
//     {1,1},
//     {2,2},
//     {3,3},
//     {4,4},
//     {1,1},
//     {2,2},
//     {3,3},
//     {4,4},
    
// }; 
// uint32_t m2_3[8][3]={
//     {1,1,1},
//     {2,2,2},
//     {3,3,3},
//     {4,4,4},
//     {1,1,1},
//     {2,2,2},
//     {3,3,3},
//     {4,4,4},
    
// }; 
// uint32_t m2_4[8][4]={
//     {1,1,1,1},
//     {2,2,2,2},
//     {3,3,3,3},
//     {4,4,4,4},
//     {1,1,1,1},
//     {2,2,2,2},
//     {3,3,3,3},
//     {4,4,4,4},
    
// }; 
// uint32_t m2[32][row_size]={
//     {1,1,1},
//     {2,2,2},
//     {3,3,3},
//     {4,4,4},
//     {1,1,1},
//     {2,2,2},
//     {3,3,3},
//     {4,4,4},
    
// }; 
uint64_t prod[row_size][row_size];
void gemm(uint32_t m1[row_size][col_size],uint32_t m2[col_size][row_size]){
    int i, j,k;
    for(i=0;i<row_size;i++) {
        for(j=0;j<row_size;j++) {
            uint64_t sum = 0;
            for(k=0;k<col_size;k++) {
                sum += m1[i][k] * m2[k][j];
            }
            // printf("%d %d %d \n", i,j,sum);
            prod[i][j]  = sum;
            // prod[i][j] = m1[i][0] * m2[0][j] + m1[i][1] * m2[1][j] + m1[i][2] * m2[2][j] +m1[i][3] * m2[3][j] + 
            // m1[i][4] * m2[4][j]+m1[i][5] * m2[5][j]+m1[i][6] * m2[6][j] +m1[i][7] * m2[7][j];
        }
    }
}
// uint64_t result  __attribute__((__used__));
int main() {
// #ifdef CGRATEST
// CGRACfg(cfgLen,cfgData);

// CGRAWRTest(inputData[0],outputData);
// fencei();
// for(int i = 0 ; i < 32 ; i ++){
//     printf("%d ",outputData[i]);
// }
// // // fencei();
// // result  = outputData[0];
// // printf("%d\n",result);
// #else 
gemm(m1,m2);

    // int i, j;
    // for(i=0;i<row_size;i++) {
    //     for(j=0;j<row_size;j++) {
    //          printf("%d ",prod[i][j]);
    //     }
    // }

// #endif


}