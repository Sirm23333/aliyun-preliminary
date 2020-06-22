package com.contest.ali.pilotlb.service.impl.iter4_syp_0614;


import com.contest.ali.pilotlb.service.impl.iter4_syp_0614.model.App;
import com.contest.ali.pilotlb.service.impl.iter4_syp_0614.model.Pilot;
import com.contest.ali.pilotlb.service.impl.iter4_syp_0614.model.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalContain {
    // pilot列表
    public static List<Pilot> PILOT_LIST = new ArrayList<>();
    // pilot-name列表
    public static int PILOT_SUM;

    // app列表
    public static List<App> APP_LIST = new ArrayList<>();
    public static int APP_SUM;

    // service-name map
    public static Map<String, Service> SERVICE_NAME_MAP = new HashMap<>();
    // service list
    public static List<Service> SERVICE_LIST = new ArrayList<>();
    public static int SERVICE_SUM;



}
