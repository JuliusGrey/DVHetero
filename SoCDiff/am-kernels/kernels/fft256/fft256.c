#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#define CGRATEST
// #define MMIOTEST
#define Cycle 1024


#define KernelInLen 4
#define KernelOutLen 8

// #define TotalKernelNum 2
// #define BenchKernelNum 1
#define TotalKernelNum 1024
#define BenchKernelNum 32

#define InBatchLen BenchKernelNum*KernelInLen
#define InTotalLen TotalKernelNum*KernelInLen
#define OutBatchLen BenchKernelNum*KernelOutLen
#define OutTotalLen TotalKernelNum*KernelOutLen

#define kernelInBatchNum 64
#define kernelInTotalNum 1024

#define kernelOutBatchNum 64
#define kernelOutTotalNum 1024



#define TYPE int

#define THREADS 64
#define cmplx_M_x(a_x, a_y, b_x, b_y) (a_x*b_x - a_y *b_y)
#define cmplx_M_y(a_x, a_y, b_x, b_y) (a_x*b_y + a_y *b_x)
#define cmplx_MUL_x(a_x, a_y, b_x, b_y ) (a_x*b_x - a_y*b_y)
#define cmplx_MUL_y(a_x, a_y, b_x, b_y ) (a_x*b_y + a_y*b_x)
#define cmplx_mul_x(a_x, a_y, b_x, b_y) (a_x*b_x - a_y*b_y)
#define cmplx_mul_y(a_x, a_y, b_x, b_y) (a_x*b_y + a_y*b_x)
#define cmplx_add_x(a_x, b_x) (a_x + b_x)
#define cmplx_add_y(a_y, b_y) (a_y + b_y)
#define cmplx_sub_x(a_x, b_x) (a_x - b_x)
#define cmplx_sub_y(a_y, b_y) (a_y - b_y)
#define cm_fl_mul_x(a_x, b) (b*a_x)
#define cm_fl_mul_y(a_y, b) (b*a_y)



#define FF2(a0_x, a0_y, a1_x, a1_y){			\
    TYPE c0_x = a0_x;		\
    TYPE c0_y = a0_y;		\
    a0_x = cmplx_add_x(c0_x, a1_x);	\
    a0_y = cmplx_add_y(c0_y, a1_y);	\
    a1_x = cmplx_sub_x(c0_x, a1_x);	\
    a1_y = cmplx_sub_y(c0_y, a1_y);	\
}

#define FFT4(a0_x, a0_y, a1_x, a1_y, a2_x, a2_y, a3_x, a3_y){           \
    TYPE exp_1_44_x;		\
    TYPE exp_1_44_y;		\
    TYPE tmp;			\
    exp_1_44_x =  0;		\
    exp_1_44_y =  -1;		\
    FF2( a0_x, a0_y, a2_x, a2_y);   \
    FF2( a1_x, a1_y, a3_x, a3_y);   \
    tmp = a3_x;			\
    a3_x = a3_x*exp_1_44_x-a3_y*exp_1_44_y;     	\
    a3_y = tmp*exp_1_44_y - a3_y*exp_1_44_x;    	\
    FF2( a0_x, a0_y, a1_x, a1_y );                  \
    FF2( a2_x, a2_y, a3_x, a3_y );                  \
}



// void dma(void* src , void* dst , long long int len){
//     asm volatile (".insn r 0x7b, 6, 6,%0,%1,%2"::"r"(len - 1),"r"(src ),"r"(dst) );
// }

// void CGRACfg(int lenCfg ,uint64_t cfgData[]){
//     dma(cfgData,(uint64_t*)(0x02010080),lenCfg & 0x7fffffffffffffff);
// }

// void CGRAInput( uint32_t dataIn[]){
//     dma(dataIn,(uint64_t*)(0x02010000), 0x8000000000000008);
// }

// uint32_t getCgraOut4(){
//     uint32_t* ptr = (uint32_t*)(0x02010050 );
//     return *ptr;
// }
// uint32_t getCgraOut9(int outIndex){
//     uint32_t* ptr = (uint32_t*)(0x02010064 );
//     return *ptr;
// }
// void ptintfInt(uint32_t data){
//     putch(data + '0');
// }
// uint32_t inputData[kernelInTotalNum][8] __attribute__((aligned(256)))={
//      [0 ... Cycle-1] = {1,3,8,15,24,35,48,63}
// };
uint32_t inputDataCGRA[kernelInTotalNum][8] ={
[0 ... kernelInTotalNum-1] = {24,35,3,63,8,48,15,1}
    //  [0 ... Cycle-1] = {5,6,2,8,3,7,4,1}
};
uint32_t outputData[kernelInTotalNum][8] __attribute__((aligned(256)));
uint64_t cfgData[][4] __attribute__((aligned(256)))={
{0x1000000001,0,0,0},
{0x2000000002,0,0,0},
{0x3000000001,0,0,0},
{0x4000000002,0,0,0},
{0x5000000002,0,0,0},
{0x6000000001,0,0,0},
{0x7000000002,0,0,0},
{0x8000000001,0,0,0},
{0x9000000001,0,0,0},
{0xa000000002,0,0,0},
{0xb000000001,0,0,0},
{0xc000000002,0,0,0},
{0xd000000001,0,0,0},
{0xe000000002,0,0,0},
{0xf000000001,0,0,0},
{0x10000000002,0,0,0},
{0x1180e710821,0,0,0},
{0x11942946342,0,0,0},
{0x11a00003188,0,0,0},
{0x128d8c529ce,0,0,0},
{0x12995ac8439,0,0,0},
{0x12a00003192,0,0,0},
{0x13046000502,0,0,0},
{0x13100038017,0,0,0},
};

int cfgLen = sizeof(cfgData)/sizeof(cfgData[0]);



#define CGRAWRTest() \
    CGRAWR((void *)inputDataCGRA[0] , (uint32_t*)(0b101111011010000),kernelInBatchNum,kernelInTotalNum,(void *)outputData[0] , (uint32_t*)(0b1101111000101) , kernelOutBatchNum, kernelOutTotalNum)

#define CGRAWTest() \
    CGRAW(kernelInBatchNum ,kernelInTotalNum,(void *)inputDataCGRA[0],(0b101111011010000) , (0b1101111000101))
// #ifdef CGRATEST
// uint64_t result  __attribute__((__used__));
// #endif

int main() {



#ifdef CGRATEST

CGRACfg(cfgLen,cfgData);
CGRAWRTest();
// CGRAWRTest();
fencei();
// // result = outputData[0][7];
// // for(int i = 0; i < kernelInTotalNum; i ++){
// //       printf("cnt is %d\n ",i);
      printf("%d ",outputData[0][ 5]);
      printf("%d ",outputData[0][ 4]);
      printf("%d ",outputData[0][ 0]);
      printf("%d ",outputData[0][ 6]);
      printf("%d ",outputData[0][ 7]);
      printf("%d ",outputData[0][ 2]);
      printf("%d ",outputData[0][ 3]);
      printf("%d ",outputData[0][ 1]);
//       printf("\n");
// }
    //   printf("%d ",outputData[1023][ 5]);
    //   printf("%d ",outputData[1023][ 4]);
    //   printf("%d ",outputData[1023][ 0]);
    //   printf("%d ",outputData[1023][ 6]);
    //   printf("%d ",outputData[1023][ 7]);
    //   printf("%d ",outputData[1023][ 2]);
    //   printf("%d ",outputData[1023][ 3]);
    //   printf("%d ",outputData[1023][ 1]);
    // printf("\n");
#ifdef MMIOTEST
fencei();
result = outputData[0][7];
// for(int i = 0; i < Cycle; i ++){
//       printf("%d ",outputData[i][ 5]);
//       printf("%d ",outputData[i][ 0]);
//       printf("%d ",outputData[i][ 7]);
//       printf("%d ",outputData[i][ 3]);
//       printf("%d ",outputData[i][ 1]);
//       printf("%d ",outputData[i][ 2]);
//       printf("%d ",outputData[i][ 4]);
//       printf("%d ",outputData[i][ 6]);
//     printf("\n");
// }
#endif
#else 

    for(int i = 0 ; i < Cycle; i ++){

        TYPE a0_x = inputData[i][0];
        TYPE a0_y =inputData[i][1];
        TYPE a1_x =inputData[i][2];
        TYPE a1_y = inputData[i][3];
        TYPE a2_x = inputData[i][4];
        TYPE a2_y =inputData[i][5];
        TYPE a3_x =inputData[i][6];
        TYPE a3_y = inputData[i][7];
        FFT4(a0_x, a0_y, a1_x, a1_y, a2_x, a2_y, a3_x, a3_y);
        outputData[i][0] = a0_x;
        outputData[i][1] = a0_y;
        outputData[i][2] = a1_x;
        outputData[i][3] = a1_y;
        outputData[i][4] = a2_x;
        outputData[i][5] = a2_y;
        outputData[i][6] = a3_x;
        outputData[i][7] = a3_y;
        printf("%d ",a0_x);
        printf("%d ",a0_y);
        printf("%d ",a1_x);
        printf("%d ",a1_y);
        printf("%d ",a2_x);
        printf("%d ",a2_y);
        printf("%d ",a3_x);
        printf("%d ",a3_y);
        printf("\n");
}
#endif

return 0;
}
