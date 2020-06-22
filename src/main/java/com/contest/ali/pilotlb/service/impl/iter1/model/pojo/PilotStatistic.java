package com.contest.ali.pilotlb.service.impl.iter1.model.pojo;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Arrays;

/**
 *  @author sirm
 *  @Date 2020/6/1 下午2:18
 *  @Description pilot列表的统计值
 */
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class PilotStatistic  implements Serializable {
    // pilot中app实例连接数,每个pilot尽量平均,所有pilot的和尽量小
    public int[] connectionCnt ;
    // pilot中加载服务个数,每个pilot尽量平均
    public int[] servicesCnt ;
    // 连接数平均数
    public double connectionAvg ;
    // 加载服务数平均数
    public double servicesAvg ;
    // connectionCnt[idx]增加count
    public void addConnections(int idx , int count){
        if(idx < 0 || idx >= connectionCnt.length){
            log.info("[PilotStatistic:addConnections] idx out of bound");
            throw new RuntimeException();
        }
        connectionCnt[idx] += count;
        connectionAvg += count / connectionCnt.length;
    }
    // servicesCnt[idx]增加count
    public void addServicesCnt(int idx , int count){
        if(idx < 0 || idx >= servicesCnt.length){
            log.info("[PilotStatistic:addServicesCnt] idx out of bound");
            throw new RuntimeException();
        }
        servicesCnt[idx] += count;
        servicesAvg += count / servicesCnt.length;
    }
    public void init(){
        for(int i = 0; i < connectionCnt.length; ++i){
            connectionCnt[i] = 0;
            servicesCnt[i] = 0;
        }
        connectionAvg = 0;
        servicesAvg = 0;
    }

    @Override
    public String toString() {
        return "PilotStatistic{" +
                "connectionCnt=" + Arrays.toString(connectionCnt) +
                ", servicesCnt=" + Arrays.toString(servicesCnt) +
                ", connectionAvg=" + connectionAvg +
                ", servicesAvg=" + servicesAvg +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        PilotStatistic pst = new PilotStatistic();
        pst.connectionAvg = this.connectionAvg;
        pst.servicesAvg = this.servicesAvg;
        pst.connectionCnt = this.connectionCnt.clone();
        pst.servicesCnt = this.servicesCnt.clone();
        return pst;

    }
}
