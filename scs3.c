#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

#define KB 1024
#define MB (1024*1024)

int main(int argc, char* argv[]){
    srand (time(NULL));
    FILE *fp;
    fp = fopen("scs3.out", "w+");
    int line_size = atoi(argv[6]);
    int cpu_cores = atoi(argv[5]);
    int cpu_logical_processores = atoi(argv[4]);
    int speedL1 = ((int) ceil(atof(argv[7])))*100;
    int speedL2 = ((int) ceil(atof(argv[8])))*100;
    int speedL3 = ((int) ceil(atof(argv[9])))*100;


    const int L1_CACHE_SIZE = (atoi(argv[1])*KB/cpu_logical_processores);
    const int L2_CACHE_SIZE =(atoi(argv[2])*KB/cpu_cores);
    const int L3_CACHE_SIZE =(atoi(argv[3])*KB);
    const int NUM_ACCESSES = 1000000;
    const int SECONDS_PER_NS = 1000000000;
    double L1_latency[L1_CACHE_SIZE];
    double L2_latency[L1_CACHE_SIZE];
    double L3_latency[L1_CACHE_SIZE];
    double RAM_latency[L1_CACHE_SIZE];
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
    while (index < L1_CACHE_SIZE) {
        int tmp = arrayAccess[index];               //Access Value from L2
        index = (index + tmp + ((index & 4) ? line_size-4 : line_size+4));   // on average this should give 32 element skips, with changing strides
        count++;                                           //divide overall time by this 
    }
    clock_gettime(CLOCK_MONOTONIC, &endAccess);
    mainMemAccess = ((endAccess.tv_sec - startAccess.tv_sec) * SECONDS_PER_NS) + (endAccess.tv_nsec - startAccess.tv_nsec);
    mainMemAccess /= ((double)count*1.0);


    index = 0;
    count=0;
    clock_gettime(CLOCK_MONOTONIC, &startAccess); //start clock
    while (index < L1_CACHE_SIZE) {
        int tmp = arrayAccess[index];               //Access Value from L2
        index = (index + tmp + ((index & 4) ? line_size-4 : line_size+4));   // on average this should give 32 element skips, with changing strides
        count++;                                           //divide overall time by this 
    }
    clock_gettime(CLOCK_MONOTONIC, &endAccess); //end clock              
    L1Access = ((endAccess.tv_sec - startAccess.tv_sec) * SECONDS_PER_NS) + (endAccess.tv_nsec - startAccess.tv_nsec);
    L1Access /= ((double)count*1.0);

    //invalidate L1 by accessing all elements of array which is larger than cache
    double miss = 0;
    /*for(count=0; count < 10000; count++){
        clock_gettime(CLOCK_MONOTONIC, &startAccess);
        char read = arrayInvalidateL1[rand() & (L1_CACHE_SIZE-1)]; 
        clock_gettime(CLOCK_MONOTONIC, &endAccess);
        read++;
        readValue+=read;
        double hit = ((endAccess.tv_sec - startAccess.tv_sec) * SECONDS_PER_NS) + (endAccess.tv_nsec - startAccess.tv_nsec);
        if(hit>=500){
            miss++;
        }
        printf( "%d---%.12f\n",count,hit);*/
    //}

    for(count=0; count < L1_CACHE_SIZE; count++){
        clock_gettime(CLOCK_MONOTONIC, &startAccess);
        int read = arrayInvalidateL1[count]; 
        clock_gettime(CLOCK_MONOTONIC, &endAccess);
        read++;
        readValue+=read;
    double hit = ((endAccess.tv_sec - startAccess.tv_sec) * SECONDS_PER_NS) + (endAccess.tv_nsec - startAccess.tv_nsec);
        if(hit>=speedL1){
            miss++;
        }
        printf( "%d---%.12f\n",count,hit);
    }

    index = 0;
    count = 0;
    clock_gettime(CLOCK_MONOTONIC, &startAccess); //start clock
    while (index < L1_CACHE_SIZE) {
        int tmp = arrayAccess[index];               //Access Value from L2
        index = (index + tmp + ((index & 4) ? line_size-4 : line_size+4));   // on average this should give 32 element skips, with changing strides
        count++;                                           //divide overall time by this 
    }
    clock_gettime(CLOCK_MONOTONIC, &endAccess); //end clock
    L2Access = ((endAccess.tv_sec - startAccess.tv_sec) * SECONDS_PER_NS) + (endAccess.tv_nsec - startAccess.tv_nsec);
    L2Access /=((double)count*1.0);

    //invalidate L2 by accessing all elements of array which is larger than cache
    double miss2 = 0;
    double count2 = 0;
    /*for(count=0; count < 10000; count++){
        clock_gettime(CLOCK_MONOTONIC, &startAccess);
        char read = arrayInvalidateL2[rand() & (L2_CACHE_SIZE-1)];
        clock_gettime(CLOCK_MONOTONIC, &endAccess); 
        read++;
        readValue+=read;
        
        double hit = ((endAccess.tv_sec - startAccess.tv_sec) * SECONDS_PER_NS) + (endAccess.tv_nsec - startAccess.tv_nsec);
        if(hit>=1500){
            miss2++;
        }
        fprintf(fp, "%d---%.12f\n",count,hit);
    }*/
    
    

    for(count=0; count < L2_CACHE_SIZE; count++){
        clock_gettime(CLOCK_MONOTONIC, &startAccess);
        int read = arrayInvalidateL2[count];  
        clock_gettime(CLOCK_MONOTONIC, &endAccess);
        read++;
        readValue+=read;
        double hit = ((endAccess.tv_sec - startAccess.tv_sec) * SECONDS_PER_NS) + (endAccess.tv_nsec - startAccess.tv_nsec);
        if(hit>=speedL2){
            miss2++;
        }
        if(hit>=speedL1){
            count2++;
        }
        printf( "%d---%.12f\n",count,hit);                        
    }

    index = 0;
    count=0;
    clock_gettime(CLOCK_MONOTONIC, &startAccess); //sreadValue+=read;tart clock
    while (index < L1_CACHE_SIZE) {
        int tmp = arrayAccess[index];               //Access Value from L2
        index = (index + tmp + ((index & 4) ? line_size-4 : line_size+4));   // on average this should give 32 element skips, with changing strides
        count++;                                           //divide overall time by this 
    }
    clock_gettime(CLOCK_MONOTONIC, &endAccess); //end clock
    L3Access = ((endAccess.tv_sec - startAccess.tv_sec) * SECONDS_PER_NS) + (endAccess.tv_nsec - startAccess.tv_nsec);
    L3Access /= ((double)count*1.0);

    //fprintf(fp, "Read Value: %d", readValue);
    double miss3 = 0;
    double count3 = 0;
    for(count=0; count < L3_CACHE_SIZE; count++){
        clock_gettime(CLOCK_MONOTONIC, &startAccess);
        int read = arrayInvalidateL3[count];  
        clock_gettime(CLOCK_MONOTONIC, &endAccess);
        read++;
        readValue+=read;
        double hit = ((endAccess.tv_sec - startAccess.tv_sec) * SECONDS_PER_NS) + (endAccess.tv_nsec - startAccess.tv_nsec);
        if(hit>=speedL3){
            miss3++;
        }
        if(hit>=speedL2){
            count3++;
        }
        printf( "%d---%.12f\n",count,hit);                        
    }

    fprintf(fp, "%.12f\n",miss/L1_CACHE_SIZE*100);
    fprintf(fp, "%.12f\n",miss2/count2*100);
    fprintf(fp, "%.12f\n",miss3/count3*100);

    free(arrayAccess);
    free(arrayInvalidateL1);
    free(arrayInvalidateL2);
    free(arrayInvalidateL3);

}