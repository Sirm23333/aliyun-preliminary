package com.contest.ali.pilotlb.constant;


public class GlobalConstant {

    public final static String KEY_APPS = "apps";

    public final static String KEY_DEPENDENCIES = "dependencies";

    public final static String KEY_PILOTS = "pilots";

    public final static String STAGE_1 = "stage1";

    public final static String STAGE_2 = "stage2";

    public static long START_TIME;
    public static long END_TIME;
    // 阶段1时间限制(ms) 2分钟
    public final static long STAGE_1_TIME_LIMIT = 2 * 60 * 1000 ;
//    public final static long STAGE_1_TIME_LIMIT = 5 * 1000 ;
    // 阶段2时间限制(ms) 15秒
    public final static long STAGE_2_TIME_LIMIT = 15 * 1000 ;
//    public final static long STAGE_2_TIME_LIMIT = 500 ;
}
