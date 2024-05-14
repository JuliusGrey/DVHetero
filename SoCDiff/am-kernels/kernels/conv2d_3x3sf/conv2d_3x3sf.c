#include <am.h>
#include <klib.h>
#include <klib-macros.h>
// #define CGRATEST
// #define MMIOTEST
#define ONECYCLY
#define Row 32
#define num (Row - 2)
#define Cycle (num*num)
#define inputNum 9

// #define CFGADDR 0x02010080
// #define DEALYCFG 0x02010088

// void CGRAWRTest(uint32_t dataIn[] , uint64_t dataOut[]){
//     CGRAWR(dataIn , (uint32_t*)(0b111111100010010), Cycle*(9 +1)/2,dataOut , (uint32_t*)(0b100000000000000) , 1*Cycle);
// }

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
// uint32_t inputData[Cycle][10] = { [0 ... Cycle-1] ={3,4,2,2,4,3,2,3,4,0} };

// uint32_t inputData[9][10] ={
//   {3,2,2,4,4,3,2,4,3,0},
//   {3,2,2,4,4,3,2,4,3,0},
//   {3,2,2,4,4,3,2,4,3,0},
//   {4,3,3,5,5,4,3,5,4,0},
//   {4,3,3,5,5,4,3,5,4,0},
//   {4,3,3,5,5,4,3,5,4,0},
//   {5,4,4,6,6,5,4,6,5,0},
//   {5,4,4,6,6,5,4,6,5,0},
//   {5,4,4,6,6,5,4,6,5,0},
// };
// uint64_t outputData[9];

// uint32_t matrix3[3][3] ={
//     {2,2,2},
//     {3,3,3},
//     {4,4,4},
//     // {7,7,7,7,7,7}
// };

// uint32_t matrix4[4][4] ={
//     {2,2,2,2},
//     {3,3,3,3},
//     {4,4,4,4},
//     {5,5,5,5}
//     // {7,7,7,7,7,7}
// };

// uint32_t matrix5[5][5] ={
//     {2,2,2,2,2},
//     {3,3,3,3,3},
//     {4,4,4,4,4},
//     {5,5,5,5,5},
//     {6,6,6,6,6},
//     // {7,7,7,7,7,7}
// };
// uint32_t matrix6[6][6] ={
//     {2,2,2,2,2,2},
//     {3,3,3,3,3,3},
//     {4,4,4,4,4,4},
//     {5,5,5,5,5,5},
//     {6,6,6,6,6,6},
//     {7,7,7,7,7,7}
// };

uint32_t matrix32[32][32] ={ [0 ... 32-1] ={1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10,1,2} };;

uint64_t res[Cycle];

void expressconv2d_3x3(uint32_t dataIn[Row][Row], uint64_t dataOut[Cycle-1]){
    for(int i = 0; i <num ; i ++){//列
        for(int j = 0 ; j < num; j ++){//行
            dataOut[i*num + j] = dataIn[i][j]*3 + dataIn[i][j+1]*3+ dataIn[i][j+2]*3+
                                dataIn[i+1][j]*5 + dataIn[i+1][j+1]*5+ dataIn[i+1][j+2]*5+
                                dataIn[i+2][j]*6 + dataIn[i+2][j+1]*6+ dataIn[i+2][j+2]*6;
        }
    }

};
// uint64_t cfgData[] ={
// 0x1000000001,
// 0x2000000001,
// 0x3000000001,
// 0x4000000001,
// 0x5000000001,
// 0x6000000003,
// 0x7000000001,
// 0x8000000003,
// 0xb000000001,
// 0xd000000003,
// 0xe000000001,
// 0x11400000006,
// 0x11800e03400,
// 0x119100c0000,
// 0x11a0000000f,
// 0x12500000005,
// 0x12700000003,
// 0x128a710dd8b,
// 0x12988552da3,
// 0x12a000ac130,
// 0x13103000000,
// };

// int cfgLen = sizeof(cfgData)/sizeof(cfgData[0]);
// #ifdef MMIOTEST
// uint64_t result  __attribute__((__used__));
// #endif
int main() {



// #ifdef CGRATEST

// CGRACfg(cfgLen,cfgData);
// // memset((void*)0x02010088 , 30 ,1);
// CGRAWRTest(inputData[0],outputData);
// // result = outputData[0];
// #ifdef MMIOTEST
// fencei();
// result  = outputData[0];
// // printf("%d \n ",result);
// #endif
// fencei();
// for(int i = 0; i < 3; i ++){
//     for(int j = 0; j < 3; j ++){
//       printf("%d ",outputData[j + i*3]);
//     }
//     printf("\n");
// }
// #else 
expressconv2d_3x3(matrix32,res);
// for(int i = 0; i < 3; i ++){
//     for(int j = 0; j < 3; j ++){
//       printf("%d ",res[j + i*3]);
//     }
//     printf("\n");
// }

// #endif

return 0;
}
