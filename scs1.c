#ifndef GET_CACHE_LINE_SIZE_H_INCLUDED
#define GET_CACHE_LINE_SIZE_H_INCLUDED


#include <stddef.h>
#include <stdio.h>
size_t cache_line_size();

#if defined(__APPLE__)

#include <sys/sysctl.h>
size_t cache_line_size() {
    size_t line_size = 0;
    size_t sizeof_line_size = sizeof(line_size);
    sysctlbyname("hw.cachelinesize", &line_size, &sizeof_line_size, 0, 0);
    return line_size;
}

#elif defined(_WIN32)

#include <stdlib.h>
#include <tchar.h>
#include <windows.h>

#define DIV 1048576
FILE *fp;

// Use to convert bytes to MB
//#define DIV 1024

// Specify the width of the field in which to print the numbers. 
// The asterisk in the format specifier "%*I64d" takes an integer 
// argument and uses it to pad and right justify the number.

#define WIDTH 7
size_t cache_line_size() {
    size_t line_size = 0;
    DWORD buffer_size = 0;
    DWORD i = 0;
    SYSTEM_LOGICAL_PROCESSOR_INFORMATION * buffer = 0;

    GetLogicalProcessorInformation(0, &buffer_size);
    buffer = (SYSTEM_LOGICAL_PROCESSOR_INFORMATION *)malloc(buffer_size);
    GetLogicalProcessorInformation(&buffer[0], &buffer_size);
    DWORD cachesize[3];
    cachesize[0]=0;
    cachesize[1]=0;
    cachesize[2]=0;
    for (i = 1; i != buffer_size / sizeof(SYSTEM_LOGICAL_PROCESSOR_INFORMATION); i++) {
        if(buffer[i].Cache.Level == 1){
            cachesize[0]+=buffer[i].Cache.Size;
        }else if(buffer[i].Cache.Level == 2){
            cachesize[1]+=buffer[i].Cache.Size;
            line_size = buffer[i].Cache.LineSize;
        }else if(buffer[i].Cache.Level == 3){
            cachesize[2]+=buffer[i].Cache.Size;
        }
    }
    for(int i=0;i<3;i++){
        fprintf(fp,  "Cache L%d\n",i+1);
        fprintf(fp,  "Cache size %d KB\n",cachesize[i]/1024);
        fprintf(fp,  "Line size %d bytes\n\n",line_size);
    }

    free(buffer);
    return line_size;
}

int main(){
    fp = fopen("scs1.out", "w+");
    cache_line_size();
    MEMORYSTATUSEX statex;
    statex.dwLength = sizeof (statex);
    GlobalMemoryStatusEx (&statex);
    fprintf(fp,  "RAM %d MB \n\n",statex.ullTotalPhys/DIV);
    return 0;
}

#elif defined(linux)

#include <stdio.h>
size_t cache_line_size() {
    FILE * p = 0;
    p = fopen("/sys/devices/system/cpu/cpu0/cache/index0/coherency_line_size", "r");
    unsigned int i = 0;
    if (p) {
        fscanf(p, "%d", &i);
        fclose(p);
    }
    return i;
}

#else
#error Unrecognized platform
#endif

#endif