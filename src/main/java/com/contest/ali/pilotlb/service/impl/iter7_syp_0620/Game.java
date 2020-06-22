package com.contest.ali.pilotlb.service.impl.iter7_syp_0620;
import com.contest.ali.pilotlb.service.impl.iter7_syp_0620.model.App;
import com.contest.ali.pilotlb.service.impl.iter7_syp_0620.model.Pilot;
import com.contest.ali.pilotlb.service.impl.iter7_syp_0620.model.Service;
import com.contest.ali.pilotlb.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
@Slf4j
public class Game{

    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(4);
    private List<App> apps; // 待分配的所有的app
    private List<Service> services ; // 所有的服务
    private List<Pilot> pilots ; // 一种分配情况
    private RandomNum randomNum;
    private int[] appsIdx; // 记录每个app在那个pilot中
    private int[][] appCntEachService; // 记录每个pilot中依赖每一个服务的app的数量
    private long[] mems; // 每个pilot的内存
    private long[] cons; // 每个pilot的连接数
    private boolean flag;

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
        this.randomNum = new RandomNum(apps.size());
        this.flag = flag;
        preDistribution();
    }

    public void gameRun(){
        int appIdx , fromPilotIdx,toPilotIdx;
        App tmpApp;
//        while(-1 < (appIdx = randomNum.next()) && System.currentTimeMillis() - Iter7MainRinImpl.start < 14500){
        while(-1 < (appIdx = randomNum.next())){
            tmpApp = apps.get(appIdx);
            fromPilotIdx = appsIdx[appIdx];
            // 从原来的pilot中删除
            delAppFromPilot(tmpApp , fromPilotIdx);
            // 找一个最好的pilot
            toPilotIdx = getBestIdx(tmpApp);
            // 放入最好的pilot
            setAppToPilot(tmpApp , toPilotIdx);
            if(toPilotIdx != fromPilotIdx){
                randomNum.init();
            }
        }
        for(Pilot pilot : pilots){
            pilot.appList.clear();
            if(!flag) pilot.srvBM.initSrvArr();
        }
        for(int i = 0; i < appsIdx.length; ++i){
            pilots.get(appsIdx[i]).addApp(apps.get(i));
        }
    }

    private double score(long[] mems , long[] cons){
        double stdMem = NumberUtil.calStd(mems);
        double stdCon = NumberUtil.calStd(cons);
        return stdMem * 0.01 + stdCon;
    }
    private double score(long[] mems, long[] cons , int idx , int add1 , int add2){
        double stdMem = NumberUtil.calStd(mems,idx,add1);
        double stdCon = NumberUtil.calStd(cons,idx,add2);
        return stdMem * 0.01 + stdCon;
    }
    private void preDistribution(){
        // 对app排序
//        Collections.sort(apps);
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
                setAppToPilot(apps.get(i),getBestIdx(apps.get(i)));
            }
        }
    }

    private int getBestIdx(App app){

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
            if(bestScore > (tmpScore = score(mems,cons))){
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