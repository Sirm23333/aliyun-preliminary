package com.contest.ali.pilotlb.service.impl.iter11_syp_0625;

import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.constant.GlobalConstant;
import com.contest.ali.pilotlb.service.MainRun;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 第一阶段为合并两次,
 * 第一次为根据 交集/并集 定义的相似度合并 , 合并下限为0.9
 * 第二次为根据 交集/自身 定义的相似度合并 , 合并下限为0.8 , 并且满足所有的合并后的app内存小于3倍的平均值 , 连接小于平均值
 * 对合并后的app博弈,博弈均衡后,再将1%的app打乱,打乱规则为随机选择app,放入使比例降低最多的pilot中 , 重复多次取最好值
 * 对app进行一次拆分,拆分为第二次合并前的状态
 * 再用同样方式博弈取最好
 * 对app进行二次拆分,拆分为原始状态
 * 再用同样的方式博弈去最好
 * 第二阶段为对全局博弈,博弈均衡后,再将1%的app打乱,打乱规则为随机选择app,放入使比例降低最多的pilot中,尝试多次取最好结果
 * */
@Slf4j
public class Iter11MainRunImpl implements MainRun {

    Iter11DataHandler dataHandler = new Iter11DataHandler();

    Map<String, List<String>> result = new HashMap<>();



    @Override
    public Map<String, List<String>> stage1Run(List<String> pilotNames, String dataPath) {
        GlobalConstant.START_TIME = System.currentTimeMillis();
        GlobalConstant.END_TIME = GlobalConstant.START_TIME + GlobalConstant.STAGE_1_TIME_LIMIT - 500;
        dataHandler.initDataStage1(pilotNames, dataPath);
        new Game( GlobalContain.APP_LIST_MERGE_II , GlobalContain.SERVICE_LIST , GlobalContain.PILOT_LIST, GlobalConstant.STAGE_1,10,0.1).gameRun();
        dataHandler.formatPilotListII(GlobalContain.PILOT_LIST); // 把第二次合并的拆开
        new Game( GlobalContain.APP_LIST_MERGE , GlobalContain.SERVICE_LIST , GlobalContain.PILOT_LIST,GlobalConstant.STAGE_1,10,0.1).gameRun();
        dataHandler.formatPilotList(GlobalContain.PILOT_LIST); // 把第一次合并的拆开
        new Game( GlobalContain.APP_LIST_SRC , GlobalContain.SERVICE_LIST , GlobalContain.PILOT_LIST,GlobalConstant.STAGE_1,8,0.1).gameRun();
        return result = dataHandler.buildResultMap(GlobalContain.PILOT_LIST);
    }

    @Override
    public Map<String, List<String>> stage2Run(JSONObject data) {
        GlobalConstant.START_TIME = System.currentTimeMillis();
        GlobalConstant.END_TIME = GlobalConstant.START_TIME + GlobalConstant.STAGE_2_TIME_LIMIT - 500;
        dataHandler.initDataStage2(data);
        new Game( GlobalContain.APP_LIST_SRC  , GlobalContain.SERVICE_LIST , GlobalContain.PILOT_LIST,GlobalConstant.STAGE_2,2,0.02).gameRun();
        return result = dataHandler.buildResultMap(GlobalContain.PILOT_LIST);
    }

}
