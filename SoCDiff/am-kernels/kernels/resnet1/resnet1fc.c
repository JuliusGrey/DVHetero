#include <am.h>
#include <klib.h>
#include <klib-macros.h>
// #define CGRATEST
#define MMIOTEST
// #define PRF
#define Cycle 9


void CGRAWRTest(uint32_t dataIn[] , uint64_t dataOut[]){
    CGRAWR(dataIn , (uint32_t*)(0b1111111111111111), Cycle*8,dataOut , (uint32_t*)(0b1000000 ) , 1*Cycle);
}

uint32_t inputData[Cycle][16] ={
    [0 ... Cycle-1] = {10,4,6,13,5,1,11,14,7,2,16,8,9,3,15,12}
};//744

uint32_t inputDataCPU[Cycle][16] ={
    [0 ... Cycle-1] = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16}
};//744
uint64_t outputData[Cycle];
void resnet1(uint32_t  inputData[Cycle][16], uint64_t dataOut[Cycle]){
    for(int i = 0; i <Cycle ; i ++){
        dataOut[i] = inputData[i][0] *inputData[i][1] + inputData[i][2]*inputData[i][3] +
                        inputData[i][4] *inputData[i][5] + inputData[i][6]*inputData[i][7] +
                        inputData[i][8] *inputData[i][9] + inputData[i][10]*inputData[i][11] +
                        inputData[i][12] *inputData[i][13] + inputData[i][14]*inputData[i][15] ;
    }

};
uint64_t cfgData[] ={
0x1000000003,
0x2000000003,
0x3000000003,
0x4000000003,
0x5000000003,
0x6000000003,
0x7000000003,
0x8000000003,
0x9000000001,
0xa000000001,
0xb000000001,
0xc000000001,
0xd000000001,
0xe000000001,
0xf000000001,
0x11896939482,
0x119a4186103,
0x11a00000358,
0x128ccc411a5,
0x12905882970,
0x12a000051de,
0x13007000000,
};

int cfgLen = sizeof(cfgData)/sizeof(cfgData[0]);


#ifdef MMIOTEST
uint64_t result  __attribute__((__used__));
#endif
int main() {



#ifdef CGRATEST

CGRACfg(cfgLen,cfgData);
CGRAWRTest(inputData[0],outputData);
#ifdef MMIOTEST
fencei();
result = outputData[0];
#endif
#ifdef PRF
fencei();
for(int i = 0; i < Cycle; i ++){
      printf("%d ",outputData[i]);
}
#endif
#else 
resnet1(inputDataCPU,outputData);
#ifdef PRF
for(int i = 0; i < Cycle; i ++){
      printf("%d ",outputData[i]);
}
#endif
#endif


return 0;
}
