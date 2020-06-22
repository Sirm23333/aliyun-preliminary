package com.contest.ali.pilotlb.service.impl.iter3_syp_0611;

import com.contest.ali.pilotlb.service.impl.iter3_syp_0611.model.App;
import com.contest.ali.pilotlb.service.impl.iter3_syp_0611.model.Pilot;
import com.contest.ali.pilotlb.service.impl.iter3_syp_0611.model.Service;
import com.contest.ali.pilotlb.service.impl.iter3_syp_0611.model.Sim;

import java.util.*;

public class GlobalContain {
    // pilot列表
    public static List<Pilot> PILOT_LIST = new ArrayList<>();

    // app列表
    public static List<App> APP_LIST = new ArrayList<>();
    public static int APP_SUM;

    // service-name map
    public static Map<String,Service> SERVICE_NAME_MAP = new HashMap<>();
    public static int SERVICE_SUM;

    // pilot-name列表
    public static List<String> PILOT_NAME = new ArrayList<>();
    public static int PILOT_SUM;

    // 记录合并后的app两两之间的相似度
    public static Map<String , List<Sim>> similarityArr = new HashMap<>();
}
