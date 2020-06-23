package com.contest.ali.pilotlb.service.impl.iter9_syp_0623;

import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.service.MainRun;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 *
 */
@Slf4j
public class Iter9MainRunImpl implements MainRun {

    Iter9DataHandler dataHandler = new Iter9DataHandler();

    Map<String, List<String>> result = new HashMap<>();

    public static long start ;

    @Override
    public Map<String, List<String>> stage1Run(List<String> pilotNames, String dataPath) {
        dataHandler.initDataStage1(pilotNames, dataPath);
        new Game( GlobalContain.APP_LIST_SRC , GlobalContain.SERVICE_LIST , GlobalContain.PILOT_LIST,false,20,0.01).gameRun();
        return result = dataHandler.buildResultMap(GlobalContain.PILOT_LIST);
    }

    @Override
    public Map<String, List<String>> stage2Run(JSONObject data) {
        dataHandler.initDataStage2(data);
        new Game( GlobalContain.APP_LIST_SRC  , GlobalContain.SERVICE_LIST , GlobalContain.PILOT_LIST,true,2,0.01).gameRun();
        return result = dataHandler.buildResultMap(GlobalContain.PILOT_LIST);
    }

}
