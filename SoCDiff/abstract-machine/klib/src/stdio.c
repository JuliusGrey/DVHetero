#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)


void itoa(unsigned int n, char * buf)
{
        int i;
        
        if(n < 10)
        {
                buf[0] = n + '0';
                buf[1] = '\0';
                return;
        }
        itoa(n / 10, buf);

        for(i=0; buf[i]!='\0'; i++);
        
        buf[i] = (n % 10) + '0';
        
        buf[i+1] = '\0';
}


int printf(const char *fmt, ...) {
    // memset(str, '\0', sizeof(str));
    int count = 0;
    // char c;
    char *s;
    int n;
    
    // int index = 0;
    // int ret = 2;
    
    char buf[65];
    // char digit[16];
    // int num = 0;
    // int len = 0;
    
    // memset(buf, 0, sizeof(buf));
    // memset(digit, 0, sizeof(digit));

    va_list ap;
    
    va_start(ap, fmt);
    
    while(*fmt != '\0')
    {
        // printf("*fmt=[%c]\n", *fmt);
        if(*fmt == '%')
        {
            fmt++;
            switch(*fmt)
         {
                case 'd': /*整型*/
                {
                        n = va_arg(ap, int);
                        if(n < 0)
                        {
                            putch('-');
                            n = -n;
                        }
                        // printf("case d n=[%d]\n", n);
                        itoa(n, buf);
                        // printf("case d buf=[%s]\n", buf);
                        putstr(buf);
                        break;
                }
                case 's': /*字符串*/
                {
                        s = va_arg(ap, char *);
                        putstr(s);
                        break;
                }
                default:
                        break;
            }
        }
        else
        {
            // *str = *fmt;
            // str++;
            putch(*fmt);
            
            // if(*fmt == '\n')
            // {
                    
            // }
        }
        fmt++;
        count++;
    }

    va_end(ap);

    return count;

}

int vsprintf(char *out, const char *fmt, va_list ap) {
  panic("Not implemented");
}

// int sprintf(char *out, const char *fmt, ...) {
//   panic("Not implemented");
// }


int sprintf(char * str, const char *fmt, ...)
{
    // memset(str, '\0', sizeof(str));
    int count = 0;
    // char c;
    char *s;
    int n;
    
    // int index = 0;
    // int ret = 2;
    
    char buf[65];
    // char digit[16];
    // int num = 0;
    // int len = 0;
    
    // memset(buf, 0, sizeof(buf));
    // memset(digit, 0, sizeof(digit));

    va_list ap;
    
    va_start(ap, fmt);
    
    while(*fmt != '\0')
    {
        // printf("*fmt=[%c]\n", *fmt);
        if(*fmt == '%')
        {
            fmt++;
            switch(*fmt)
         {
                case 'd': /*整型*/
                {
                        n = va_arg(ap, int);
                        if(n < 0)
                        {
                            *str = '-';
                            str++;
                            n = -n;
                        }
                        // printf("case d n=[%d]\n", n);
                        itoa(n, buf);
                        // printf("case d buf=[%s]\n", buf);
                        memcpy(str, buf, strlen(buf));
                        str += strlen(buf);
                        break;
                }
                case 's': /*字符串*/
                {
                        s = va_arg(ap, char *);
                        memcpy(str, s, strlen(s));
                        str += strlen(s);
                        break;
                }
                default:
                        break;
            }
        }
        else
        {
            *str = *fmt;
            str++;
            
            // if(*fmt == '\n')
            // {
                    
            // }
        }
        fmt++;
        count++;
    }

    va_end(ap);

    return count;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
}

#endif
