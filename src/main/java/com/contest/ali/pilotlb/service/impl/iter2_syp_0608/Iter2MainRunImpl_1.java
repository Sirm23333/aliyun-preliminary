package com.contest.ali.pilotlb.service.impl.iter2_syp_0608;

import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.constant.GlobalConstant;
import com.contest.ali.pilotlb.constant.ObjectFactory;
import com.contest.ali.pilotlb.service.MainRun;
import com.contest.ali.pilotlb.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.List;

@Slf4j
public class Iter2MainRunImpl_1 implements MainRun {

    private int M = 100; // 种群规模
    private int M_COUNT = 5; // 分M_COUNT次生成第一代
    public static Map<String, List<String>> result;
    @Override
    public Map<String, List<String>> stage1Run(List<String> pilotNames, String dataPath) {
        long start = System.currentTimeMillis();
        initContain(pilotNames,dataPath);
        Collections.sort(GlobalContain.APP_DEPEND_LIST,(a1,a2)->{
            return a2.size() - a1.size();
        });
        List<Integer> chromosome = new ArrayList<>();
        for(int i = 0; i < GlobalContain.APP_SUM; ++i){
            chromosome.add(i % GlobalContain.PILOT_SUM);
        }
        result = getResultByX(chromosome);
        result.put(GlobalContain.APP_DEPEND_LIST.get(0).size()+","+
                GlobalContain.APP_DEPEND_LIST.get(10).size()+","+
                GlobalContain.APP_DEPEND_LIST.get(20).size()+","+
                GlobalContain.APP_DEPEND_LIST.get(50).size()+","+
                GlobalContain.APP_DEPEND_LIST.get(70).size(),new ArrayList<>());
        return result;
    }

    @Override
    public Map<String, List<String>> stage2Run(JSONObject data) {
        JSONObject apps = data.getJSONObject(GlobalConstant.KEY_APPS);
        Set<String> keys = apps.keySet();
        int i = 0;
        for(String key : keys){
            result.get(GlobalContain.PILOT_NAME_LIST.get(i % GlobalContain.PILOT_SUM)).add(key);
            i++;
        }

        return result;
    }
    /**
     * @author: sirm
     * @description: 阶段一初始化各个数据结构
     * @date: 2020/6/6
     * @return
     */
    protected void initContain(List<String> pilotNames, String dataPath){

        // 初始化pilot相关的数据结构
        for(String pilotName : pilotNames){
            GlobalContain.PILOT_MAP.put(pilotName , GlobalContain.PILOT_MAP.size());
            GlobalContain.PILOT_NAME_LIST.add(pilotName);
            GlobalContain.PILOT_CONNECTION_LIST.add(new ArrayList<>());
        }
        JSONObject data = ObjectFactory.dataHandler.dataReader(dataPath);
        JSONObject dependJson = data.getJSONObject(GlobalConstant.KEY_DEPENDENCIES);
        JSONObject countJson = data.getJSONObject(GlobalConstant.KEY_APPS);

        // 初始化app和service相关的数据结构
        Set<String> appNames = countJson.keySet();// 所有的app-name
        JSONObject appDependJson = null;
        Set<String> serviceNames = null;
        GlobalContain.APP_SUM = appNames.size();
        GlobalContain.PILOT_SUM = pilotNames.size();
        for(String appName : appNames){
            GlobalContain.APP_MAP.put(appName , GlobalContain.APP_MAP.size());
            GlobalContain.APP_NAME_LIST.add(appName);
            GlobalContain.APP_COUNT_LIST.add(countJson.getIntValue(appName));
            // service依赖集合
            Set<Integer> dependSet = new HashSet<>();
            appDependJson = dependJson.getJSONObject(appName);
            serviceNames = appDependJson.keySet();
            for(String serviceName : serviceNames){
                if(!GlobalContain.SERVICE_MAP.containsKey(serviceName)){
                    GlobalContain.SERVICE_MAP.put(serviceName , GlobalContain.SERVICE_MAP.size());
                    GlobalContain.SERVICE_NAME_LIST.add(serviceName);
                    GlobalContain.SERVICE_COUNT_LIST.add(appDependJson.getIntValue(serviceName));
                }
                dependSet.add(GlobalContain.SERVICE_MAP.get(serviceName));
            }
            GlobalContain.APP_DEPEND_LIST.add(dependSet);
        }


        log.info("[pilot sum] : {}" , GlobalContain.PILOT_NAME_LIST.size());
        log.info("[app sum] : {}" , GlobalContain.APP_NAME_LIST.size());
        log.info("[service sum] : {}" , GlobalContain.SERVICE_NAME_LIST.size());
    }

    /**
     * @author: sirm
     * @description: 生成第一代种群,count次随机生成M个种群,每次取前M/count个个体放入第一代种群
     * @date: 2020/6/7
     * @return
     */
    List<Individual> initFirGeneration(int count){
        List<Individual> individuals = new ArrayList<>();
        List<Individual> tmp = new ArrayList<>();
        for(int i = 0 ; i < count; ++i){
            // 随机生成M个个体
            for(int j = 0; j < M; ++j){
                List<Integer> chromosome = new ArrayList<>();
                for(int k = 0; k < GlobalContain.APP_SUM; ++k){
                    chromosome.add(NumberUtil.getRandomNum(0,GlobalContain.PILOT_SUM));
                }
                tmp.add(new Individual(chromosome));
            }
            Collections.sort(tmp , (ind1 , ind2)->{
                return ind2.fitness - ind1.fitness > 0 ? 1 : -1;
            });
            for(int j = 0; j < M / count; ++j){
                individuals.add(tmp.get(j));
            }
        }
        log.info("generation1 sum : {}",individuals.size());
        for(Individual individual : individuals){
            log.info("generation1 fitness : {}" , individual.fitness);
        }
        return individuals;
    }

    /**
     * @author: sirm
     * @description: 由x得到要求的一个分配map
     * @date: 2020/6/6
     * @return
     */
    protected Map<String , List<String>> getResultByX(List<Integer> x){
        Map<String , List<String> > result = new HashMap<>();
        for(int i = 0; i < x.size(); ++i){
            String appName = GlobalContain.APP_NAME_LIST.get(i);
            String pilotName = GlobalContain.PILOT_NAME_LIST.get(x.get(i));
            if(!result.containsKey(pilotName)){
                result.put(pilotName,new ArrayList<>());
            }
            result.get(pilotName).add(appName);
        }
        return result;
    }

    /**
     *  @author sirm
     *  @Date 2020/6/7 下午1:33
     *  @Description 个体类
     */
    class Individual{

        // 染色体
        List<Integer> chromosome;
        // 适应度
        double fitness;

        public Individual(List<Integer> chromosome){
            this.chromosome = chromosome;
            this.fitness = calFitness(chromosome);
        }

        private double calFitness(List<Integer> chromosome){
            double fitness = 0;
            List<Integer> sidecarSum = new ArrayList<>();// 保存每个pilot中连接app数
            List<Set<Integer>> serviceSet = new ArrayList<>(); // 保存每个pilot中加载的服务集合
            for(int i = 0; i < GlobalContain.PILOT_NAME_LIST.size(); ++i){
                sidecarSum.add(0);
                serviceSet.add(new HashSet<>());
            }
            for(int i = 0; i < chromosome.size(); ++i){
                sidecarSum.set(chromosome.get(i),GlobalContain.APP_COUNT_LIST.get(i));
                serviceSet.get(chromosome.get(i)).addAll(GlobalContain.APP_DEPEND_LIST.get(i));
            }
            long conSum = 0; // 连接总数
            long conSum2 = 0; // 连接平方和
            double dConnection = 0; // 连接方差
            long srvSum = 0; // 依赖总数
            long srvSum2 = 0; // 依赖平方和
            double dService = 0; // 依赖方差
            for(int i = 0 ; i < sidecarSum.size(); ++i){
                conSum += sidecarSum.get(i);
                conSum2 += sidecarSum.get(i) * sidecarSum.get(i);
                long srvCnt = 0;
                for(Integer j : serviceSet.get(i)){
                    srvCnt += GlobalContain.SERVICE_COUNT_LIST.get(j);
                }
                srvSum += srvCnt;
                srvSum2 += srvCnt * srvCnt;
            }
            dConnection = ( (double)conSum2 / GlobalContain.PILOT_SUM - Math.pow((double)conSum / GlobalContain.PILOT_SUM , 2));
            dService = ( (double)srvSum2 / GlobalContain.PILOT_SUM - Math.pow((double)srvSum / GlobalContain.PILOT_SUM , 2));
            fitness = conSum * ( dConnection + dService );
            return fitness;

        }

    }
    int cc = 0;
    List<Integer> tmp = new ArrayList<>();
    private void printSameRate(int idx , double rate){
        tmp.clear();
        for(int i = 0; i < idx; ++i){
            tmp.add(0);
        }
        //算两两相似度
        for(int i = 0; i < idx; ++i){
            for(int j = i+1; j < idx; ++j){
                Set<Integer> s1 = GlobalContain.APP_DEPEND_LIST.get(i);
                Set<Integer> s2 = GlobalContain.APP_DEPEND_LIST.get(j);
                int same = 0;
                for(Integer k : s1){
                    if(s2.contains(k)){
                        same++;
                    }
                }
                double sameRate = (double) same / (s1.size() + s2.size() - same);
                if(sameRate > rate && tmp.get(j) == 0){
                    tmp.set(j , i);
                    if(s1.size() < 50){
                        cc++;
                    }
                }
            }
        }
    }
}
