package com.contest.ali.pilotlb.service.impl.iter8_syp_0622;
import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.constant.GlobalConstant;
import com.contest.ali.pilotlb.service.DataHandler;
import com.contest.ali.pilotlb.service.impl.iter8_syp_0622.model.*;
import com.contest.ali.pilotlb.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
public class Iter8DataHandler implements DataHandler {

    // 第一次合并的相似度下限,将app相似度超过similarity的合并
    private double jacSimilarity = GlobalParam.JAC_MERGE_SIMILARITY;
    // 第二次合并的相似度下限
    private double coverSimilarity = GlobalParam.COV_MERGE_SIMILARITY;
    // 第二次合并, 如mergeIdxRatio[0] = 0.8  则尝试将后20%的app向前80%的app合并 , 这个值越大,合并之后app数量越多
    private double mergeIdxRatio1 = GlobalParam.COV_MERGE_RATIO_1;
    private double mergeIdxRatio2 = GlobalParam.COV_MERGE_RATIO_2;
    // 第二次合并的次数,
    private int secondMergeCnt = GlobalParam.COV_MERGE_CNT;
    private int secondMergeMemUpper = GlobalParam.COV_MERGE_MEM_UPPER_MUL;


    public JSONObject dataReader(String path) {
        String input = null;
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
//        GlobalContain.APP_LIST_MERGE = mergeAppByJacSimilarityRange(GlobalContain.APP_LIST_SRC,0,0.2,0.99);
//        GlobalContain.APP_LIST_MERGE = mergeAppByJacSimilarity(GlobalContain.APP_LIST_SRC,jacSimilarity);
//        GlobalContain.APP_SUM_MERGE = GlobalContain.APP_LIST_MERGE.size();
//        printAppListFeature( GlobalContain.APP_LIST_MERGE);
//
//        // 二次合并
//        for(int i = 0; i < secondMergeCnt; ++i){
//            GlobalContain.APP_LIST_MERGE = mergeAppByCoverSimilarity(GlobalContain.APP_LIST_MERGE,coverSimilarity,mergeIdxRatio1,mergeIdxRatio2,
//                    GlobalContain.SERVICE_MEM / GlobalContain.PILOT_SUM * secondMergeMemUpper,
//                    GlobalContain.CON_SUM / GlobalContain.PILOT_SUM );
//            printAppListFeature( GlobalContain.APP_LIST_MERGE);
//        }
//
//        GlobalContain.APP_SUM_MERGE = GlobalContain.APP_LIST_MERGE.size();
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
        // 更新App列表
//        for(int i = 0; i < GlobalContain.APP_SUM_SRC_PRE; i++){
//            GlobalContain.APP_LIST_SRC.get(i).srvBitMap.resetSrvList(GlobalContain.SERVICE_LIST);
//        }

    }

    /**
     * 对给定的app按照similarity合并,返回一个新的app列表
     */
    private List<App> mergeAppByJacSimilarity(List<App> apps , double similarity) {
        return mergeAppByJacSimilarityRange(apps,0,1,similarity);
    }

    /**
     * 将apps的 radio1到radio2范围内的按照相似度similarity合并
     */
    private List<App> mergeAppByJacSimilarityRange(List<App> apps , double radio1 , double radio2 , double similarity)  {
        Collections.sort(apps);
        List<App> mergedAppList = new ArrayList<>();
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
                mergedAppList.add( apps.get(i).clone());
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
                App app = new App(mergedAppList.size() , mergedAppName.toString(),mergedAppCount,mergedSrvBitMap,null,mergedSrvBitMap.calMem());
                app.updateListByBitMap();
                mergedAppList.add(app);
            }
        }
        for(int i = end; i < apps.size(); ++i){
            try {
                mergedAppList.add( apps.get(i).clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return  mergedAppList;
    }
    /**
     * 尝试将apps的后idxRadio的app合并到前idxRadio的app中,
     * 合并条件为 共同内存 / 后app内存 > similarity 并且合并后的内存小于memUpper 连接数小于conUpper
     * 尝试合并cnt次
     */
    private List<App> mergeAppByCoverSimilarity(List<App> apps , double similarity , double idxRadio1 , double idxRadio2 , long memUpper , long conUpper  ){
            Collections.sort(apps);
            boolean[] merged = new boolean[apps.size()]; // merged为true  表示已经合并到其他app了
            List<App> mergedAppList = new ArrayList<>();
            int midIdx1 = (int) (apps.size() * idxRadio1);
            int midIdx2 = (int) (apps.size() * idxRadio2);
            for(int i = 0; i < midIdx1; i++){
                apps.get(i).id = i;
                mergedAppList.add(apps.get(i));
            }
            App app1 , app2;
            // 第一遍 由多到少
            for(int i = midIdx1; i < midIdx2; ++i){
                app1 = apps.get(i);
                for(int j = midIdx1 - 1; j >= 0 ; --j){
                    app2 = mergedAppList.get(j);
                    long same = app1.calSameMem(app2);
                    if((double) same / app1.srvMem > similarity && app1.srvMem + app2.srvMem - same < memUpper && app1.count + app2.count < conUpper){
                        // 把app1合到app2
                        app2.name = app2.name + ";" + app1.name;
                        app2.count += app1.count;
                        app2.srvMem = app1.srvMem + app2.srvMem - same;
                        app2.srvBitMap.addAllService(app1.srvBitMap);
                        merged[i] = true;
                        break;
                    }
                }
            }
            // 第二遍 由少到多捞一下
            Collections.sort(mergedAppList);
            for(int i = midIdx2 - 1; i >= midIdx1; --i){
                if(merged[i])
                    continue;
                app1 = apps.get(i);
                for(int j = midIdx1 - 1; j >= 0 ; --j){
                    app2 = mergedAppList.get(j);
                    long same = app1.calSameMem(app2);
                    if((double) same / app1.srvMem  > similarity && app1.srvMem + app2.srvMem - same < memUpper && app1.count + app2.count < conUpper){
                        // 把app1合到app2
                        app2.name = app2.name + ";" + app1.name;
                        app2.count += app1.count;
                        app2.srvMem = app1.srvMem + app2.srvMem - same;
                        app2.srvBitMap.addAllService(app1.srvBitMap);
                        merged[i] = true;
                        break;
                    }
                }
            }

            for(int i = midIdx1; i < merged.length;++i){
                if(!merged[i]){
                    apps.get(i).id = mergedAppList.size();
                    mergedAppList.add(apps.get(i));
                }
            }
            for(App app : mergedAppList){
                app.updateListByBitMap();
            }
            return mergedAppList;
    }

    /**
     * 拆开pilots列表中合并的app
     */
    public void formatPilotList(List<Pilot> pilots){
        for(Pilot pilot : pilots){
            List<App> toDel = new ArrayList<>(), toAdd = new ArrayList<>();
            for(App app : pilot.appList){
                if(app.name.contains(";")){
                    toDel.add(app);
                    for(String appName : app.name.split(";")){
                        if(appName.length() > 0){
                            toAdd.add(GlobalContain.APP_NAME_MAP.get(appName));
                        }
                    }
                }
            }
            pilot.appList.removeAll(toDel);
            pilot.appList.addAll(toAdd);
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
