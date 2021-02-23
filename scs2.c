#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>


#define KB 1024
#define MB (1024*1024)

int main(int argc, char *argv[]){
    srand (time(NULL));
    FILE *fp;
    fp = fopen("scs2.out", "w+");
    int line_size = atoi(argv[6]);
    int cpu_cores = atoi(argv[5]);
    int cpu_logical_processores = atoi(argv[4]);

    const int L1_CACHE_SIZE = (atoi(argv[1])*KB/cpu_logical_processores);
    const int L2_CACHE_SIZE =(atoi(argv[2])*KB/cpu_cores);
    const int L3_CACHE_SIZE =(atoi(argv[3])*KB);
    const int NUM_ACCESSES = 1000000;
    const int SECONDS_PER_NS = 1000000000;
    double L1_latency[L1_CACHE_SIZE];
    double L2_latency[L1_CACHE_SIZE];
    double L3_latency[L1_CACHE_SIZE];
    double RAM_latency[L1_CACHE_SIZE];
    for(int j=100;j<L1_CACHE_SIZE;j+=100){
    int *arrayAccess = (int*) malloc(sizeof(int)*L1_CACHE_SIZE);
    int *arrayInvalidateL1=(int*) malloc(sizeof(int)*L1_CACHE_SIZE);
    int *arrayInvalidateL2=(int*) malloc(sizeof(int)*L2_CACHE_SIZE);
    int *arrayInvalidateL3=(int*) malloc(sizeof(int)*L3_CACHE_SIZE);
    int count=0;
    int index=0;
    int i=0;
    struct timespec startAccess, endAccess;
    double mainMemAccess, L1Access, L2Access, L3Access;
    int readValue=0;
    memset(arrayAccess, 0, L1_CACHE_SIZE*sizeof(int));
    memset(arrayInvalidateL1, 0, L1_CACHE_SIZE*sizeof(int));
    memset(arrayInvalidateL2, 0, L2_CACHE_SIZE*sizeof(int));
    memset(arrayInvalidateL3, 0, L3_CACHE_SIZE*sizeof(int));


    index = 0;
    clock_gettime(CLOCK_MONOTONIC, &startAccess); //start clock
    while (index < j) {
        int tmp = arrayAccess[index];               //Access Value from L2
        index = (index + tmp + ((index & 4) ? line_size-4 : line_size+4));   // on average this should give 32 element skips, with changing strides
        count++;                                           //divide overall time by this 
    }
    clock_gettime(CLOCK_MONOTONIC, &endAccess);
    mainMemAccess = ((endAccess.tv_sec - startAccess.tv_sec) * SECONDS_PER_NS) + (endAccess.tv_nsec - startAccess.tv_nsec);
    mainMemAccess /= ((double)count*1.0);

    RAM_latency[j]= mainMemAccess;

    index = 0;
    count=0;
    clock_gettime(CLOCK_MONOTONIC, &startAccess); //start clock
    while (index < j) {
        int tmp = arrayAccess[index];               //Access Value from L2
        index = (index + tmp + ((index & 4) ? line_size-4 : line_size+4));   // on average this should give 32 element skips, with changing strides
        count++;                                           //divide overall time by this 
    }
    clock_gettime(CLOCK_MONOTONIC, &endAccess); //end clock              
    L1Access = ((endAccess.tv_sec - startAccess.tv_sec) * SECONDS_PER_NS) + (endAccess.tv_nsec - startAccess.tv_nsec);
    L1Access /= ((double)count*1.0);

    L1_latency[j] = L1Access;

    //invalidate L1 by accessing all elements of array which is larger than cache
    for(count=0; count < L1_CACHE_SIZE; count++){
        int read = arrayInvalidateL1[count]; 
        read++;
        readValue+=read;               
    }

    index = 0;
    count = 0;
    clock_gettime(CLOCK_MONOTONIC, &startAccess); //start clock
    while (index < j) {
        int tmp = arrayAccess[index];               //Access Value from L2
        index = (index + tmp + ((index & 4) ? line_size-4 : line_size+4));   // on average this should give 32 element skips, with changing strides
        count++;                                           //divide overall time by this 
    }
    clock_gettime(CLOCK_MONOTONIC, &endAccess); //end clock
    L2Access = ((endAccess.tv_sec - startAccess.tv_sec) * SECONDS_PER_NS) + (endAccess.tv_nsec - startAccess.tv_nsec);
    L2Access /=((double)count*1.0);

    L2_latency[j] = L2Access;

    //invalidate L2 by accessing all elements of array which is larger than cache
    for(count=0; count < L2_CACHE_SIZE; count++){
        int read = arrayInvalidateL2[count];  
        read++;
        readValue+=read;                        
    }

    index = 0;
    count=0;
    clock_gettime(CLOCK_MONOTONIC, &startAccess); //sreadValue+=read;tart clock
    while (index < j) {
        int tmp = arrayAccess[index];               //Access Value from L2
        index = (index + tmp + ((index & 4) ? line_size-4 : line_size+4));   // on average this should give 32 element skips, with changing strides
        count++;                                           //divide overall time by this 
    }
    clock_gettime(CLOCK_MONOTONIC, &endAccess); //end clock
    L3Access = ((endAccess.tv_sec - startAccess.tv_sec) * SECONDS_PER_NS) + (endAccess.tv_nsec - startAccess.tv_nsec);
    L3Access /= ((double)count*1.0);

    L3_latency[j] = L3Access;
    //fprintf(fp, "Read Value: %d", readValue);
    //fprintf(fp, "%d\n",j);
    free(arrayAccess);
    free(arrayInvalidateL1);
    free(arrayInvalidateL2);
    free(arrayInvalidateL3);
    }
    double avg_ram = 0;
    double avg_l1 = 0;
    double avg_l2 = 0;
    double avg_l3 = 0;
    for(int j=100;j<L1_CACHE_SIZE;j+=100){
        avg_l1+=L1_latency[j]/cpu_logical_processores;
        //fprintf(fp, "%.8f\n",L1_latency[j]/cpu_logical_processores);
    }
    fprintf(fp, "\n");
    for(int j=100;j<L1_CACHE_SIZE;j+=100){
        avg_l2 += L2_latency[j]/cpu_cores;
        //fprintf(fp, "%.8f\n",L2_latency[j]/cpu_cores);
    }
    fprintf(fp, "\n");
    for(int j=100;j<L1_CACHE_SIZE;j+=100){
        avg_l3 += L3_latency[j]; 
        //fprintf(fp, "%.8f\n",L3_latency[j]);
    }
    fprintf(fp, "\n");
    for(int j=100;j<L1_CACHE_SIZE;j+=100){
        avg_ram += RAM_latency[j];
        //fprintf(fp, "%.8f\n",RAM_latency[j]);
    }
    fprintf(fp, "\n");
    fprintf(fp, "avg L1 = %0.8f\n",avg_l1/(L1_CACHE_SIZE/100));
    fprintf(fp, "avg L2 = %0.8f\n",avg_l2/(L1_CACHE_SIZE/100));
    fprintf(fp, "avg L3 = %0.8f\n",avg_l3/(L1_CACHE_SIZE/100));
    fprintf(fp, "avg Ram = %0.8f\n",avg_ram/(L1_CACHE_SIZE/100));

}