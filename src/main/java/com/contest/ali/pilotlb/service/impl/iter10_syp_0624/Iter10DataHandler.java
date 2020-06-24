package com.contest.ali.pilotlb.service.impl.iter10_syp_0624;

import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.constant.GlobalConstant;
import com.contest.ali.pilotlb.service.DataHandler;
import com.contest.ali.pilotlb.service.impl.iter10_syp_0624.model.*;
import com.contest.ali.pilotlb.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
public class Iter10DataHandler implements DataHandler {

    // 第一次合并的相似度下限,将app相似度超过similarity的合并
    private double jacSimilarity = 0.9;
    // 第二次合并的相似度下限
    private double coverSimilarity = 0.8;
    // 第二合并允许的合并后app的内存上限 (平均值的倍数)
    private int secondMergeMemUpper = 3;

    public JSONObject dataReader(String path) {
        String input ;
        try {
            input = FileUtils.readFileToString(new File(path), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("io error!");        }
        JSONObject jsonObject = JSONObject.parseObject(input);
        return jsonObject;
    }

    /**
     * 第一阶段 读数据 并构建数据结构
     */
    public void initDataStage1(List<String> pilotNames , String dataPath ){
        GlobalContain.init();
        GlobalContain.PILOT_SUM = pilotNames.size();
        JSONObject data = dataReader(dataPath);
        JSONObject apps = data.getJSONObject(GlobalConstant.KEY_APPS);
        JSONObject dependencies = data.getJSONObject(GlobalConstant.KEY_DEPENDENCIES);
        JSONObject appDependencies ; // 某一个app的依赖,做遍历时临时变量用
        Set<String> serviceNames ; // 某一个app的依赖集合,做遍历时临时变量用
        Set<String> appNames = apps.keySet();
        int tmpCon ;
        for(String appName : appNames){
            tmpCon = apps.getInteger(appName);
            GlobalContain.CON_SUM += tmpCon;
            App app = new App( 0 , appName , apps.getInteger(appName)  , null , new ArrayList<>(),0  );
            appDependencies = dependencies.getJSONObject(appName);
            serviceNames = appDependencies.keySet();
            for(String serviceName : serviceNames){
                Service service = GlobalContain.SERVICE_NAME_MAP.get(serviceName);
                if(service == null){
                    service = new Service(GlobalContain.SERVICE_NAME_MAP.size() , serviceName , appDependencies.getInteger(serviceName));
                    GlobalContain.SERVICE_NAME_MAP.put(serviceName , service);
                    GlobalContain.SERVICE_LIST.add(service);
                    GlobalContain.SERVICE_MEM += service.count;
                }
                app.srvMem += service.count;
                app.srvList.add(service);
            }
            GlobalContain.APP_NAME_MAP.put(app.name , app);
            GlobalContain.APP_LIST_SRC.add(app);
        }
        GlobalContain.SERVICE_SUM = GlobalContain.SERVICE_NAME_MAP.size();
        GlobalContain.APP_SUM_SRC = GlobalContain.APP_LIST_SRC.size();

        // 初始化pilot列表
        for(String name : pilotNames){
            GlobalContain.PILOT_LIST.add(new Pilot(name , GlobalContain.SERVICE_LIST ));
        }
        // 构建app依赖bitmap
        for(int i = 0; i < GlobalContain.APP_SUM_SRC; ++i){
            GlobalContain.APP_LIST_SRC.get(i).srvBitMap = new ServiceBitMap( GlobalContain.SERVICE_LIST);
            GlobalContain.APP_LIST_SRC.get(i).updateBitMapByList();
        }

        // 一次合并
        mergeAppByJacSimilarity(GlobalContain.APP_LIST_SRC,jacSimilarity);
        printAppListFeature( GlobalContain.APP_LIST_MERGE);

        // 二次合并
        mergeAppByCoverSimilarity(GlobalContain.APP_LIST_MERGE,coverSimilarity,
                GlobalContain.SERVICE_MEM / GlobalContain.PILOT_SUM * secondMergeMemUpper,
                GlobalContain.CON_SUM / GlobalContain.PILOT_SUM );
        printAppListFeature( GlobalContain.APP_LIST_MERGE_II);
    }

    /**
     * 第二阶段处理数据
     */
    public  void initDataStage2(JSONObject jsonAppsDependencies){
        GlobalContain.APP_SUM_SRC_PRE = GlobalContain.APP_SUM_SRC;
        JSONObject apps = jsonAppsDependencies.getJSONObject(GlobalConstant.KEY_APPS);
        JSONObject dependencies = jsonAppsDependencies.getJSONObject(GlobalConstant.KEY_DEPENDENCIES);
        JSONObject appDependencies ; // 某一个app的依赖,做遍历时临时变量用
        Set<String> serviceNames ; // 某一个app的依赖集合,做遍历时临时变量用
        Set<String> appNames = apps.keySet();
        int tmpCon ;
        for(String appName : appNames){
            tmpCon = apps.getInteger(appName);
            GlobalContain.CON_SUM += tmpCon;
            App app = new App( 0 , appName , apps.getInteger(appName)  , null , new ArrayList<>(),0  );
            appDependencies = dependencies.getJSONObject(appName);
            serviceNames = appDependencies.keySet();
            for(String serviceName : serviceNames){
                Service service = GlobalContain.SERVICE_NAME_MAP.get(serviceName);
                if(service == null){
                    service = new Service(GlobalContain.SERVICE_NAME_MAP.size() , serviceName , appDependencies.getInteger(serviceName));
                    GlobalContain.SERVICE_NAME_MAP.put(serviceName , service);
                    GlobalContain.SERVICE_LIST.add(service);
                    GlobalContain.SERVICE_MEM += service.count;
                }
                app.srvMem += service.count;
                app.srvList.add(service);
            }
            GlobalContain.APP_NAME_MAP.put(app.name , app);
            GlobalContain.APP_LIST_SRC.add(app);
        }
//        log.info("新增应用{},新增服务{}",GlobalContain.APP_LIST_SRC.size() - GlobalContain.APP_SUM_SRC_PRE ,GlobalContain.SERVICE_NAME_MAP.size() - GlobalContain.SERVICE_SUM );
        GlobalContain.SERVICE_SUM = GlobalContain.SERVICE_NAME_MAP.size();
        GlobalContain.APP_SUM_SRC = GlobalContain.APP_LIST_SRC.size();
        // 构建新加app依赖bitmap
        for(int i = GlobalContain.APP_SUM_SRC_PRE; i < GlobalContain.APP_SUM_SRC; ++i){
            GlobalContain.APP_LIST_SRC.get(i).srvBitMap = new ServiceBitMap( GlobalContain.SERVICE_LIST);
            GlobalContain.APP_LIST_SRC.get(i).updateBitMapByList();
        }
        // 更新pilot列表
        for(Pilot pilot : GlobalContain.PILOT_LIST){
            pilot.resetSrvList(GlobalContain.SERVICE_LIST);
        }
    }

    /**
     * 对给定的app按照similarity合并,返回一个新的app列表
     * 合并后app名字以;分割
     */
    private void mergeAppByJacSimilarity(List<App> apps , double similarity) {
        mergeAppByJacSimilarityRange(apps,0,1,similarity);
    }

    /**
     * 将apps的 radio1到radio2范围内的按照相似度similarity合并
     */
    private void  mergeAppByJacSimilarityRange(List<App> appsSrc , double radio1 , double radio2 , double similarity)  {
        List<App> apps = new ArrayList<>();
        apps.addAll(appsSrc);
        Collections.sort(apps);
        int start = (int) Math.round(apps.size() * radio1);
        int end = (int) Math.round(apps.size() * radio2);
        int length = end - start;
        // 记录app是否已经合并
        boolean[] merged = new boolean[length];
        Queue<Integer> mergeQueue = new ArrayDeque<>();
        App app1 , app2;
        double tmpSimilarity;
        for(int i = 0; i < start; ++i){
            try {
                App app = apps.get(i).clone();
                GlobalContain.APP_LIST_MERGE.add(app );
                GlobalContain.APP_MERGE_NAME_MAP.put(app.name , app);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        for(int i = start; i < end; ++i){
            if(!merged[i]){
                StringBuffer mergedAppName = new StringBuffer();
                int mergedAppCount = 0;
                ServiceBitMap mergedSrvBitMap = new ServiceBitMap( GlobalContain.SERVICE_LIST);
                mergeQueue.add(i);
                merged[i] = true;
                while(!mergeQueue.isEmpty()){
                    int tmp = mergeQueue.poll();
                    app1 = apps.get(tmp);
                    mergedAppName.append(app1.name+";");
                    mergedAppCount += app1.count;
                    mergedSrvBitMap.addAllService(app1.srvBitMap);
                    for(int j = i + 1; j < end; ++j){
                        app2 = apps.get(j);
                        long maxK = Math.max(app1.srvMem,app2.srvMem);
                        if(!merged[j] && Math.abs(app1.srvMem - app2.srvMem) < (1 - similarity) * maxK){
                            tmpSimilarity = app1.calJacSimilarityByMem(app2);
                            if(tmpSimilarity > similarity){
                                mergeQueue.add(j);
                                merged[j] = true;
                            }
                        }
                    }
                }
                App app = new App(0 , mergedAppName.toString(),mergedAppCount,mergedSrvBitMap,null,mergedSrvBitMap.calMem());
                app.updateListByBitMap();
                GlobalContain.APP_MERGE_NAME_MAP.put(app.name , app);
                GlobalContain.APP_LIST_MERGE.add(app);
            }
        }
        for(int i = end; i < apps.size(); ++i){
            try {
                App app = apps.get(i).clone();
                GlobalContain.APP_LIST_MERGE.add(app );
                GlobalContain.APP_MERGE_NAME_MAP.put(app.name , app);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        GlobalContain.APP_SUM_MERGE = GlobalContain.APP_LIST_MERGE.size();
    }
    /**
     * 尝试将apps按similarity相似度合并
     * 合并条件为 共同内存 / 后app内存 > similarity 并且合并后的内存小于memUpper 连接数小于conUpper
     * 合并后app名字以:分割
     */
    private void mergeAppByCoverSimilarity(List<App> appsSrc , double similarity , long memUpper , long conUpper  ){
        List<App> apps = new ArrayList<>();
        for(App app : appsSrc){
            try {
                apps.add(app.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(apps);
        boolean[] merged = new boolean[apps.size()]; // merged为true  表示已经合并到其他app了
        App app1 , app2;
        for(int i = apps.size() - 1; i >= 0; --i){
            app1 = apps.get(i);
            for(int j = 0; j < apps.size()  ; ++j){
                if(merged[j] || i == j){
                    continue;
                }
                app2 = apps.get(j);
                long same = app1.calSameMem(app2);
                if((double) same / app1.srvMem > similarity && app1.srvMem + app2.srvMem - same < memUpper && app1.count + app2.count < conUpper){
                    // 把app1合到app2
                    app2.name = app2.name + ":" + app1.name + ":";
                    app2.count += app1.count;
                    app2.srvMem = app1.srvMem + app2.srvMem - same;
                    app2.srvBitMap.addAllService(app1.srvBitMap);
                    merged[i] = true;
                    break;
                }
            }
        }

        for(int i = 0; i < apps.size();++i){
            if(!merged[i]){
                GlobalContain.APP_LIST_MERGE_II.add(apps.get(i));
            }
        }
        for(App app : GlobalContain.APP_LIST_MERGE_II){
            app.updateListByBitMap();
            GlobalContain.APP_MERGE_NAME_II_MAP.put(app.name , app);
        }
        GlobalContain.APP_SUM_MERGE_II = GlobalContain.APP_LIST_MERGE_II.size();

    }

    /**
     * 拆开pilots列表中一次合并的app
     */
    public void formatPilotList(List<Pilot> pilots){
        for(Pilot pilot : pilots){
            for(App app : pilot.appList){
                if(app.name.contains(":")){
                    log.error("{}",app.name);
                    throw new RuntimeException("第二次合并的还没有拆...");
                }
            }
        }
        for(Pilot pilot : pilots){
            List<App> splitApp = new ArrayList<>();
            for(App app : pilot.appList){
                if(app.name.contains(";")){
                    for(String appName : app.name.split(";")){
                        if(appName.length() > 0){
                            splitApp.add(GlobalContain.APP_NAME_MAP.get(appName));
                        }
                    }
                }else {
                    splitApp.add(GlobalContain.APP_NAME_MAP.get(app.name));
                }
            }
            pilot.appList = splitApp;
        }
    }
    /**
     * 拆开pilots列表中二次合并的app
     */
    public void formatPilotListII(List<Pilot> pilots){
        for(Pilot pilot : pilots){
            List<App> splitApp = new ArrayList<>();
            for(App app : pilot.appList){
                if(app.name.contains(":")){
                    for(String appName : app.name.split(":")){
                        if(appName.length() > 0){
                            splitApp.add(GlobalContain.APP_MERGE_NAME_MAP.get(appName));
                        }
                    }
                }else {
                    splitApp.add(GlobalContain.APP_MERGE_NAME_MAP.get(app.name));
                }
            }
            pilot.appList = splitApp;
        }
    }

    public Map<String,List<String>> buildResultMapWithFormat(List<Pilot> pilots){
        Map<String,List<String>> result = new HashMap<>();
        for(Pilot pilot : GlobalContain.PILOT_LIST){
            List<String> appNames = new ArrayList<>();
            for(App app : pilot.appList){
                if(!app.name.contains(";")){
                    appNames.add(app.name);
                }else{
                    for(String appName : app.name.split(";")){
                        if(appName.length() > 0){
                            appNames.add(appName);
                        }
                    }
                }
            }
            result.put(pilot.name,appNames);
        }
        return result;
    }

    public  Map<String,List<String>> buildResultMap(List<Pilot> pilots){
        Map<String,List<String>> result = new HashMap<>();
        for(Pilot pilot : pilots){
            List<String> appNames = new ArrayList<>();
            for(App app : pilot.appList){
                appNames.add(app.name);
            }
            result.put(pilot.name,appNames);
        }
        return result;
    }

    public double[] calScore(List<Pilot> pilots){
        long mems[] = new long[pilots.size()];
        long cons[] = new long[pilots.size()];
        Set<Service> has = new HashSet<>();
        int i = 0;
        for(Pilot pilot : pilots){
            has.clear();
            for(App app : pilot.appList){
                cons[i] += app.count;
                for(Service service : app.srvList){
                    if(has.add(service)){
                        mems[i] += service.count;
                    }
                }
            }
            i++;
        }
        double stdCon = NumberUtil.calStd(cons);
        double[] d = NumberUtil.calStdAndSum(mems);
        double stdMem = d[0];
        double sumMem = d[1];
        double[] result = {(double)sumMem/ GlobalContain.SERVICE_MEM , stdCon , stdMem * 0.01 ,(double)sumMem/ GlobalContain.SERVICE_MEM * (stdCon + stdMem * 0.01) };
        return result;
    }


    public void printAppListFeature(List<App> apps){
        log.info("*------------apps分析-------------");
        // app数量
        int size = apps.size();
        log.info("* app数量:{}",size);
        Collections.sort(apps,(a1,a2)->{
            return (int) (a2.srvMem - a1.srvMem);
        });
        double totalMem = 0; // 理论最小值
        double realMem = 0; // 实际加载内存
        long con = 0; // 连接数
        for(Service service : GlobalContain.SERVICE_LIST){
            totalMem += service.count * 0.01;
        }
        for(App app : apps){
            con += app.count;
            realMem += app.srvMem * 0.01;
        }
        log.info("* 理论最小加载内存{},实际加载内存{},比例{}",totalMem , realMem , realMem / totalMem);
        log.info("* 加载内存分布(服务数/内存) : 0%(app0)={}/{} , 5%(app{})={}/{} , " +
                        "10%(app{})={}/{} , 20%(app{})={}/{} , " +
                        "50%(app{})={}/{} , 80%(app{})={}/{} , 100%(app{})={}/{}" ,
                apps.get(0).srvBitMap.calSrvCnt() , apps.get(0).srvMem * 0.01 ,
                (int) (size * 0.05),apps.get((int) (size * 0.05)).srvBitMap.calSrvCnt() , apps.get((int) (size * 0.05)).srvMem  * 0.01 ,
                (int) (size * 0.1),apps.get((int) (size * 0.1)).srvBitMap.calSrvCnt(), apps.get((int) (size * 0.1)).srvMem  * 0.01 ,
                (int) (size * 0.2),apps.get((int) (size * 0.2)).srvBitMap.calSrvCnt() , apps.get((int) (size * 0.2)).srvMem  * 0.01 ,
                (int) (size * 0.5),apps.get((int) (size * 0.5)).srvBitMap.calSrvCnt() , apps.get((int) (size * 0.5)).srvMem  * 0.01 ,
                (int) (size * 0.8),apps.get((int) (size * 0.8)).srvBitMap.calSrvCnt() , apps.get((int) (size * 0.8)).srvMem  * 0.01 ,
                size - 1,apps.get(size - 1).srvBitMap.calSrvCnt() , apps.get(size - 1).srvMem  * 0.01 );
        double avg = realMem / size;
        log.info("* 实际加载内存平均数 : {}/{}" , totalMem / size , avg);
        int idx = 0;
        for(App app : apps){
            if(app.srvMem * 0.01 < avg){
                break;
            }
            idx++;
        }
        log.info("* 实际加载内存平均数位置 : app{}" , idx);
        // 连接数情况
        Collections.sort(apps,(a1,a2)->{
            return (int) (a2.count - a1.count);
        });
        log.info("* 全部连接数:{}",con);
        log.info("* 连接数情况:  0%={} , 5%={} , 10%={} , 20%={} , 50%={} , 80%={} , 100%={}" ,
                apps.get(0).count ,
                apps.get((int) (size * 0.05)).count ,
                apps.get((int) (size * 0.1)).count ,
                apps.get((int) (size * 0.2)).count ,
                apps.get((int) (size * 0.5)).count ,
                apps.get((int) (size * 0.8)).count,
                apps.get(size - 1).count );
        log.info("*------------apps分析结束----------\n\n");
    }

}
