#include <am.h>
#include <klib.h>
#include <klib-macros.h>
// #define CGRATEST
// #define MMIOTEST
// #define PRF
#define row_size 3
#define col_size 8
#define Cycle row_size*row_size

void CGRAWRTest(uint32_t dataIn[] , uint64_t dataOut[]){
    CGRAWR(dataIn , (uint32_t*)(0b111111100000010), 8*Cycle ,dataOut , (uint32_t*)(0b100000000000000 ) , 1*Cycle);
}


#define rownum_helper(x) (x * 2)
#define rownum rownum_helper(Cycle)

uint32_t inputData[rownum][8] ={
    [0 ... rownum-1] = {1,1,2,4,3,2,3,4}
};


uint64_t outputData[Cycle];


uint64_t cfgData[] ={
0x1000000003,
0x2000000001,
0x3000000001,
0x4000000003,
0x5000000003,
0x6000000003,
0x7000000050,
0x8000000001,
0x11a00070000,
0x1289684de8a,
0x12916426e8c,
0x12a0000a81a,
};

int cfgLen = sizeof(cfgData)/sizeof(cfgData[0]);



uint32_t m1[row_size][8]={
    [0 ... row_size-1] = {1,2,3,4,1,2,3,4}
}; 
uint32_t m2_3[8][3]={
    {1,1,1},
    {2,2,2},
    {3,3,3},
    {4,4,4},
    {1,1,1},
    {2,2,2},
    {3,3,3},
    {4,4,4},
    
}; 
uint64_t prod[row_size][row_size];
void gemm(uint32_t m1[row_size][8],uint32_t m2[8][row_size]){
    int i, j,k;
    for(i=0;i<row_size;i++) {
        for(j=0;j<row_size;j++) {
            uint64_t sum = 0;
            for(k=0;k<col_size;k++) {
                sum += m1[i][k] * m2[k][j];
            }
            prod[i][j]  = sum;
        }
    }
}
#ifdef MMIOTEST
uint64_t result  __attribute__((__used__));
#endif
int main() {
#ifdef CGRATEST
CGRACfg(cfgLen,cfgData);

CGRAWRTest(inputData[0],outputData);
#ifdef PRF
fencei();
for(int i = 0 ; i < Cycle ; i ++){
    printf("%d ",outputData[i]);
}
#endif
#ifdef MMIOTEST
fencei();
result  = outputData[0];
#endif
// printf("%d\n",result);
#else 
gemm(m1,m2_3);
#ifdef PRF
    int i, j;
    for(i=0;i<row_size;i++) {
        for(j=0;j<row_size;j++) {
             printf("%d ",prod[i][j]);
        }
    }
#endif

#endif


}