package com.contest.ali.pilotlb.service.impl.iter10_syp_0624.model;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class App implements Comparable,Cloneable{
    public int id;
    public String name;
    public int count;
    public ServiceBitMap srvBitMap; // 依赖的服务bitmap,便于计算相似度
    public List<Service> srvList; // 依赖服务列表
    public long srvMem;

    @Override
    public boolean equals(Object obj) {
        return name.equals(((App)obj).name);
    }

    @Override
    public App clone() throws CloneNotSupportedException {
        List<Service> srvs = new ArrayList<>();
        srvs.addAll(srvList);
        return new App(id,name,count,(ServiceBitMap) srvBitMap.clone(),srvs,srvMem);
    }

    /**
     * 计算该app和another的相似度
     * same / (this.srvMem + another.srvMem)
     * @param another
     * @return
     */
    public double calJacSimilarityByMem(App another){
        long same = calSameMem(another);
        return (double) same / (this.srvMem + another.srvMem - same);
    }

    public void updateListByBitMap(){
        srvList = srvBitMap.getSrvList();
    }

    public void updateBitMapByList(){
        if(srvBitMap == null){
            throw new RuntimeException();
        }
        srvBitMap.initSrvArr();
        srvBitMap.setSrvBitMap(this.srvList);
    }
    /**
     * 计算该app和another相同的内存大小
     * @param another
     * @return
     */
    public long calSameMem(App another){
        return this.srvBitMap.calSameMem(another.srvBitMap);
    }

    @Override
    public int compareTo(Object o) {
        long diff = ((App)o).srvMem - this.srvMem;
        if(diff > 0){
            return 1;
        }else if(diff < 0){
            return -1;
        }else{
            return 0;
        }
    }
}
