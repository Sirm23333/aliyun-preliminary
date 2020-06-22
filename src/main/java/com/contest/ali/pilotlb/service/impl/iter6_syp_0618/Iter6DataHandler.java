package com.contest.ali.pilotlb.service.impl.iter6_syp_0618;
import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.constant.GlobalConstant;
import com.contest.ali.pilotlb.service.DataHandler;
import com.contest.ali.pilotlb.service.impl.iter6_syp_0618.model.App;
import com.contest.ali.pilotlb.service.impl.iter6_syp_0618.model.Pilot;
import com.contest.ali.pilotlb.service.impl.iter6_syp_0618.model.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
public class Iter6DataHandler implements DataHandler {

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


    @Override
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
     * 读数据 并构建数据结构
     */
    public void initData(List<String> pilotNames , String dataPath ){
        GlobalContain.PILOT_LIST = new ArrayList<>();
        GlobalContain.PILOT_SUM = 0;
        GlobalContain.APP_LIST_SRC = new ArrayList<>();
        GlobalContain.APP_SUM_SRC = 0;
        GlobalContain.APP_LIST_MERGE = new ArrayList<>();
        GlobalContain.APP_SUM_MERGE = 0;
        GlobalContain.CON_SUM = 0;
        GlobalContain.SERVICE_NAME_MAP = new HashMap<>();
        GlobalContain.SERVICE_LIST = new ArrayList<>();
        GlobalContain.SERVICE_SUM = 0;
        GlobalContain.SERVICE_MEM = 0;

        GlobalContain.PILOT_SUM = pilotNames.size();
        for(String pilotName : pilotNames){
            GlobalContain.PILOT_LIST.add(new Pilot(GlobalContain.PILOT_LIST.size(), pilotName , new ArrayList<>()));
        }
        JSONObject data = dataReader(dataPath);
        JSONObject apps = data.getJSONObject(GlobalConstant.KEY_APPS);
        JSONObject dependencies = data.getJSONObject(GlobalConstant.KEY_DEPENDENCIES);
        JSONObject appDependencies ; // 某一个app的依赖,做遍历时临时变量用
        Set<String> serviceNames ; // 某一个app的依赖集合,做遍历时临时变量用
        Set<String> appNames = apps.keySet();
        List<List<Integer>> appDepend = new ArrayList<>(); // 临时记录每个app的依赖服务
        int tmpCon ;
        for(String appName : appNames){
            List<Integer> depend = new ArrayList<>();
            tmpCon = apps.getInteger(appName);
            GlobalContain.CON_SUM += tmpCon;
            App app = new App( appName , apps.getInteger(appName)  , null ,  0 );
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
                depend.add(service.id);
            }
            GlobalContain.APP_LIST_SRC.add(app);
            appDepend.add(depend);
        }
        GlobalContain.SERVICE_SUM = GlobalContain.SERVICE_NAME_MAP.size();
        GlobalContain.APP_SUM_SRC = GlobalContain.APP_LIST_SRC.size();
        // 构建app依赖bitmap
        for(int i = 0; i < GlobalContain.APP_SUM_SRC; ++i){
            App app = GlobalContain.APP_LIST_SRC.get(i);
            app.srvDepend = new long[GlobalContain.SERVICE_SUM / BitMapUtil.LONG_BYTE + 1];
            for(int id : appDepend.get(i)){
                BitMapUtil.set(app.srvDepend , id );
            }
        }
        // 一次合并
        GlobalContain.APP_LIST_MERGE = mergeAppByJacSimilarity(GlobalContain.APP_LIST_SRC,jacSimilarity);
        printAppListFeature( GlobalContain.APP_LIST_MERGE);

        // 二次合并
        for(int i = 0; i < secondMergeCnt; ++i){
            GlobalContain.APP_LIST_MERGE = mergeAppByCoverSimilarity(GlobalContain.APP_LIST_MERGE,coverSimilarity,mergeIdxRatio1,mergeIdxRatio2,
                    GlobalContain.SERVICE_MEM / GlobalContain.PILOT_SUM * secondMergeMemUpper,
                    GlobalContain.CON_SUM / GlobalContain.PILOT_SUM );
            printAppListFeature( GlobalContain.APP_LIST_MERGE);
        }

        GlobalContain.APP_SUM_MERGE = GlobalContain.APP_LIST_MERGE.size();
    }


    /**
     * 计算app1与app2的服务维度的相似度
     *
     * */
    private double calCoverSimilarityByMem(App app1 , App app2){
        long same = BitMapUtil.calMem(BitMapUtil.and(app1.srvDepend,app2.srvDepend));
        return (double) same / app1.srvMem ;
    }
    /**
     * 计算app1与app2的内存维度的相似度
     */
    private double calJacSimilarityByMem(App app1 , App app2){
        long same = BitMapUtil.calMem(BitMapUtil.and(app1.srvDepend,app2.srvDepend));
        return (double) same / (app1.srvMem + app2.srvMem - same);
    }

    public List<App> mergeAppByJacSimilarity(List<App> apps , double similarity) {
        List<App> mergedAppList = new ArrayList<>();
        int length = apps.size();
        // 记录app是否已经合并
        boolean[] merged = new boolean[length];
        Queue<Integer> mergeQueue = new ArrayDeque<>();
        App app1 , app2;
        double tmpSimilarity;
        for(int i = 0; i < length; ++i){
            if(!merged[i]){
                StringBuffer mergedAppName = new StringBuffer();
                int mergedAppCount = 0;
                long mergedAppSrvMem = 0;
                long[] mergedSrv = new long[GlobalContain.SERVICE_SUM / BitMapUtil.LONG_BYTE + 1];
                mergeQueue.add(i);
                merged[i] = true;
                while(!mergeQueue.isEmpty()){
                    int tmp = mergeQueue.poll();
                    app1 = apps.get(tmp);
                    mergedAppName.append(app1.name+";");
                    mergedAppCount += app1.count;
                    mergedSrv = BitMapUtil.or(mergedSrv , app1.srvDepend);
                    for(int j = i + 1; j < length; ++j){
                        app2 = apps.get(j);
                        long maxK = Math.max(app1.srvMem,app2.srvMem);
                        if(!merged[j] && Math.abs(app1.srvMem - app2.srvMem) < (1 - similarity) * maxK){
                            tmpSimilarity = calJacSimilarityByMem(app1,app2);
                            if(tmpSimilarity > similarity){
                                mergeQueue.add(j);
                                merged[j] = true;
                            }
                        }
                    }
                }
                App app = new App(mergedAppName.toString(),mergedAppCount,mergedSrv,BitMapUtil.calMem(mergedSrv) );
                mergedAppList.add(app);
            }
        }
        return  mergedAppList;
    }

    /**
     * 尝试将apps的后idxRadio的app合并到前idxRadio的app中,
     * 合并条件为 共同内存 / 后app内存 > similarity 并且合并后的内存小于memUpper 连接数小于conUpper
     * 尝试合并cnt次
     */
    public List<App> mergeAppByCoverSimilarity(List<App> apps , double similarity , double idxRadio1 , double idxRadio2 , long memUpper , long conUpper  ){
            Collections.sort(apps , (a1,a2)->{
                return (int) (a2.srvMem - a1.srvMem);
            });
            boolean[] merged = new boolean[apps.size()]; // merged为true  表示已经合并到其他app了
            List<App> mergedAppList = new ArrayList<>();
            int midIdx1 = (int) (apps.size() * idxRadio1);
            int midIdx2 = (int) (apps.size() * idxRadio2);
            for(int i = 0; i < midIdx1; i++){
                mergedAppList.add(apps.get(i));
            }
            App app1 , app2;
            // 第一遍 由多到少
            for(int i = midIdx1; i < midIdx2; ++i){
                app1 = apps.get(i);
                for(int j = midIdx1 - 1; j >= 0 ; --j){
                    app2 = mergedAppList.get(j);
                    double sim = calCoverSimilarityByMem(app1,app2);
                    if(sim > similarity && app1.srvMem + app2.srvMem - app1.srvMem * sim < memUpper && app1.count + app2.count < conUpper){
                        // 把app1合到app2
                        app2.name = app2.name + ";" + app1.name;
                        app2.count += app1.count;
                        app2.srvMem = Math.round(app1.srvMem + app2.srvMem - app1.srvMem * sim);
                        app2.srvDepend = BitMapUtil.or(app1.srvDepend,app2.srvDepend);
                        merged[i] = true;
                        break;
                    }
                }
            }
            // 第二遍 由少到多捞一下
            Collections.sort(mergedAppList , (a1,a2)->{
                return (int) (a2.srvMem - a1.srvMem);
            });
            for(int i = midIdx2 - 1; i >= midIdx1; --i){
                if(merged[i])
                    continue;
                app1 = apps.get(i);
                for(int j = midIdx1 - 1; j >= 0 ; --j){
                    app2 = mergedAppList.get(j);
                    double sim = calCoverSimilarityByMem(app1,app2);
                    if(sim > similarity && app1.srvMem + app2.srvMem - app1.srvMem * sim < memUpper && app1.count + app2.count < conUpper){
                        // 把app1合到app2
                        app2.name = app2.name + ";" + app1.name;
                        app2.count += app1.count;
                        app2.srvMem = Math.round(app1.srvMem + app2.srvMem - app1.srvMem * sim);
                        app2.srvDepend = BitMapUtil.or(app1.srvDepend,app2.srvDepend);
                        merged[i] = true;
                        break;
                    }
                }
            }

            for(int i = midIdx1; i < merged.length;++i){
                if(!merged[i]){
                    mergedAppList.add(apps.get(i));
                }
            }
            return mergedAppList;
    }
    public  Map<String,List<String>> buildResultMap(){
        Map<String,List<String>> result = new HashMap<>();
        for(Pilot pilot : GlobalContain.PILOT_LIST){
            List<String> appNames = new ArrayList<>();
            for(App app : pilot.apps){
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
                BitMapUtil.count1Num(apps.get(0).srvDepend) , apps.get(0).srvMem * 0.01 ,
                (int) (size * 0.05),BitMapUtil.count1Num(apps.get((int) (size * 0.05)).srvDepend) , apps.get((int) (size * 0.05)).srvMem  * 0.01 ,
                (int) (size * 0.1),BitMapUtil.count1Num(apps.get((int) (size * 0.1)).srvDepend), apps.get((int) (size * 0.1)).srvMem  * 0.01 ,
                (int) (size * 0.2),BitMapUtil.count1Num(apps.get((int) (size * 0.2)).srvDepend) , apps.get((int) (size * 0.2)).srvMem  * 0.01 ,
                (int) (size * 0.5),BitMapUtil.count1Num(apps.get((int) (size * 0.5)).srvDepend) , apps.get((int) (size * 0.5)).srvMem  * 0.01 ,
                (int) (size * 0.8),BitMapUtil.count1Num(apps.get((int) (size * 0.8)).srvDepend) , apps.get((int) (size * 0.8)).srvMem  * 0.01 ,
                size - 1,BitMapUtil.count1Num(apps.get(size - 1).srvDepend) , apps.get(size - 1).srvMem  * 0.01 );
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
