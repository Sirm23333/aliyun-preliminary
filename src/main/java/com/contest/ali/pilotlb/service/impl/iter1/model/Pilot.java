package com.contest.ali.pilotlb.service.impl.iter1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.*;
import java.util.List;
import java.util.Set;

/**
 *  @author sirm
 *  @Date 2020/5/29 上午11:07
 *  @Description pilot实例
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pilot implements Serializable{


    Integer id;

    String pilotName;

    List<App> apps; // 连接的应用列表

    Set<Service> servers; // 加载的服务集合

    Integer connectionCnt; // app实例连接数,每个pilot尽量平均,所有pilot的和尽量小

    Integer srvCnt;// 加载服务个数,每个pilot尽量平均

    public int addApp(App app){
        apps.add(app);
        int addService = 0;
        connectionCnt += app.getCount();
        for(Service service : app.getDependencies()){
            if(servers.add(service)){
                addService += service.getCount();
            }
        }
        srvCnt += addService;
        return addService;
    }
    public void init(){
        apps.clear();
        servers.clear();
        connectionCnt = 0;
        srvCnt = 0;
    }

}
