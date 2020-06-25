package com.contest.ali.pilotlb.service.impl.iter11_syp_0625;

import com.contest.ali.pilotlb.service.impl.iter11_syp_0625.model.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalContain {

    public static List<Pilot> PILOT_LIST ;      // pilot list
    public static List<App> APP_LIST_SRC ;      // 原始的app列表
    public static List<App> APP_LIST_MERGE;     // 一次合并后app列表
    public static List<App> APP_LIST_MERGE_II ; // 二次合并后app列表
    public static List<Service> SERVICE_LIST;   // service list

    public static int PILOT_SUM;            // pilot sum
    public static int APP_SUM_SRC;          // APP_LIST_SRC数量
    public static int APP_SUM_SRC_PRE;      // 上一次APP_LIST_SRC数量
    public static int APP_SUM_MERGE;        // 一次合并后app数量
    public static int APP_SUM_MERGE_II;     // 二次合并后app数量
    public static long SERVICE_SUM;         // 服务总数
    public static long CON_SUM;             // 连接总数
    public static long SERVICE_MEM;         // 服务内存(未乘0.01)

    public static Map<String,App> APP_NAME_MAP;           // 原始的app name -> obj
    public static Map<String,App> APP_MERGE_NAME_MAP;     // 一次合并后 app name -> obj  , {"testapp1;testapp2":app-obj}
    public static Map<String,App> APP_MERGE_NAME_II_MAP;  // 二次合并后 app name -> obj  , {"testapp1;testapp2:testapp3":app-obj}
    public static Map<String, Service> SERVICE_NAME_MAP;  // service-name -> obj

    public static void init(){
        PILOT_LIST = new ArrayList<>();
        APP_LIST_SRC = new ArrayList<>();
        APP_LIST_MERGE = new ArrayList<>();
        APP_LIST_MERGE_II = new ArrayList<>();
        SERVICE_LIST = new ArrayList<>();

        PILOT_SUM = 0;
        APP_SUM_SRC = 0;
        APP_SUM_SRC_PRE = 0;
        APP_SUM_MERGE = 0;
        APP_SUM_MERGE_II = 0;
        CON_SUM = 0;
        SERVICE_SUM = 0;
        SERVICE_MEM = 0;

        APP_NAME_MAP = new HashMap<>();
        APP_MERGE_NAME_MAP = new HashMap<>();
        APP_MERGE_NAME_II_MAP = new HashMap<>();
        SERVICE_NAME_MAP = new HashMap<>();
    }

}
