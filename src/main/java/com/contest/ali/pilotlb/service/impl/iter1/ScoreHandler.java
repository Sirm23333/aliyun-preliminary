package com.contest.ali.pilotlb.service.impl.iter1;

import com.contest.ali.pilotlb.service.impl.iter1.model.App;

import java.util.List;

/**
 *  @author sirm
 *  @Date 2020/5/30 上午11:37
 *  @Description 计算相关
 */
public interface ScoreHandler {

    /**
     * @author: sirm
     * @description: 计算阶段一当前分配方式的最终得分
     * @date: 2020/5/31
     * @return
     */
    double calScore();

    /**
     * @author: sirm
     * @description: 打印输出app列表的情况
     * @date: 2020/5/31
     * @return
     */
    void printAppsOverview(List<App> apps);

}
