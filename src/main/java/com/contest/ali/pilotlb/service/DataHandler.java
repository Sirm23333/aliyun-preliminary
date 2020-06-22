package com.contest.ali.pilotlb.service;

import com.alibaba.fastjson.JSONObject;

/**
 *
 */
public interface DataHandler {

    /**
     * @author: sirm
     * @description: 将本地文件的data.json读为json形式
     * @date: 2020/6/6
     * @return
     */
    JSONObject dataReader(String path) ;

}
