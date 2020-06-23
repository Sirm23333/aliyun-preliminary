package com.contest.ali.pilotlb.service.impl.iter8_syp_0622;

import com.contest.ali.pilotlb.service.impl.iter8_syp_0622.model.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalContain {

    // pilot列表
    public static List<Pilot> PILOT_LIST ;
    // pilot数量
    public static int PILOT_SUM;

    // app列表
    public static Map<String,App> APP_NAME_MAP = new HashMap<>();
    public static List<App> APP_LIST_SRC = new ArrayList<>();
    public static int APP_SUM_SRC_PRE;
    public static int APP_SUM_SRC;
    // 合并后app列表
    public static List<App> APP_LIST_MERGE = new ArrayList<>();
    public static int APP_SUM_MERGE;
    // 连接总数
    public static long CON_SUM;

    // service-name map
    public static Map<String, Service> SERVICE_NAME_MAP = new HashMap<>();
    // service list
    public static List<Service> SERVICE_LIST = new ArrayList<>();
    public static int SERVICE_SUM;
    public static long SERVICE_MEM; // 服务内存(未乘0.01)

    public static void init(){
        APP_NAME_MAP = new HashMap<>();
        PILOT_LIST = new ArrayList<>();
        PILOT_SUM = 0;
        APP_LIST_SRC = new ArrayList<>();
        APP_SUM_SRC = 0;
        APP_LIST_MERGE = new ArrayList<>();
        APP_SUM_MERGE = 0;
        CON_SUM = 0;
        SERVICE_NAME_MAP = new HashMap<>();
        SERVICE_LIST = new ArrayList<>();
        SERVICE_SUM = 0;
        SERVICE_MEM = 0;
    }

}
