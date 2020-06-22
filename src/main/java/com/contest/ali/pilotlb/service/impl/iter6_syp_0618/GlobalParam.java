package com.contest.ali.pilotlb.service.impl.iter6_syp_0618;

/**
 *
 */
public class GlobalParam {
    // 第一次合并的相似度下限
    public static double JAC_MERGE_SIMILARITY = 0.85;
    // 第二次合并的相似度下限
    public static double COV_MERGE_SIMILARITY = 0.8;
    // 第二合并的分割点  后COV_MERGE_RATIO的app向前COV_MERGE_RATIO的app合并
    public static double COV_MERGE_RATIO_1 = 0.2;
    public static double COV_MERGE_RATIO_2 = 1;
    // 第二次合并的次数
    public static int COV_MERGE_CNT = 2;
    // 第二次合并的每个app的加载内存的上限(平均值的倍数)
    public static int COV_MERGE_MEM_UPPER_MUL = 3;



    // 第一次合并的相似度下限
//    public static double JAC_MERGE_SIMILARITY = 0.8;
//    // 第二次合并的相似度下限
//    public static double COV_MERGE_SIMILARITY = 0.8;
//    // 第二合并的分割点  后COV_MERGE_RATIO的app向前COV_MERGE_RATIO的app合并
//    public static double COV_MERGE_RATIO_1 = 0.2;
//    public static double COV_MERGE_RATIO_2 = 1;
//    // 第二次合并的次数
//    public static int COV_MERGE_CNT = 1;
//    public static int COV_MERGE_MEM_UPPER_MUL = 1;



}
