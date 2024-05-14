#define CFGADDR 0x02010080
#define CGRAINADDR 0x02010000
#define DEALYCFG 0x02010088
#include <am.h>
#include <io.h>




void fencei(){
    asm volatile("fence.i");
}

void waitRes(int slaveNum){
    uint64_t signal = 0;
    uint64_t* ptr =(uint64_t*)(uintptr_t)(0xb0000000 + slaveNum) ;
    while(signal != 1){
        signal = * ptr>> 63 ;
    }
}



// void dma(uint64_t src , uint64_t dst , uint64_t len){
//     asm volatile (".insn r 0x7b, 6, 6,%0,%1,%2"::"r"(len - 1),"r"(src ),"r"(dst) );
// }

void dma(uint64_t src , uint64_t dst , uint64_t len){
    asm volatile (".insn r 0x7b, 6, 6,%0,%1,%2"::"r"(len ),"r"(src ),"r"(dst) );
}

// void CGRACfg(int lenCfg ,void* cfgData){
//     dma((uint64_t)cfgData,(uint64_t)(0x02010080),lenCfg & 0x7fffffffffffffff);
// }
void CGRAW(long long int lenBatch ,long long int lenTotal ,void* Data,long long int maskIn , long long int maskOut){

    asm volatile (".insn r 0x7b, 6, 6,%0,%1,%2"::
    "r"(lenBatch |lenTotal << 32 ) ,
    "r"((uint64_t)Data |(long long int) 1 <<32),
    "r"((uint64_t)maskIn |  (uint64_t)maskOut<< 32) 
    );

}
void CGRAR(long long int lenBatch ,long long int lenTotal ,void* Data,long long int mask){

    asm volatile (".insn r 0x7b, 7, 7,%0,%1,%2"::
    "r"(lenBatch |lenTotal << 32 ) ,
    "r"((uint64_t)Data| (long long int)2 <<32),
    "r"((uint64_t)mask<< 32) 
    );

}
void CGRACfg(int lenCfg ,void* cfgData){
    dma((uint64_t)cfgData,(uint64_t)(0x02010080),lenCfg);
}

void CGRAInput(int lenCfg , void* dataIn ,long long int mask){
    dma((uint64_t)dataIn,(uint64_t)(mask), (uint64_t)lenCfg);
}

// void CGRAWR(void* from_host , void* to_CGRA , long long int lenR,void* to_host , void* from_CGRA , long long int lenW){
//     asm volatile (".insn r 0x7b, 7, 7,%0,%1,%2"::
//     "r"((lenR - 1) |((lenW - 1) << 32)) ,
//     "r"((uint64_t)from_host | ((uint64_t)to_host << 32) ),
//     "r"((uint64_t)to_CGRA | ((uint64_t)from_CGRA << 32)) 
//     );

// }

void CGRAWR(void* from_host , void* to_CGRA , long long int lenRBatch,long long int lenRTotal,void* to_host , void* from_CGRA , long long int lenWBath,long long int lenWTotal){
    asm volatile (".insn r 0x7b, 7, 7,%0,%1,%2"::
    "r"((lenRBatch)|(lenRTotal) <<16  |((lenWBath ) << 32) |lenWTotal << 48 ) ,
    "r"((uint64_t)from_host | ((uint64_t)to_host << 32) ),
    "r"((uint64_t)to_CGRA | ((uint64_t)from_CGRA << 32)) 
    );

}

// uint32_t getCgraOut4(){
//     uint32_t* ptr = (uint32_t*)(0x02010050 );
//     return *ptr;
// }
// uint32_t getCgraOut9(int outIndex){
//     uint32_t* ptr = (uint32_t*)(0x02010064 );
//     return *ptr;
// }
void ptintfInt(uint32_t data){
    putch(data + '0');
}
//注意dma的时候读出来一个占64位，但mmio一个占32位，因为mmio不用考虑对齐
uint32_t getCgraOut(int portNum){
    uint32_t* ptr = (uint32_t*)(uintptr_t)(0x02010040 + 4*portNum );
    return *ptr;
}
uint32_t getCgraOut0(){
    uint32_t* ptr = (uint32_t*)(uintptr_t)(0x02010040 );
    return *ptr;
}

