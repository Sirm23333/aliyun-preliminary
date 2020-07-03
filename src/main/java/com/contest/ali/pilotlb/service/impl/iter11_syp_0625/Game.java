package com.contest.ali.pilotlb.service.impl.iter11_syp_0625;

import com.contest.ali.pilotlb.constant.GlobalConstant;
import com.contest.ali.pilotlb.service.impl.iter11_syp_0625.model.*;

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
    private List<Pilot> pilots ;        // 分配情况,由外部传入,通过博弈对其更改
    private int[] appsIdx;              // 记录每个app在那个pilot中
    private int[][] appCntEachService;  // 记录每个pilot中依赖每一个服务的app的数量
    private long[] mems;                // 每个pilot的内存
    private long[] cons;                // 每个pilot的连接数
    private boolean flag;               // flag为true时,表示第二阶段,对pilots已经加载的内存只增不减
    private int tryCnt;                 // 博弈均衡后再重新打乱重试的次数
    private double upsetRatio;          // 将百分比upsetRatio的app打乱
    private boolean timeout = false;

    /**
     *
     * @param apps
     * @param services
     * @param pilots
     */
    public Game(List<App> apps , List<Service> services , List<Pilot> pilots , String stage){
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
        switch (stage){
            case(GlobalConstant.STAGE_1):flag = false;break;
            case(GlobalConstant.STAGE_2):flag = true;break;
            default:break;
        }
        if(System.currentTimeMillis() > GlobalConstant.END_TIME){
            timeout = true;
        }
        preDistribution();
    }
    public Game(List<App> apps , List<Service> services , List<Pilot> pilots , String stage , int tryCnt , double upsetRatio){
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
        this.tryCnt = tryCnt;
        this.upsetRatio = upsetRatio;
        switch (stage){
            case(GlobalConstant.STAGE_1):flag = false;break;
            case(GlobalConstant.STAGE_2):flag = true;break;
            default:break;
        }
        if(System.currentTimeMillis() > GlobalConstant.END_TIME){
            timeout = true;
        }
        preDistribution();
    }
    public void gameRun(){
        if(flag){
            gameRunSub_2();
        }else {
            gameRunSub_1();
        }
    }

    /**
     * 第一阶段博弈主逻辑
     */
    private void gameRunSub_1(){
        int appIdx , fromPilotIdx,toPilotIdx;
        App tmpApp;
        int[] tmpBestAppsIdx = appsIdx;
        double tmpBestScore = Double.MAX_VALUE;
        RandomNum randomNum = new RandomNum(apps.size());
        for(int i = 0; i < tryCnt && !timeout; ++i){
            if(i > 0){
                randomNum.init();
                // 打破均衡
                int size = (int) (apps.size() * upsetRatio);
                for(int j = 0; j < size; ++j){
                    appIdx = randomNum.next();
                    tmpApp = apps.get(appIdx);
                    fromPilotIdx = appsIdx[appIdx];
                    delAppFromPilot(tmpApp , fromPilotIdx);
                    toPilotIdx = getBestIdx2(tmpApp,4);
                    setAppToPilot(tmpApp , toPilotIdx);
                }
                randomNum.init();
            }
            while(-1 < (appIdx = randomNum.next())){
                tmpApp = apps.get(appIdx);
                fromPilotIdx = appsIdx[appIdx];
                // 从原来的pilot中删除
                delAppFromPilot(tmpApp , fromPilotIdx);
                // 找一个最好的pilot,第一阶段考虑整体分数,第二阶段只考虑标准差之和
                toPilotIdx = getBestIdx2(tmpApp,3);
                // 放入最好的pilot
                setAppToPilot(tmpApp , toPilotIdx);
                if(toPilotIdx != fromPilotIdx){
                    randomNum.init();
                }
                if(System.currentTimeMillis() > GlobalConstant.END_TIME){
                    timeout = true;
                    break;
                }
            }
            // 第二阶段选择标准差小的结果
            double score =  score3(mems,cons);
            if(score < tmpBestScore){
                tmpBestScore = score;
                tmpBestAppsIdx = Arrays.copyOf(appsIdx,appsIdx.length);
            }
//            log.info("第{}次博弈,分数{},比例{},目前最好分数{}",i,score,score4(mems) / GlobalContain.SERVICE_MEM,tmpBestScore);
        }
        appsIdx = tmpBestAppsIdx;
        for(Pilot pilot : pilots){
            pilot.appList.clear();
            pilot.srvBM.initSrvArr(); // 第一阶段加载的内存不会常驻
        }
        for(int i = 0; i < appsIdx.length; ++i){
            pilots.get(appsIdx[i]).addApp(apps.get(i));
        }
    }
    private void gameRunSub_2(){
        int appIdx , fromPilotIdx,toPilotIdx;
        App tmpApp;
        int[] tmpBestAppsIdx = appsIdx;
        double tmpBestScore = Double.MAX_VALUE;
        RandomNum randomNum = new RandomNum(apps.size());
        for(int i = 0; i < tryCnt && !timeout; ++i){
            if(i > 0){
                randomNum.init();
                // 打破均衡
                int size = (int) (apps.size() * upsetRatio);
                for(int j = 0; j < size; ++j){
                    appIdx = randomNum.next();
                    tmpApp = apps.get(appIdx);
                    fromPilotIdx = appsIdx[appIdx];
                    delAppFromPilot(tmpApp , fromPilotIdx);
                    toPilotIdx = getBestIdx2(tmpApp,4);
                    setAppToPilot(tmpApp , toPilotIdx);
                }
                randomNum.init();
            }
            while(-1 < (appIdx = randomNum.next())){
                tmpApp = apps.get(appIdx);
                fromPilotIdx = appsIdx[appIdx];
                // 从原来的pilot中删除
                delAppFromPilot(tmpApp , fromPilotIdx);
                // 找一个最好的pilot,第一阶段考虑整体分数,第二阶段只考虑标准差之和
                toPilotIdx = getBestIdx2(tmpApp,1);
                // 放入最好的pilot
                setAppToPilot(tmpApp , toPilotIdx);
                if(toPilotIdx != fromPilotIdx){
                    randomNum.init();
                }
                if(System.currentTimeMillis() > GlobalConstant.END_TIME){
                    timeout = true;
                    break;
                }
            }
            // 第二阶段选择标准差小的结果
            double score = score1(mems,cons);
            if(score < tmpBestScore){
                tmpBestScore = score;
                tmpBestAppsIdx = Arrays.copyOf(appsIdx,appsIdx.length);
            }
//            log.info("第{}次博弈,分数{},比例{},目前最好分数{}",i,score,score4(mems) / GlobalContain.SERVICE_MEM,tmpBestScore);
        }
        appsIdx = tmpBestAppsIdx;
        for(Pilot pilot : pilots){
            pilot.appList.clear();
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
                setAppToPilot(apps.get(i),getBestIdx2(apps.get(i),1));
            }
        }
    }

    /**
     *
     * @param app
     * @param flag true为根据方差和选择 false为根据实际加载内存选择
     * @return
     */
    private int getBestIdx(App app , int flag){

        double bestScore = Double.MAX_VALUE, tmpScore = bestScore;
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
            switch (flag){
                case 1:tmpScore = score1(mems,cons);break; // 标准差之和
                case 3:tmpScore = score3(mems,cons);break; // 标准差之和 * 比例
                case 4:tmpScore = score4(mems);break; // 内存总和
                case 5:tmpScore = score5(mems);break; // 内存标准差
                default:break;
            }
            if(bestScore > tmpScore){
                bestScore = tmpScore;
                bestPilotIdx = i;
            }
            cons[i] -= app.count;
            mems[i] -= addMem;
        }
        return bestPilotIdx;
    }
    private int getBestIdx2(App app , int flag){
        double bestScore = Double.MAX_VALUE, tmpScore = bestScore;
        int  bestPilotIdx = 0 , addMem ;
        int size = pilots.size();
        for(int i = 0; i < size; ++i){
            addMem = 0;
            for(Service service : app.srvList){
                if(appCntEachService[i][service.id] == 0){
                    addMem += service.count;
                }
            }
            switch (flag){
                case 1:tmpScore = score1(mems,cons,i,addMem,app.count);break; // 标准差之和
                case 3:tmpScore = score3(mems,cons,i,addMem,app.count);break; // 标准差之和 * 比例
                case 4:tmpScore = score4(mems,addMem);break; // 内存总和
                case 5:tmpScore = score5(mems,i,addMem);break; // 内存标准差
                default:break;
            }
            if(bestScore > tmpScore){
                bestScore = tmpScore;
                bestPilotIdx = i;
            }
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
    private double score1(long[] mems, long[] cons , int idx , long add1 , long add2){
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

    private double score3(long[] mems, long[] cons, int i, long addMem, long count) {
        double[] scores = NumberUtil.calStdAndSum(mems,i,addMem);
        double stdCon = NumberUtil.calStd(cons,i,count);
        return (scores[0] * 0.01 + stdCon) * scores[1] / GlobalContain.SERVICE_MEM;
    }
    /**
     * 实际加载内存
     */
    private double score4(long[] mems){
        long sum = NumberUtil.calSum(mems);
        return sum;
    }
    private double score4(long[] mems , long add){
        long sum = NumberUtil.calSum(mems);
        return sum + add;
    }
    /**
     * 内存标准差
     */
    private double score5(long[] mems){
        double std = NumberUtil.calStd(mems);
        return std;
    }
    private double score5(long[] mems , int idx , long add){
        double std = NumberUtil.calStd(mems , idx , add);
        return std;
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