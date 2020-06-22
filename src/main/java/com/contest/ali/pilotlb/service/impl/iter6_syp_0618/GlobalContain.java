package com.contest.ali.pilotlb.service.impl.iter6_syp_0618;



import com.contest.ali.pilotlb.service.impl.iter6_syp_0618.model.App;
import com.contest.ali.pilotlb.service.impl.iter6_syp_0618.model.Pilot;
import com.contest.ali.pilotlb.service.impl.iter6_syp_0618.model.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalContain {
    // pilot列表
    public static List<Pilot> PILOT_LIST = new ArrayList<>();
    // pilot数量
    public static int PILOT_SUM;

    // app列表
    public static List<App> APP_LIST_SRC = new ArrayList<>();
    public static int APP_SUM_SRC;
    public static List<App> APP_LIST_MERGE = new ArrayList<>();
    public static int APP_SUM_MERGE;
    public static long CON_SUM;

    // service-name map
    public static Map<String, Service> SERVICE_NAME_MAP = new HashMap<>();
    // service list
    public static List<Service> SERVICE_LIST = new ArrayList<>();
    public static int SERVICE_SUM;
    public static long SERVICE_MEM; // 服务内存(未乘0.01)



}
