package com.contest.ali.pilotlb.service.impl.iter1;
import com.contest.ali.pilotlb.service.impl.iter1.model.App;
import com.contest.ali.pilotlb.service.impl.iter1.model.Pilot;
import com.contest.ali.pilotlb.service.impl.iter1.model.Service;
import com.contest.ali.pilotlb.service.impl.iter1.model.pojo.PilotStatistic;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

/**
 *  @author sirm
 *  @Date 2020/6/1 下午2:56
 *  @Description 阶段一加权分配
 */
@Slf4j
public class MainRunImpl_2 extends MainRunImpl {

    public static double weight[] = { 1 , 1 , 0 };
    public static double oWeight[] = { 1 , 1 , 0 }; // 迭代后找到的最优参数
    public static double step[] = {0.02, 0 , 0}; // 每次迭代增加的步长
    public static double oValue = Double.MAX_VALUE; // 在用oWeight参数跑出的第一阶段的成绩
    public static double timeLimit1 = 1000 * 60 * 1.5; // 1.5分钟
    public static double timeLimit2 = 1000 * 10; // 10秒
    @Override
    protected void staticLB(){
        // 对app按依赖服务数排序
        Collections.sort(GlobalContain.appList,(app1,app2)-> {
            return app2.getDependencies().size() - app1.getDependencies().size();
        });
        // 迭代找最优参数
        getOWeight();
        // 初始化全局容器
        GlobalContain.initPilotList();
        GlobalContain.initPilotStatistic();

//        ObjectFactory.dataHandler.printDatas();

        int choceIdx;
        for(App app : GlobalContain.appList){
            choceIdx = getPilotIdx(app,oWeight);
            int addServiceCnt = GlobalContain.pilotList.get(choceIdx).addApp(app);
            GlobalContain.pilotStatistic.addServicesCnt(choceIdx , addServiceCnt);
            GlobalContain.pilotStatistic.addConnections(choceIdx , app.getCount());
        }
    }
    @Override
    protected void dynamicLB(List<App> newApps){
        // 对app按依赖服务数排序
        Collections.sort(newApps,(app1,app2)-> {
            return app2.getDependencies().size() - app1.getDependencies().size();
        });
        getOWeight2(newApps);
        log.info("======weight {}",oWeight);
        int choceIdx;
        for(App app : newApps){
            choceIdx = getPilotIdx(app,oWeight);
            int addServiceCnt = GlobalContain.pilotList.get(choceIdx).addApp(app);
            GlobalContain.pilotStatistic.addServicesCnt(choceIdx , addServiceCnt);
            GlobalContain.pilotStatistic.addConnections(choceIdx , app.getCount());
        }
    }
    /**
     * @author: sirm
     * @description: 计算把app分到pilot上的损耗值
     * @date: 2020/6/1
     * @return
     */
    private int getPilotIdx(App app , double[] mWeight){
        int pilotIdx = 0;
        double minValue = Double.MAX_VALUE;
        double currValue = 0;
        // 需要增加的service实例数
        int addServiceCnt ;
        // 连接数到平均值的距离
        double opprConAvg ;
        double opprSrvAvg;
        for(int idx = 0; idx < GlobalContain.pilotList.size(); ++idx){
            Pilot pilot = GlobalContain.pilotList.get(idx);
            PilotStatistic pilotStatistic = GlobalContain.pilotStatistic;
            // 需要增加的service实例数
            addServiceCnt = 0;
            for(Service service : app.getDependencies()){
                if(!pilot.getServers().contains(service)){
                    addServiceCnt += service.getCount();
                }
            }
            // 需要增加的依赖服务数
            opprSrvAvg = pilotStatistic.servicesCnt[idx] - pilotStatistic.servicesAvg ;
            opprConAvg = pilotStatistic.connectionCnt[idx] - pilotStatistic.connectionAvg ;
            currValue = mWeight[0] * addServiceCnt  +  mWeight[1] * opprSrvAvg + mWeight[2] * opprConAvg;
            if(minValue > currValue){
                minValue = currValue;
                pilotIdx = idx;
            }
        }
        return pilotIdx;
    }
    /**
     * @author: sirm
     * @description: 阶段一迭代1.5分钟找一个最优权重参数
     * @date: 2020/6/2
     * @return
     */
    private void getOWeight(){
        long start = System.currentTimeMillis();
        while(System.currentTimeMillis() - start < timeLimit1){
            // 先把容器初始化
            GlobalContain.initPilotList();
            GlobalContain.initPilotStatistic();
            // 增加步长
            for(int i = 0 ; i < weight.length; ++i){
                weight[i] += step[i];
            }
            int choceIdx;
            for(App app : GlobalContain.appList){
                choceIdx = getPilotIdx(app,weight);
                int addServiceCnt = GlobalContain.pilotList.get(choceIdx).addApp(app);
                GlobalContain.pilotStatistic.addServicesCnt(choceIdx , addServiceCnt);
                GlobalContain.pilotStatistic.addConnections(choceIdx , app.getCount());
            }
            double value = ObjectFactory.scoreHandler.calScore();
            log.info(" weight={},value={}" , weight , value);
            if(value < oValue){
                oValue = value;
                oWeight = weight.clone();
            }
        }
    }

    private void getOWeight2(List<App> newApps){
        long start = System.currentTimeMillis();
        weight[0] = 0.1;
        weight[1] = 1;
        oValue = Double.MAX_VALUE;
        double servicesAvg = GlobalContain.pilotStatistic.servicesAvg;
        double connectionAvg =  GlobalContain.pilotStatistic.connectionAvg;
        int[] servicesCnt = new int[GlobalContain.pilotStatistic.servicesCnt.length];
        int[] connectionCnt = new int[GlobalContain.pilotStatistic.connectionCnt.length];
        System.arraycopy(GlobalContain.pilotStatistic.servicesCnt, 0, servicesCnt, 0, servicesCnt.length);
        System.arraycopy(GlobalContain.pilotStatistic.connectionCnt, 0, connectionCnt, 0, connectionCnt.length);
        List<PilotAdd> pilotAdds = new ArrayList<>();
        for(int i = 0; i < GlobalContain.pilotList.size(); ++i){
            pilotAdds.add(new PilotAdd());
        }
        while(System.currentTimeMillis() - start < timeLimit2){
            // 增加步长
            for(int i = 0 ; i < weight.length; ++i){
                weight[i] += step[i];
            }
            int choceIdx;
            for(App app : newApps){
                choceIdx = getPilotIdx(app,weight);
                pilotAdds.get(choceIdx).apps.add(app);
                for(Service service : app.getDependencies()){
                    if(!GlobalContain.pilotList.get(choceIdx).getServers().contains(service)){
                        pilotAdds.get(choceIdx).services.add(service);
                    }
                }
                int addServiceCnt = GlobalContain.pilotList.get(choceIdx).addApp(app);
                GlobalContain.pilotStatistic.addServicesCnt(choceIdx , addServiceCnt);
                GlobalContain.pilotStatistic.addConnections(choceIdx , app.getCount());
            }
            double value = ObjectFactory.scoreHandler.calScore();
            log.info(" weight={},value={}" , weight , value);
            if(value < oValue){
                oValue = value;
                oWeight = weight.clone();
            }
            GlobalContain.pilotStatistic.connectionAvg = connectionAvg;
            GlobalContain.pilotStatistic.servicesAvg = servicesAvg;
            System.arraycopy(servicesCnt, 0, GlobalContain.pilotStatistic.servicesCnt, 0, servicesCnt.length);
            System.arraycopy(connectionCnt, 0, GlobalContain.pilotStatistic.connectionCnt, 0, connectionCnt.length);
            for(int i = 0; i < pilotAdds.size(); ++i){
                for(App app : pilotAdds.get(i).apps){
                    GlobalContain.pilotList.get(i).getApps().remove(app);
                    GlobalContain.pilotList.get(i).setConnectionCnt(GlobalContain.pilotList.get(i).getConnectionCnt() - app.getCount());
                }
                for(Service service : pilotAdds.get(i).services){
                    if(GlobalContain.pilotList.get(i).getServers().contains(service)){
                        GlobalContain.pilotList.get(i).getServers().remove(service);
                        GlobalContain.pilotList.get(i).setSrvCnt(GlobalContain.pilotList.get(i).getSrvCnt() - service.getCount());
                    }
                }
            }
            for(int i = 0; i < GlobalContain.pilotList.size(); ++i){
                pilotAdds.get(i).apps.clear();
                pilotAdds.get(i).services.clear();
            }

        }
    }
    class PilotAdd{
        List<App> apps = new ArrayList<>();
        Set<Service> services = new HashSet<>();
    }
}
