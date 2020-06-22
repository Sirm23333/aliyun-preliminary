package com.contest.ali.pilotlb.service.impl.iter6_syp_0618;


/**
 *
 */
public class BitMapUtil {

    public static final int LONG_BYTE = 64;

    /**
     * flag记录bitMap非零位
     */
    public static void set(long[] bitMap , int num ){
        int idx = num / LONG_BYTE;
        int pos = num % LONG_BYTE;
        bitMap[idx] |= (1L << pos);
    }
    public static void remove(long[] bitMap , int num){
        int idx = num / LONG_BYTE;
        int pos = num % LONG_BYTE;
        bitMap[idx] &= ~(1L << pos);
    }
    public static long[] and(long[] longs1 , long[] longs2){
        long[] result = new long[longs1.length];
        for(int i = 0; i < result.length; ++i){
            result[i] = longs1[i] & longs2[i];
        }
        return result;
    }

    public static long[] or(long[] longs1 , long[] longs2){
        long[] result = new long[longs1.length];
        for(int i = 0; i < result.length; ++i){
            result[i] = longs1[i] | longs2[i];
        }
        return result;
    }

    public static long[] xor(long[] longs1 , long[] longs2){
        long[] result = new long[longs1.length];
        for(int i = 0; i < result.length; ++i){
            result[i] = longs1[i] ^ longs2[i];
        }
        return result;
    }
    // longs2取反 与 longs1
    public static long[] mask(long[] longs1 , long[] longs2){
        long[] result = new long[longs1.length];
        for(int i = 0; i < result.length; ++i){
            result[i] = longs1[i] & (~longs2[i]);
        }
        return result;
    }
    public static long[] calAppConAndMem(long[] apps){
        long tmp;
        long con = 0;
        long[] srvDepend = new long[GlobalContain.SERVICE_SUM / LONG_BYTE + 1];
        for(int i = 0 , j = 0; i < apps.length; ++i , j = 0){
            tmp = apps[i];
            while(tmp != 0){
                if((tmp & 1) == 1){
                    con += GlobalContain.APP_LIST_MERGE.get(i * LONG_BYTE + j).count;
                    srvDepend = or(srvDepend , GlobalContain.APP_LIST_MERGE.get(i * LONG_BYTE + j).srvDepend);
                }
                tmp >>>= 1;
                j++;
            }
        }
        long[] result = {con , calMem(srvDepend)};
        return result;
    }

    /**
     * 计算连接和内存,并把依赖服务保存值srvDepend,srvDepend要外部置0
     */
    public static long[] calAppConAndMem(long[] apps , long[] srvDepend){
        long tmp;
        long con = 0;
        for(int i = 0 , j = 0; i < apps.length; ++i , j = 0){
            tmp = apps[i];
            while(tmp != 0){
                if((tmp & 1) == 1){
                    con += GlobalContain.APP_LIST_MERGE.get(i * LONG_BYTE + j).count;
                    srvDepend = or(srvDepend , GlobalContain.APP_LIST_MERGE.get(i * LONG_BYTE + j).srvDepend);
                }
                tmp >>>= 1;
                j++;
            }
        }
        long[] result = {con , calMem(srvDepend)};
        return result;
    }
    public static void getAppDepend(long[] apps, long[] srvDepend){
        long tmp;
        for(int i = 0 , j = 0; i < apps.length; ++i , j = 0){
            tmp = apps[i];
            while(tmp != 0){
                if((tmp & 1) == 1){
                    srvDepend = or(srvDepend , GlobalContain.APP_LIST_MERGE.get(i * LONG_BYTE + j).srvDepend);
                }
                tmp >>>= 1;
                j++;
            }
        }
    }

    public static long calMem(long[] srvDepend){
        long tmp;
        long mem = 0;
        for(int i = 0 , j = 0; i < srvDepend.length; ++i , j = 0){
            tmp = srvDepend[i];
            while(tmp != 0){
                if((tmp & 1) == 1){
                    mem += GlobalContain.SERVICE_LIST.get(i * LONG_BYTE + j).count;
                }
                tmp >>>= 1;
                j++;
            }
        }
        return mem;
    }
    public static int count1Num(long[] srvDepend){
        long tmp;
        int cnt = 0;
        for(int i = 0 , j = 0; i < srvDepend.length; ++i , j = 0){
            tmp = srvDepend[i];
            while(tmp != 0){
                if((tmp & 1) == 1){
                    cnt++;
                }
                tmp >>>= 1;
                j++;
            }
        }
        return cnt;
    }
}
