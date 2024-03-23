#include <klib.h>
#include <klib-macros.h>
#include <stdint.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

size_t strlen(const char *s) {
//   panic("Not implemented");
      int len = 0;
    assert(s);
    while(*s++ != '\0')
        len ++;
    return len;

}

// char *strcpy(char *dst, const char *src) {
//   panic("Not implemented");
// }

char* strcpy(char *dst, const char *src)
{
    char *p = dst;
    while((*dst++ = *src ++) != '\0');
    return p;
}

char *strncpy(char *dst, const char *src, size_t n) {
//   panic("Not implemented");
    char *tmp = dst;

   while (n) {
        if ((*tmp = *src) != 0)
            src++;
        tmp++;
        n--;
    }
    return dst;
}
// char *strcat(char *dst, const char *src) {
//   panic("Not implemented");
// }

char* strcat(char *dst, const char *src)
{
    assert(dst != NULL && src != NULL);
    char *temp = dst;
    while (*temp != '\0')
        temp++;
    while ((*temp++ = *src++) != '\0');

    return dst;
}

// int strcmp(const char *s1, const char *s2) {
//   // panic("Not implemented");

// }
int strcmp(const char *s1, const char *s2)  
{ 
    for ( ; *s1 == *s2; s1++, s2++)  
    {     
        if (*s1 == '\0')  
        return 0;  
    }  
      
    if (*(unsigned char*)s1 > *(unsigned char*)s2){  
        return 1;  
    }else if (*(unsigned char*)s1 < *(unsigned char*)s2){  
        return -1;  
    } else return 0;
}  

int strncmp(const char *s1, const char *s2, size_t n) {

 
    if(!n)return 0;
 
    while(--n && *s1 && (*s1 == *s2))
    {
        s1++;
        s2++;
    }
 
    return *s1 - *s2;
//   panic("Not implemented");
}

// void *memset(void *s, int c, size_t n) {
//   panic("Not implemented");
// }

void *memset(void *s, int c, size_t n){
    	char *pdest = (char *)s;
	while (n-->0)
	{
		*pdest++ = c;
	}
    return s;
}

// void *memset(void *s, int c, size_t n){
// 	// if (s == NULL || n < 0)
// 	// {
// 	// 	return NULL;
// 	// }
//     printf("come to memset\n");
//     printf("addr is %d\n",(uintptr_t) s);
//     printf("len is %d\n",(uintptr_t) n);
// 	// char *pdest = (char *)s;
// 	// while (n-->0)
// 	// {
// 	// 	*pdest++ = c;
// 	// }

//      if(((uintptr_t) s )%8 == 0) {
    
//         uint64_t div = n /8 ;
//         uint64_t off = n %8 ;
//         uint64_t c_64 = (uint64_t)c | (uint64_t)c <<8 | (uint64_t)c<< 16 |(uint64_t)c << 24 | (uint64_t)c << 32 | (uint64_t)c << 40 |(uint64_t)c << 48 |(uint64_t)c << 56;
//         uint64_t* dst_64 = (uint64_t *)s;
//         while(div > 0) {
//             *dst_64++ = c_64;
//             div--;
//         } 
//         char* dst_8 = (char *)dst_64;
//         while(off > 0) {
//             *dst_8++ = c;
//             off --;
//         }   

//      }else if(((uintptr_t) s )%8 + n > 8){
//         uint64_t offbegin = 8 - n %8 ;
//         uint64_t last = n - offbegin;
//         uint64_t div = last/8 ;
//         uint64_t off = last%8 ;
//         char *dest8begin = (char *)s;
//         while (offbegin-->0)
//             {
//                 *dest8begin++ = c;
//             }
//         uint64_t c_64 = (uint64_t)c | (uint64_t)c <<8 | (uint64_t)c<< 16 |(uint64_t)c << 24 || (uint64_t)c << 32 | (uint64_t)c << 40 | (uint64_t)c << 48 | (uint64_t)c << 56;
//         uint64_t* dst_64 = (uint64_t *)dest8begin;
//         while(div > 0) {
//             *dst_64++ = c_64;
//             div--;
//         } 
//         char* dst_8 = (char *)dst_64;
//         while(off > 0) {
//             *dst_8++ = c;
//             off --;
//         }
//      } else{
//         	char *pdest = (char *)s;
//             while (n-->0)
//             {
//                 *pdest++ = c;
//             }
//      }
// 	return s;
// }


void *memmove(void *dst, const void *src, size_t n) {
  panic("Not implemented");
}

void *memcpy(void *s1, const void *s2, size_t n) {
  // panic("Not implemented");
//   printf("com to am!!!!!!\n");
  	if(s1 == NULL || s2 == NULL || n <= 0)
		return s1;
 

	// if(dst_pos < src_pos + n && s1 > s2){ //DOWN COPY，向前拷贝
	// 	dst_pos = dst_pos + n;
	// 	src_pos = src_pos + n;
 
	// 	while(n > 0){
	// 		*dst_pos-- = *src_pos--;
	// 		n --;
	// 	}
	// }
	// else { //UP COPY,向后拷贝
    // printf("come to memcpy\n");
    // printf("addr 1 is %d\n",(uintptr_t) s1);
    // printf("addr 2 is %d\n",(uintptr_t) s2);
    // printf("len is %d\n",(uintptr_t) n);
    // printf("addr1 is %#lx\n",(uintptr_t) s1 );
    // printf("addr1 is %#lx\n",(uintptr_t) s1 );
    if(!((((uintptr_t) s1 )%8 == 0) && (((uintptr_t) s2)%8 == 0))){
    //     prin
    //     printf("addr 1 is %#x\n",(uintptr_t) s1);
    // printf("addr 2 is %#x\n",(uintptr_t) s2);
        uint8_t* dst_pos = (uint8_t *)s1;
	    uint8_t* src_pos = (uint8_t *)s2;
		while(n > 0){
			*dst_pos++ = *src_pos++;
			n--;
		}}else {
            uint64_t div = n /8 ;
            uint64_t off = n %8 ;
         uint64_t* dst_64 = (uint64_t *)s1;
	    uint64_t* src_64 = (uint64_t *)s2;
        while(div > 0) {
            *dst_64++ = *src_64++;
            div--;
        }   
         char* dst_8 = (char *)dst_64;
	    char* src_8 = (char *)src_64;
        while(off > 0) {
            *dst_8++ = *src_8++;
            off --;
        }        
        }
	// }
    // printf("levea am!!!!!!\n");
 
	return s1; 
//       	if(s1 == NULL || s2 == NULL || n <= 0)
// 		return s1;
//         uint64_t div = n /8 ;
//         uint64_t off = n %8 ;

//         // if(s1 < s2 + n && s1 > s2){
//         //     char* dst_8 = (char *)s1 + n;
// 	    //     char* src_8 = (char *)s2 + n;
//         //     while(off > 0){
//         //         *dst_8++ = *src_8++;
//         //     }
//         //     char* dst_64 = (char *)dst_8;
// 	    //     char* src_64 = (char *)src_8;
//         //     while(div > 0) {
//         //     *dst_64++ = *src_64++;
//         //      }

//         // }else{
//         uint64_t* dst_64 = (uint64_t *)s1;
// 	    uint64_t* src_64 = (uint64_t *)s2;
//         while(div > 0) {
//             *dst_64++ = *src_64++;
//             div--;
//         }
//         char* dst_8 = (char *)dst_64;
// 	    char* src_8 = (char *)src_64;
//         while(off > 0) {
//             *dst_8++ = *src_8++;
//             off --;
//         }


//         // }
//         return s1;
}

// int memcmp(const void *s1, const void *s2, size_t n) {
//   panic("Not implemented");
// }

int memcmp(const void * s1, const void * s2, size_t n )

{

// if (!n )
// {return 0;}

	while (--n &&*(char*) s1 == *(char *)s2){
		s1 = (char *)s1 + 1;
		s2 = (char *)s2 + 1;
	}
	return (*(unsigned char*) s1 - *(unsigned char*)s2 );
}

#endif
