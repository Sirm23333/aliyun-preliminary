package com.contest.ali.pilotlb.service.impl.iter6_syp_0618;

import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.constant.GlobalConstant;
import com.contest.ali.pilotlb.service.MainRun;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@Slf4j
public class Iter6MainRinImpl implements MainRun {

    Iter6DataHandler dataHandler = new Iter6DataHandler();
    public static String info = new String();

    Map<String, List<String>> result ;

    @Override
    public Map<String, List<String>> stage1Run(List<String> pilotNames, String dataPath) {
        // 读入数据及预处理
        long start = System.currentTimeMillis();
        String spend = "" ;
        dataHandler.initData(pilotNames, dataPath);
//        spend += (System.currentTimeMillis() - start) + ";";
//        start = System.currentTimeMillis();
//        GA ga = new GA();
//        ga.GARun();
//        spend += (System.currentTimeMillis() - start) + ";";
//        result = dataHandler.buildResultMap();
//        info += spend;
//        info += GlobalContain.APP_SUM_MERGE;
//        result.put(info,new ArrayList<>());
        return result;
    }
    @Override
    public Map<String, List<String>> stage2Run(JSONObject data) {
        JSONObject apps = data.getJSONObject(GlobalConstant.KEY_APPS);
        Set<String> keys = apps.keySet();
        int i = 0;
        for(String key : keys){
            result.get(GlobalContain.PILOT_LIST.get(i % GlobalContain.PILOT_SUM).name).add(key);
            i++;
        }
        return result;
    }

}
