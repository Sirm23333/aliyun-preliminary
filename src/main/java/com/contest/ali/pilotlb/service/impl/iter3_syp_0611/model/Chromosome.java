package com.contest.ali.pilotlb.service.impl.iter3_syp_0611.model;

import com.contest.ali.pilotlb.service.impl.iter3_syp_0611.GlobalContain;
import com.contest.ali.pilotlb.util.NumberUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Chromosome {

    public List<Set<App>> chromosome;
    public Map<String,Integer> appMap; // app-name_pilot-idx
    public double fitness;

    public Chromosome copy(){
        Chromosome nchromo = new Chromosome();
        nchromo.fitness = this.fitness;
        nchromo.chromosome = new ArrayList<>();
        nchromo.appMap = new HashMap<>();
        for(Set<App> apps : chromosome){
            Set<App> napps = new HashSet<>();
            napps.addAll(apps);
            nchromo.chromosome.add(napps);
        }
        nchromo.appMap.putAll(this.appMap);
        return nchromo;
    }

    public void crossover(int idx , Chromosome that , int thatIdx){
        Set<App> preCroThis = this.chromosome.get(idx);
        Set<App> preCrothat = that.chromosome.get(thatIdx);
        this.chromosome.set(idx , preCrothat);
        that.chromosome.set(thatIdx , preCroThis);
        Set<App> toDelThis = new HashSet<>();
        Set<App> toAddThis = new HashSet<>();
        Set<App> toDelThat = new HashSet<>();
        Set<App> toAddThat = new HashSet<>();
        for(App app : preCroThis){
            if(!preCrothat.contains(app)){
                toDelThat.add(app);
                toAddThis.add(app);
            }
        }
        for (App app : preCrothat){
            if(!preCroThis.contains(app)){
                toDelThis.add(app);
                toAddThat.add(app);
            }
        }
        // 删除重复的
        for(App app : toDelThis){
            Integer pilotIdx = this.appMap.get(app.name);
            this.chromosome.get(pilotIdx).remove(app);
            this.appMap.put(app.name,idx);
        }
        for(App app : toDelThat){
            Integer pilotIdx = that.appMap.get(app.name);
            that.chromosome.get(pilotIdx).remove(app);
            that.appMap.put(app.name,thatIdx);
        }
        // 添加缺少的
        for(App app : toAddThis){
            Integer pilotIdx = appMap.get(GlobalContain.similarityArr.get(app.name).get(0).app.name);
            this.chromosome.get(pilotIdx).add(app);
            this.appMap.put(app.name,pilotIdx);
        }
        for(App app : toAddThat){
            Integer pilotIdx = appMap.get(GlobalContain.similarityArr.get(app.name).get(0).app.name);
            that.chromosome.get(pilotIdx).add(app);
            that.appMap.put(app.name,pilotIdx);
        }
        this.fitness = -1;
        that.fitness = -1;
    }

    public void variation(int idx1 , int idx2){
        if(idx1 == idx2){
            return;
        }
        Set<App> apps1 = this.chromosome.get(idx1);
        Set<App> apps2 = this.chromosome.get(idx2);
        if(apps1.size()==0 || apps2.size()==0){
            return;
        }
        App app1 = null, app2 = null;
        int random1 = NumberUtil.getRandomNum(0,apps1.size());
        int random2 = NumberUtil.getRandomNum(0,apps2.size());
        int i = 0;
        for(App app : apps1){
            if(i == random1){
                app1 = app;
                break;
            }
            i++;
        }
        i = 0;
        for(App app : apps2){
            if(i == random2){
                app2 = app;
                break;
            }
            i++;
        }
        apps1.remove(app1);
        apps1.add(app2);
        apps2.remove(app2);
        apps2.add(app1);
        this.appMap.put(app1.name , idx2);
        this.appMap.put(app2.name , idx1);
        this.fitness = -1;
    }

}
