package com.contest.ali.pilotlb.service.impl.iter5_syp_0616;
import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.constant.GlobalConstant;
import com.contest.ali.pilotlb.service.MainRun;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 *
 */
@Slf4j
public class Iter5MainRinImpl implements MainRun {

    Iter5DataHandler dataHandler = new Iter5DataHandler();
    public static String info = new String();

    Map<String, List<String>> result ;
    @Override
    public Map<String, List<String>> stage1Run(List<String> pilotNames, String dataPath) {
        // 读入数据及预处理
        long start = System.currentTimeMillis();
        dataHandler.initData(pilotNames, dataPath);
        GA ga = new GA();
        ga.GARun();
        result = dataHandler.buildResultMap();
        info += (System.currentTimeMillis() - start)+"";
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
