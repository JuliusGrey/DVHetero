#include <am.h>
#include <klib.h>
#include <klib-macros.h>
// #define CGRATEST
// #define MMIOTEST
// #define PRF
#define Cycle 9

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

void CGRAWRTest(void* dataIn , void* dataOut){
    CGRAWR(dataIn , (uint32_t*)(0b101111011010000),Cycle*4 ,dataOut , (uint32_t*)(0b1101111000101) , Cycle*8);
}
uint32_t inputData[Cycle][8] ={
     [0 ... Cycle-1] = {1,2,3,4,5,6,7,8}
};
uint32_t inputDataCGRA[Cycle][8] ={
     [0 ... Cycle-1] = {5,6,2,8,3,7,4,1}
};
uint64_t outputData[Cycle][8];
uint64_t cfgData[] ={
0x1000000001,
0x2000000002,
0x3000000001,
0x4000000002,
0x5000000002,
0x6000000001,
0x7000000002,
0x8000000001,
0x9000000001,
0xa000000002,
0xb000000001,
0xc000000002,
0xd000000001,
0xe000000002,
0xf000000001,
0x10000000002,
0x1180e710821,
0x11942946342,
0x11a00003188,
0x128d8c529ce,
0x12995ac8439,
0x12a00003192,
0x13046000502,
0x13100038017,
};

int cfgLen = sizeof(cfgData)/sizeof(cfgData[0]);

#ifdef MMIOTEST
uint64_t result  __attribute__((__used__));
#endif

int main() {



#ifdef CGRATEST

CGRACfg(cfgLen,cfgData);
CGRAWRTest(inputDataCGRA[0],outputData);
#ifdef PRF
fencei();
for(int i = 0; i < Cycle; i ++){
      printf("%d ",outputData[i][ 5]);
      printf("%d ",outputData[i][ 4]);
      printf("%d ",outputData[i][ 0]);
      printf("%d ",outputData[i][ 6]);
      printf("%d ",outputData[i][ 7]);
      printf("%d ",outputData[i][ 2]);
      printf("%d ",outputData[i][ 3]);
      printf("%d ",outputData[i][ 1]);
    printf("\n");
}
#endif
#ifdef MMIOTEST
fencei();
result = outputData[0][7];
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
#ifdef PRF
        printf("%d ",a0_x);
        printf("%d ",a0_y);
        printf("%d ",a1_x);
        printf("%d ",a1_y);
        printf("%d ",a2_x);
        printf("%d ",a2_y);
        printf("%d ",a3_x);
        printf("%d ",a3_y);
        printf("\n");
#endif
}
#endif

return 0;
}
