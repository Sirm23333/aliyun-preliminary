package com.contest.ali.pilotlb.service.impl.iter1;

import com.contest.ali.pilotlb.service.impl.iter1.model.App;
import com.contest.ali.pilotlb.service.impl.iter1.model.Pilot;
import com.contest.ali.pilotlb.service.impl.iter1.model.Service;
import com.contest.ali.pilotlb.service.impl.iter1.model.pojo.PilotStatistic;

import java.io.Serializable;
import java.util.*;

/**
 *  @author sirm
 *  @Date 2020/5/28 下午9:13
 *  @Description 全局使用的数据
 */
class GlobalContain implements Serializable {
    // 所有的应用列表
    public static List<App> appList;

    // 所有的pilot
    public static List<Pilot> pilotList;

    // 所有服务集合
    public static Set<Service> serviceSet;

    // srv-name_srv-obj map
    public static Map<String , Service> serviceMap;

    // pilot列表的某些统计值
    public static PilotStatistic pilotStatistic;

    public static void initPilotList(){
        for(Pilot pilot : GlobalContain.pilotList){
            pilot.init();
        }
    }

    public static void initPilotStatistic(){
        pilotStatistic.init();
    }

}
