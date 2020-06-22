package com.contest.ali.pilotlb.service.impl.iter1;

import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.service.impl.iter1.model.App;
import com.contest.ali.pilotlb.service.impl.iter1.model.Pilot;
import com.contest.ali.pilotlb.service.impl.iter1.model.Service;
import com.contest.ali.pilotlb.service.impl.iter1.model.pojo.PilotStatistic;
import com.contest.ali.pilotlb.service.MainRun;
import com.contest.ali.pilotlb.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import java.util.*;

@Slf4j
public class MainRunImpl implements MainRun {

    @Override
    public Map<String, List<String>> stage1Run(List<String> pilotNames , String dataPath) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 读并构建数据
        readBuildData(pilotNames,dataPath);
        // 负载均衡处理过程
        staticLB();
        // 转成符合要求的响应格式
        Map<String, List<String>> resultMap = formatOfPilotList(GlobalContain.pilotList);
        stopWatch.stop();
        log.info("stage1Run time : {} ms" , stopWatch.getLastTaskTimeMillis());
        long totalMem = 0 ;
        long[] con = new long[GlobalContain.pilotList.size()];
        long[] mem = new long[GlobalContain.pilotList.size()];

        for(Service service : GlobalContain.serviceSet){
            totalMem+=service.getCount();
        }
        int i = 0;
        for(Pilot pilot : GlobalContain.pilotList){
            con[i] = pilot.getConnectionCnt();
            mem[i] = pilot.getSrvCnt() ;
            i++;
        }
        double[] cons = NumberUtil.calStdAndSum(con);
        double[] mems = NumberUtil.calStdAndSum(mem);
        String s = mems[1] / totalMem + ";" + cons[0] + ";" + mems[0] * 0.01;
        resultMap.put(s,new ArrayList<>());
        return resultMap;
    }

    @Override
    public Map<String, List<String>> stage2Run(JSONObject jsonAppsDependencies) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 接收并处理数据
        List<App> newApps = buildData2(jsonAppsDependencies);
        // 负载均衡处理过程
        dynamicLB(newApps);
        // 转成符合要求的响应格式
        Map<String, List<String>> resultMap = formatOfPilotList(GlobalContain.pilotList);
        stopWatch.stop();
        log.info("stage2Run time : {} ms" , stopWatch.getLastTaskTimeMillis());
        return resultMap;
    }


    /**
     * @author: sirm
     * @description: 阶段一从本地读取数据,并构建需要的全局容器
     * @date: 2020/6/1
     * @return
     */
    protected void readBuildData(List<String> pilotNames,String dataPath){
        DataHandler dataHandler = ObjectFactory.dataHandler;
        // 读本地数据并处理
        JSONObject jsonData = dataHandler.dataReader(dataPath);
        GlobalContain.pilotList = dataHandler.listToPilotsList(pilotNames);
        GlobalContain.appList = dataHandler.jsonToAppsList(jsonData);
        GlobalContain.serviceSet = dataHandler.getServiceSet(GlobalContain.appList);
        GlobalContain.serviceMap = dataHandler.getServiceMap(GlobalContain.appList);
        GlobalContain.pilotStatistic = new PilotStatistic(new int[GlobalContain.pilotList.size()],new int[GlobalContain.pilotList.size()],0,0);
        ObjectFactory.scoreHandler.printAppsOverview(GlobalContain.appList);
    }
    /**
     * @author: sirm
     * @description: 阶段二处理数据并更新全局容器
     * @date: 2020/6/2
     * @return
     */
    protected  List<App> buildData2(JSONObject jsonAppsDependencies){
        DataHandler dataHandler = ObjectFactory.dataHandler;
        List<App> newApps = dataHandler.jsonToAppsList(jsonAppsDependencies);
        for(App app : newApps){
            for(Service service : app.getDependencies()){
                if(!GlobalContain.serviceMap.containsKey(service.getServiceName())){
                    GlobalContain.serviceMap.put(service.getServiceName(),service);
                    GlobalContain.serviceSet.add(service);
                }
            }
        }
        return newApps;
    }


    /**
     * @author: sirm
     * @description: 阶段一的静态数据负载均衡主逻辑
     * @date: 2020/5/29
     * @return
     */
    protected void staticLB(){
        int pilotCount = GlobalContain.pilotList.size();
        int appCount = GlobalContain.appList.size();
        for(int i = 0 ; i < appCount; ++i){
            GlobalContain.pilotList.get(i % pilotCount).addApp(GlobalContain.appList.get(i));
        }
    }
    /**
     * @author: sirm
     * @description: 阶段二的动态数据负载均衡主逻辑
     * @date: 2020/5/29
     * @return
     */
    protected void dynamicLB(List<App> newApps) {
        // 先假设newApps和apps没有重复的
        int pilotCount = GlobalContain.pilotList.size();
        int appCount = newApps.size();
        for(int i = 0 ; i < appCount; ++i){
            GlobalContain.pilotList.get(i % pilotCount).addApp(newApps.get(i));
        }
    }

    protected Map<String, List<String>> formatOfPilotList(List<Pilot> pilots){
        Map<String, List<String>> result = new HashMap<>();
        for(Pilot pilot : pilots){
            List<String> appList = new ArrayList<>();
            for(App app : pilot.getApps()){
                appList.add(app.getAppName());
            }
            result.put(pilot.getPilotName(),appList);
        }
        return result;
    }
}
