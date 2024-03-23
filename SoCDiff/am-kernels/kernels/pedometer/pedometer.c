#include <am.h>
#include <klib.h>
#include <klib-macros.h>
// #define CGRATEST
// #define MMIOTEST
// #define PRF
#define Cycle 9


void CGRAWRTest(uint32_t dataIn[] , uint64_t dataOut[]){
    CGRAWR(dataIn , (uint32_t*)(0b1001001000101100), Cycle*3,dataOut , (uint32_t*)(0b100000000000000) , 1*Cycle);
}



uint32_t inputData[Cycle][6] ={
    [0 ... Cycle-1] ={4,1,2,6,5,3}
};
uint64_t outputData[Cycle];
uint32_t inputDataCPU[Cycle][6] ={
    [0 ... Cycle-1] ={1,2,3,4,5,6}
};
void pedometer(uint32_t  inputData[Cycle][6], uint64_t dataOut[Cycle]){
    for(int i = 0; i <Cycle ; i ++){
        dataOut[i] = (inputData[i][1]- inputData[i][0]) * (inputData[i][1]- inputData[i][0])+  (inputData[i][3]- inputData[i][2]) * (inputData[i][3]- inputData[i][2])+ (inputData[i][5]-inputData[i][4]) * (inputData[i][5]-inputData[i][4]) ;
    }

};
uint64_t cfgData[] ={
0x1000000003,
0x2000000002,
0x3000000001,
0x4000000003,
0x5000000001,
0x6000000003,
0x7000000002,
0x8000000002,
0x11a00030000,
0x128ed08d197,
0x12958cd371b,
0x12a000028ad,
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
pedometer(inputDataCPU,outputData);
#ifdef PRF
for(int i = 0; i < Cycle; i ++){
      printf("%d ",outputData[i]);
}
#endif
#endif

return 0;
}
