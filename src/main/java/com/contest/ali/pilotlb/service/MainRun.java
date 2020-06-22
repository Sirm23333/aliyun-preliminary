package com.contest.ali.pilotlb.service;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

public interface MainRun {

    Map<String , List<String>> stage1Run(List<String> pilotNames , String dataPath);

    Map<String , List<String>> stage2Run(JSONObject data);
}
