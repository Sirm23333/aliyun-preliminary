package com.contest.ali.pilotlb.service.impl.iter2_syp_0608;


import java.util.*;

class GlobalContain {

    // app-name_app_idx
    public static Map<String,Integer> APP_MAP = new HashMap<>();
    // service-name_service-idx
    public static Map<String,Integer> SERVICE_MAP = new HashMap<>();
    // pilot-name_pilot-idx
    public static Map<String,Integer> PILOT_MAP = new HashMap<>();

    // 保存app名字
    public static List<String> APP_NAME_LIST = new ArrayList<>();
    // service名字列表
    public static List<String> SERVICE_NAME_LIST = new ArrayList<>();
    // pilot名字列表
    public static List<String> PILOT_NAME_LIST = new ArrayList<>();


    // app实例数量列表
    public static List<Integer> APP_COUNT_LIST = new ArrayList<>();
    // service数量列表
    public static List<Integer> SERVICE_COUNT_LIST = new ArrayList<>();

    // app依赖列表
    public static List<Set<Integer>> APP_DEPEND_LIST = new ArrayList<>();
    // pilot连接的app列表
    public static List<List<Integer>> PILOT_CONNECTION_LIST = new ArrayList<>();

    // app 数量
    public static int APP_SUM;
    public static int PILOT_SUM;
}
