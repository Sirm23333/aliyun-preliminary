package com.contest.ali.pilotlb.service.impl.iter9_syp_0623.model;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Pilot {
    public String name;
    public List<App> appList;
    public ServiceBitMap srvBM;
    public Pilot(String name , List<Service> srvs){
        this.name = name;
        this.appList = new ArrayList<>();
        srvBM = new ServiceBitMap(srvs);
    }
    public void resetSrvList(List<Service> srvs){
        srvBM.resetSrvList(srvs);
    }
    public void addApp(App app){
        this.appList.add(app);
        this.srvBM.addAllService(app.srvBitMap);
    }
}
