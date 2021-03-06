package com.contest.ali.pilotlb.service.impl.iter8_syp_0622;

import com.contest.ali.pilotlb.service.impl.iter8_syp_0622.model.*;
import com.contest.ali.pilotlb.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
@Slf4j
public class Game{

    private List<App> apps;             // 待分配的所有的app
    private List<Service> services ;    // 所有的服务
    private List<Pilot> pilots ;        // 一种分配情况
    private int[] appsIdx;              // 记录每个app在那个pilot中
    private int[][] appCntEachService;  // 记录每个pilot中依赖每一个服务的app的数量
    private long[] mems;                // 每个pilot的内存
    private long[] cons;                // 每个pilot的连接数
    private boolean flag;               // flag为true时,对pilots已经加载的内存只增不减
    private int tryCnt;                 // 博弈均衡后再重新打乱重试的次数
    private double upsetRatio;          // 将百分比upsetRatio的app打乱

    /**
     *
     * @param apps
     * @param services
     * @param pilots
     * @param flag flag为true时,对pilots已经加载的内存只增不减
     */
    public Game(List<App> apps , List<Service> services , List<Pilot> pilots , boolean flag){
        this.pilots = pilots;
        this.apps = new ArrayList<>();
        this.apps.addAll(apps);
        this.services = services;
        this.appsIdx = new int[apps.size()];
        for(int i = 0; i < apps.size(); i++){
            appsIdx[i] = -1;
        }
        this.appCntEachService = new int[pilots.size()][this.services.size()];
        this.mems = new long[pilots.size()];
        this.cons = new long[pilots.size()];
        this.tryCnt = 1;
        this.upsetRatio = 0;
        this.flag = flag;
        preDistribution();
    }
    public Game(List<App> apps , List<Service> services , List<Pilot> pilots , boolean flag , int tryCnt , double upsetRatio){
        this.pilots = pilots;
        this.apps = new ArrayList<>();
        this.apps.addAll(apps);
        this.services = services;
        this.appsIdx = new int[apps.size()];
        for(int i = 0; i < apps.size(); i++){
            appsIdx[i] = -1;
        }
        this.appCntEachService = new int[pilots.size()][this.services.size()];
        this.mems = new long[pilots.size()];
        this.cons = new long[pilots.size()];
        this.flag = flag;
        this.tryCnt = tryCnt;
        this.upsetRatio = upsetRatio;
        preDistribution();
    }
    public void gameRun(){
        int appIdx , fromPilotIdx,toPilotIdx;
        App tmpApp;
        int[] tmpBestAppsIdx = appsIdx;
        double tmpBestScore = Double.MAX_VALUE;
        RandomNum randomNum = new RandomNum(apps.size());
        for(int i = 0; i < tryCnt; ++i){
            randomNum.init();
            for(int j = 0; j < apps.size() * upsetRatio; ++j){
                appIdx = randomNum.next();
                tmpApp = apps.get(appIdx);
                fromPilotIdx = appsIdx[appIdx];
                delAppFromPilot(tmpApp , fromPilotIdx);
                toPilotIdx = getBestIdx(tmpApp,false);
//                toPilotIdx = NumberUtil.getRandomNum(0,pilots.size());
                setAppToPilot(tmpApp , toPilotIdx);
            }
            randomNum.init();
            while(-1 < (appIdx = randomNum.next())){
                tmpApp = apps.get(appIdx);
                fromPilotIdx = appsIdx[appIdx];
                // 从原来的pilot中删除
                delAppFromPilot(tmpApp , fromPilotIdx);
                // 找一个最好的pilot
                toPilotIdx = getBestIdx(tmpApp,true);
                // 放入最好的pilot
                setAppToPilot(tmpApp , toPilotIdx);
                if(toPilotIdx != fromPilotIdx){
                    randomNum.init();
                }
            }
            double score = score3(mems,cons);
            if(score < tmpBestScore){
                tmpBestScore = score;
                tmpBestAppsIdx = Arrays.copyOf(appsIdx,appsIdx.length);
            }
            log.info("第{}次博弈,分数{},比例{},目前最好分数{}",i,score,score4(mems) / GlobalContain.SERVICE_MEM,tmpBestScore);
        }
        appsIdx = tmpBestAppsIdx;
        for(Pilot pilot : pilots){
            pilot.appList.clear();
            if(!flag) pilot.srvBM.initSrvArr();
        }
        for(int i = 0; i < appsIdx.length; ++i){
            pilots.get(appsIdx[i]).addApp(apps.get(i));
        }
    }

    private void preDistribution(){
        // 对app编号
        for(int i = 0; i < apps.size(); ++i){
            apps.get(i).id = i;
        }
        // 把pilot列表中已经分配的app记录到appsIdx
        if(flag){
            for(int idx = 0; idx < pilots.size(); ++idx){
                Pilot pilot = pilots.get(idx);
                for(App app : pilot.appList){
                    appsIdx[app.id] = idx;
                    cons[idx] += app.count;
                }
                List<Service> srvList = pilot.srvBM.getSrvList();
                for(Service service : srvList){
                    appCntEachService[idx][service.id] = Integer.MAX_VALUE / 2;
                    mems[idx] += service.count;
                }
            }
        }else{
            for(int idx = 0; idx < pilots.size(); ++idx){
                Pilot pilot = pilots.get(idx);
                for(App app : pilot.appList){
                    appsIdx[app.id] = idx;
                    cons[idx] += app.count;
                    for(Service service : app.srvList){
                        if(++appCntEachService[idx][service.id] == 1 ){
                            mems[idx] += service.count;
                        }
                    }
                }
            }
        }

        // 剩余没有分配的app,按最优匹配原则分配
        for(int i = 0; i < appsIdx.length; i++){
            if(appsIdx[i] < 0){
                setAppToPilot(apps.get(i),getBestIdx(apps.get(i),true));
            }
        }
    }

    /**
     *
     * @param app
     * @param flag true为根据方差和选择 false为根据实际加载内存选择
     * @return
     */
    private int getBestIdx(App app , boolean flag){

        double bestScore = Double.MAX_VALUE, tmpScore;
        int  bestPilotIdx = 0 , addMem ;
        for(int i = 0; i < pilots.size(); ++i){
            addMem = 0;
            for(Service service : app.srvList){
                if(appCntEachService[i][service.id] == 0){
                    addMem += service.count;
                }
            }
            cons[i] += app.count;
            mems[i] += addMem;
            if(bestScore > (tmpScore = flag ? score1(mems,cons) : score4(mems))){
                bestScore = tmpScore;
                bestPilotIdx = i;
            }
            cons[i] -= app.count;
            mems[i] -= addMem;
        }
        return bestPilotIdx;
    }

    private void setAppToPilot(App app , int pilotIdx){
        appsIdx[app.id] = pilotIdx;
        cons[pilotIdx] += app.count;
        for(Service service : app.srvList){
            if(++appCntEachService[pilotIdx][service.id] == 1){
                mems[pilotIdx] += service.count;
            }
        }
    }

    private void delAppFromPilot(App app , int pilotIdx){
        appsIdx[app.id] = -1;
        cons[pilotIdx] -= app.count;
        for(Service service : app.srvList){
            if(--appCntEachService[pilotIdx][service.id] == 0){
                mems[pilotIdx] -= service.count;
            }
        }
    }

    /**
     * 标准差的和
     */
    private double score1(long[] mems , long[] cons){
        double stdMem = NumberUtil.calStd(mems);
        double stdCon = NumberUtil.calStd(cons);
        return stdMem * 0.01 + stdCon;
    }

    /**
     * mems[idx]增加add1 cons[idx]增加add2后的标准差的和
     */
    private double score2(long[] mems, long[] cons , int idx , int add1 , int add2){
        double stdMem = NumberUtil.calStd(mems,idx,add1);
        double stdCon = NumberUtil.calStd(cons,idx,add2);
        return stdMem * 0.01 + stdCon;
    }

    /**
     * 标准差的和 * 实际加载内存
     */
    private double score3(long[] mems, long[] cons){
        double[] scores = NumberUtil.calStdAndSum(mems);
        double stdCon = NumberUtil.calStd(cons);
        return (scores[0] * 0.01 + stdCon) * scores[1] / GlobalContain.SERVICE_MEM;
    }

    /**
     * 实际加载内存
     */
    private double score4(long[] mems){
        long sum = NumberUtil.calSum(mems);
        return sum;
    }

    class RandomNum{
        private List<Integer> rest;
        private List<Integer> temp;
        public RandomNum(int sum){
            rest = new ArrayList<>();
            temp = new ArrayList<>();
            for(int i = 0; i < sum; i++){
                temp.add(i);
            }
            init();
        }
        public void init(){
            rest.clear();
            rest.addAll(temp);
        }
        public int next(){
            if(rest.isEmpty()){
                return -1;
            }
            int random = NumberUtil.getRandomNum(0,rest.size());
            int sele = rest.get(random);
            rest.remove(random);
            return sele;
        }
    }
}