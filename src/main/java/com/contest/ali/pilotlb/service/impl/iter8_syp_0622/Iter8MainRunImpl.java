package com.contest.ali.pilotlb.service.impl.iter8_syp_0622;

import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.service.MainRun;
import com.contest.ali.pilotlb.service.impl.iter5_syp_0616.GA;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 *
 */
@Slf4j
public class Iter8MainRunImpl implements MainRun {

    Iter8DataHandler dataHandler = new Iter8DataHandler();

    Map<String, List<String>> result = new HashMap<>();

    public static long start ;
    @Override
    public Map<String, List<String>> stage1Run(List<String> pilotNames, String dataPath) {
        dataHandler.initDataStage1(pilotNames, dataPath);
//        new Game( GlobalContain.APP_LIST_MERGE , GlobalContain.SERVICE_LIST , GlobalContain.PILOT_LIST,false).gameRun();
//        dataHandler.formatPilotList(GlobalContain.PILOT_LIST);
        long start = System.currentTimeMillis();
        new Game( GlobalContain.APP_LIST_SRC , GlobalContain.SERVICE_LIST , GlobalContain.PILOT_LIST,false,10,0.1).gameRun();
        result = dataHandler.buildResultMap(GlobalContain.PILOT_LIST);
        double[] score = dataHandler.calScore(GlobalContain.PILOT_LIST);
//        result.put(Arrays.toString(score) + (System.currentTimeMillis()-start),new ArrayList<>());
        return result;
    }

    @Override
    public Map<String, List<String>> stage2Run(JSONObject data) {
        dataHandler.initDataStage2(data);
        new Game( GlobalContain.APP_LIST_SRC  , GlobalContain.SERVICE_LIST , GlobalContain.PILOT_LIST,true,2,0.01).gameRun();
        return result = dataHandler.buildResultMap(GlobalContain.PILOT_LIST);
    }

}
