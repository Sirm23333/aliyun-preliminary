package com.contest.ali.pilotlb.service.impl.iter1;
import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.constant.GlobalConstant;
import com.contest.ali.pilotlb.service.impl.iter1.model.App;
import com.contest.ali.pilotlb.service.impl.iter1.model.Pilot;
import com.contest.ali.pilotlb.service.impl.iter1.model.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 *  @author sirm
 *  @Date 2020/5/28 下午9:18
 *  @Description 使用Reader读数据
 */
@Slf4j
class DataHandlerImpl implements DataHandler {

    @Override
    public JSONObject dataReader(String path)  {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String input = null;
        try {
            input = FileUtils.readFileToString(new File(path), "UTF-8");
        } catch (IOException e) {
            log.error("io error!");
            throw new RuntimeException("io error!");        }
        //将读取的数据转换为JSONObject
        JSONObject jsonObject = JSONObject.parseObject(input);
        stopWatch.stop();
//        log.info("load data.json time : {} ms" , stopWatch.getLastTaskTimeMillis());
        return jsonObject;
    }

    @Override
    public List<App> jsonToAppsList(JSONObject data) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        if(!data.containsKey(GlobalConstant.KEY_APPS)){
            log.error("[jsonToAppsList] data error : apps");
            throw new RuntimeException("data error!");
        }
        if(!data.containsKey(GlobalConstant.KEY_DEPENDENCIES)){
            log.error("[jsonToAppsList] data error : dependencies");
            throw new RuntimeException("data error!");
        }
        List<App> apps = new ArrayList<>(); // 所有的应用
        Map<String, Service> services = new HashMap<>(); // 所有的服务
        JSONObject jsonApp = data.getJSONObject(GlobalConstant.KEY_APPS); // 数据一
        JSONObject jsonDependencies = data.getJSONObject(GlobalConstant.KEY_DEPENDENCIES); // 数据二
        Set<String> keys = jsonApp.keySet();// 所有应用名
        for(String key : keys){
            apps.add(new App(apps.size(),key,jsonApp.getInteger(key),new HashSet<>()));
        }

        JSONObject jsonDependTemp;
        Set<String> dependKeys;
        for(App app : apps){
            if(!jsonDependencies.containsKey(app.getAppName())){
                log.error("[jsonToAppsList] cant match apps and dependencies!" );
                throw new RuntimeException("data error!");
            }
            jsonDependTemp = jsonDependencies.getJSONObject(app.getAppName());
            dependKeys = jsonDependTemp.keySet();
            for(String dependKey : dependKeys){
                if(!services.containsKey(dependKey)){
                    services.put(dependKey,new Service(services.size(),dependKey , jsonDependTemp.getInteger(dependKey)));
                }
                app.getDependencies().add(services.get(dependKey));
            }
        }
        stopWatch.stop();
//        log.info("jsonToAppsList time : {} ms" , stopWatch.getLastTaskTimeMillis());
//        log.info("apps count: {}",apps.size());
//        log.info("services count: {}",services.size());
        return apps;
    }

    @Override
    public List<Pilot> listToPilotsList(List<String> pilots) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<Pilot> pilotList = new ArrayList<>();
        for(String pilotName : pilots){
            pilotList.add(new Pilot(pilotList.size(),pilotName,new ArrayList<>(),new HashSet<>(),0,0));
        }
        stopWatch.stop();
//        log.info("listToPilotsList time : {} ms" , stopWatch.getLastTaskTimeMillis());
//        log.info("pilot count : {} , pilot list : {}" , pilotList.size() , pilots);
        return pilotList;
    }

    @Override
    public Set<Service> getServiceSet(List<App> apps) {
        Set<Service> services = new HashSet<>();
        for(App app : apps){
            for(Service service : app.getDependencies()){
                services.add(service);
            }
        }
        return services;
    }

    @Override
    public Map<String, Service> getServiceMap(List<App> apps) {
        Map<String, Service> serviceMap = new HashMap<>();
        for(App app : apps){
            for(Service service : app.getDependencies()){
                serviceMap.put(service.getServiceName(),service);
            }
        }
        return serviceMap;
    }

    @Override
    public void printDatas() {
        log.info("[App sum] : {}" , GlobalContain.appList.size());
        int sum = 0, max = 0, min = Integer.MAX_VALUE;
        for(App app : GlobalContain.appList){
            sum += app.getCount();
            max = max < app.getCount() ? app.getCount() : max;
            min = min > app.getCount() ? app.getCount() : min;
        }
        double avg = (double)sum / GlobalContain.appList.size();
        log.info("[App instance sum] : {}" , sum);
        log.info("[App instance max] : {}" , max);
        log.info("[App instance min] : {}" , min);
        log.info("[App instance avg] : {}" , avg);

        log.info("[Service sum] : {}" , GlobalContain.serviceSet.size());
        sum = 0;max = 0; min = Integer.MAX_VALUE;
        for(Service service : GlobalContain.serviceSet){
            sum += service.getCount();
            max = max < service.getCount() ? service.getCount() : max;
            min = min > service.getCount() ? service.getCount() : min;
        }
        avg = (double) sum / GlobalContain.serviceSet.size();
        log.info("[Service instance sum] : {}" , sum);
        log.info("[Service instance max] : {}" , max);
        log.info("[Service instance min] : {}" , min);
        log.info("[Service instance avg] : {}" , avg);
        Map<String,Integer> srv = new HashMap<>();
        for(App app : GlobalContain.appList){
            for(Service service : app.getDependencies()){
                if(!srv.containsKey(service.getServiceName())){
                    srv.put(service.getServiceName() , 0);
                }
                srv.put(service.getServiceName() , srv.get(service.getServiceName()) + 1);
            }
        }
        Map<Integer , Integer> tol = new HashMap<>();
        for(Integer val : srv.values()){
            if(!tol.containsKey(val)){
                tol.put(val,0);
            }
            tol.put(val , tol.get(val) + 1);
        }
        for(Integer key : tol.keySet()){
            log.info("[Service time {} is {} ] ", key , tol.get(key));
        }
    }


}
