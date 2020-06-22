package com.contest.ali.pilotlb.service.impl.iter1;
import com.contest.ali.pilotlb.service.impl.iter1.model.App;
import com.contest.ali.pilotlb.service.impl.iter1.model.Pilot;
import com.contest.ali.pilotlb.service.impl.iter1.model.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
public class ScoreHandlerImpl implements ScoreHandler {

    @Override
    public double calScore() {
        List<Pilot> pilots = GlobalContain.pilotList;
        int allServers = 0;
        for(Service service : GlobalContain.serviceSet){
            allServers += service.getCount();
        }
        int connection[] = new int[pilots.size()]; // 每个pilot的连接数
        int service[] = new int[pilots.size()]; // 每个pilot的依赖服务数
        for(int i = 0; i < pilots.size(); ++i){
            connection[i] = pilots.get(i).getConnectionCnt();
            service[i] = pilots.get(i).getSrvCnt();
        }
        int loadService = 0;// 总加载服务数
        for(int i : service){
            loadService += i;
        }
//        log.info("[load mem] = {}" , loadService * 0.01);
//        log.info("[all mem] = {}", allServers * 0.01);
        // 内存标准差
        double difMemory = calDif(service) * 0.01;
//        log.info("[mem std] = {}",difMemory);
        // 连接标准差
        double difConnection = calDif(connection);
//        log.info("[con std] = {}",difConnection);
        // 最终分数
        double score = (double)loadService / allServers * (difMemory + difConnection);
//        log.info("[score] = {}",score);
        return score;
    }

    @Override
    public void printAppsOverview(List<App> apps) {
        int avgServiceCnt = 0; // 依赖服务的平均值
        int maxServiceCnt = 0; // 依赖服务的最大数
        int minServiceCnt = Integer.MAX_VALUE; // 依赖服务的最小数
        int totalServiceCnt = 0; // 依赖服务的总数
        for(App app : apps){
            totalServiceCnt += app.getDependencies().size();
            maxServiceCnt = maxServiceCnt < app.getDependencies().size() ? app.getDependencies().size() : maxServiceCnt;
            minServiceCnt = minServiceCnt > app.getDependencies().size() ? app.getDependencies().size() : minServiceCnt;
        }
        avgServiceCnt = totalServiceCnt / apps.size();
        log.info("Apps overview : totalServiceCnt={}",totalServiceCnt);
        log.info("Apps overview : avgServiceCnt={}",avgServiceCnt);
        log.info("Apps overview : maxServiceCnt={}",maxServiceCnt);
        log.info("Apps overview : minServiceCnt={}",minServiceCnt);
    }

    /**
     * @author: sirm
     * @description: 计算标准差
     * @date: 2020/5/31
     * @return
     */
    private double calDif(int arr[]){
        int len = arr.length;
        int total = 0;
        for(int i : arr){
            total += i;
        }
        double avg = (double)total / len;
        double D = 0.0;
        for(int i : arr){
            D += (i - avg) * (i - avg);
        }
        return Math.sqrt(D / len);
    }


}
