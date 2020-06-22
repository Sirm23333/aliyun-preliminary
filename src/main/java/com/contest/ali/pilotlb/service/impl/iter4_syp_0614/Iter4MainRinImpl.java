package com.contest.ali.pilotlb.service.impl.iter4_syp_0614;

import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.service.MainRun;
import com.contest.ali.pilotlb.service.impl.iter4_syp_0614.model.App;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Slf4j
public class Iter4MainRinImpl implements MainRun {

    Iter4DataHandler dataHandler = new Iter4DataHandler();

    @Override
    public Map<String, List<String>> stage1Run(List<String> pilotNames, String dataPath) {
        List<App> uni = new ArrayList<>();
        List<App> rest = new ArrayList<>();
        dataHandler.initData(pilotNames, dataPath , uni , rest);
        GA ga = new GA(uni);
//        int[] ints = ga.GARun();

//        log.info("{}",ints);
        Map<String,List<String>> result = new HashMap<>();
        return result;
    }

    @Override
    public Map<String, List<String>> stage2Run(JSONObject data) {
        return null;
    }
}
