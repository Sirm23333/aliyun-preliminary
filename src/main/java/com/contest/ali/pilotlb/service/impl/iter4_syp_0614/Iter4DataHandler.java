package com.contest.ali.pilotlb.service.impl.iter4_syp_0614;
import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.constant.GlobalConstant;
import com.contest.ali.pilotlb.service.DataHandler;
import com.contest.ali.pilotlb.service.impl.iter4_syp_0614.model.App;
import com.contest.ali.pilotlb.service.impl.iter4_syp_0614.model.Pilot;
import com.contest.ali.pilotlb.service.impl.iter4_syp_0614.model.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
public class Iter4DataHandler implements DataHandler {

    // 将app相似度超过similarity的合并
    private double similarity = 0.9;
    // 在选择service全集时,app依赖的service超过choiceRate的service是新的时,则选择这个app
    private double choiceRate = 0.5;



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
    public void initData(List<String> pilotNames , String dataPath , List<App> uni , List<App> rest){
        GlobalContain.PILOT_SUM = pilotNames.size();
        for(String pilotName : pilotNames){
            GlobalContain.PILOT_LIST.add(new Pilot(GlobalContain.PILOT_LIST.size(), pilotName , new ArrayList<>() , new HashSet<>()));
        }
        JSONObject data = dataReader(dataPath);
        JSONObject apps = data.getJSONObject(GlobalConstant.KEY_APPS);
        JSONObject dependencies = data.getJSONObject(GlobalConstant.KEY_DEPENDENCIES);
        JSONObject appDependencies ; // 某一个app的依赖,做遍历时临时变量用
        Set<String> serviceNames ; // 某一个app的依赖集合,做遍历时临时变量用
        Set<String> appNames = apps.keySet();
        for(String appName : appNames){
            App app = new App( appName , apps.getInteger(appName) , new HashSet<>() , 0);
            appDependencies = dependencies.getJSONObject(appName);
            serviceNames = appDependencies.keySet();
            for(String serviceName : serviceNames){
                Service service = GlobalContain.SERVICE_NAME_MAP.get(serviceName);
                if(service == null){
                    service = new Service(GlobalContain.SERVICE_NAME_MAP.size() , serviceName , appDependencies.getInteger(serviceName));
                    GlobalContain.SERVICE_NAME_MAP.put(serviceName , service);
                    GlobalContain.SERVICE_LIST.add(service);
                }
                app.services.add(service);
                app.srvMem += service.count;
            }
            GlobalContain.APP_LIST.add(app);
        }
        GlobalContain.APP_SUM = GlobalContain.APP_LIST.size();
        GlobalContain.SERVICE_SUM = GlobalContain.SERVICE_NAME_MAP.size();
        GlobalContain.APP_LIST = mergeAppBySimilarity(GlobalContain.APP_LIST,similarity);
        log.info("合并后app数量:{}",GlobalContain.APP_LIST.size());
//        getUniverse(uni,rest,choiceRate);
//        for(App app : uni){
//            log.info("uni : {} , {}", app.services.size() , app.srvMen);
//        }
//        for(App app : rest){
//            log.info("rest : {} , {}", app.services.size() , app.srvMen);
//        }
    }

    /**
     * 从所有的App中找可以覆盖  service的App集合
     * @param uni 覆盖所有service的app
     * @param rest 剩下的app
     */
    public void getUniverse(List<App> uni , List<App> rest , double rate){
        // 将App按依赖服务数量升序排序
        Collections.sort(GlobalContain.APP_LIST);
        // service被依赖次数
        int[] serviceDependedSum = new int[GlobalContain.SERVICE_SUM];
        int cnt;
        for(App app : GlobalContain.APP_LIST){
            cnt = 0;
            for(Service service : app.services){
                if(serviceDependedSum[service.id] == 0){
                    cnt++;
                }
            }
            if((double) cnt / app.services.size() > rate){
                uni.add(app);
                for(Service service : app.services){
                    serviceDependedSum[service.id]++;
                }
            }else {
                rest.add(app);
            }
        }
        log.info("uni size = {} , rest size = {}" , uni.size() , rest.size());
        long uniMem = 0;
        Set<Service> has = new HashSet<>();
        for (App app : uni){
            has.addAll(app.services);
            for(Service service : app.services){
                uniMem += service.count;
            }
        }
        long allMem = 0;
        for(Service service : has){
            allMem += service.count;
        }
        log.info("uniCnt = {} , allCnt = {} , uniCnt/allCnt = {}" , has.size() , GlobalContain.SERVICE_SUM , (double)  has.size() / GlobalContain.SERVICE_SUM);
        log.info("uniMem = {} , allMem = {} , uniMem/allMem = {}" , uniMem , allMem , (double) uniMem / allMem);
//        log.info("{}",serviceDependedSum);
        List<Pilot> pilots = new ArrayList<>();
        for(int i = 0; i < 5; ++i){
            pilots.add(new Pilot(i,"pilot"+i,new ArrayList<>() , new HashSet<>()));
        }
    }



    public void printSim(List<App> apps){
        double[][] sim = new double[apps.size()][apps.size()];
        for(int i = 0 ; i < apps.size();i++){
            for(int j = 0; j < apps.size(); j++){
                App a1 = apps.get(i);
                App a2 = apps.get(j);
                sim[i][j] = calSimilarityBySrv(a1,a2);
            }
        }
        int i=0,j=0;
        for(double[] ss : sim){
            j=0;
            for(double s : ss){
                System.out.print(String.format("%.2f",s)+" ");
                j++;
            }
            System.out.println(apps.get(i).name+" "+apps.get(i).services.size());
            i++;
        }
    }

    /**
     * 计算app1与app2的服务维度的相似度 , app1规模小于app2时效率高
     */
    private double calSimilarityBySrv(App app1 , App app2){
        long same = 0;
        for(Service service : app1.services){
            if(app2.services.contains(service)){
                same++;
            }
        }
        return (double) same / (app1.services.size() + app2.services.size() - same);
    }
    /**
     * 计算app1与app2的内存维度的相似度 , app1规模小于app2时效率高
     */
    private double calSimilarityByMem(App app1 , App app2){
        long same = 0;
        for(Service service : app1.services){
            if(app2.services.contains(service)){
                same += service.count;
            }
        }
        return (double) same / (app1.srvMem + app2.srvMem - same);
    }

    public List<App> mergeAppBySimilarity(List<App> apps , double similarity) {
        List<App> mergedAppList = new ArrayList<>();
        int length = apps.size();
        // 记录app是否已经合并
        boolean[] merged = new boolean[length];
        Queue<Integer> mergeQueue = new ArrayDeque<>();
        App app1 , app2;
        double tmpSimilarity;
        int same; // 统计某一个比较中的重复的service的数量
        for(int i = 0; i < length; ++i){
            if(!merged[i]){
                StringBuffer mergedAppName = new StringBuffer();
                int mergedAppCount = 0;
                long mergedAppSrvMem = 0;
                Set<Service> mergedAppService = new HashSet<>();
                mergeQueue.add(i);
                merged[i] = true;
                while(!mergeQueue.isEmpty()){
                    int tmp = mergeQueue.poll();
                    app1 = apps.get(tmp);
                    mergedAppName.append(app1.name+";");
                    mergedAppCount += app1.count;
                    mergedAppService.addAll(app1.services);
                    for(int j = i + 1; j < length; ++j){
                        app2 = apps.get(j);
                        long k1 = app1.services.size();
                        long k2 = app2.services.size();
                        long maxK = Math.max(k1,k2);
                        if(!merged[j] && Math.abs(k1 - k2) < (1 - similarity) * maxK){
                            tmpSimilarity = calSimilarityBySrv(k1 > k2 ? app2 : app1 , k1 > k2 ? app1 : app2 );
                            if(tmpSimilarity > similarity){
                                mergeQueue.add(j);
                                merged[j] = true;
                            }
                        }
                    }
                }
                for(Service service : mergedAppService){
                    mergedAppSrvMem += service.count;
                }
                App app = new App(mergedAppName.toString(),mergedAppCount,mergedAppService,mergedAppSrvMem);
                mergedAppList.add(app);
            }
        }
        return  mergedAppList;
    }
}
