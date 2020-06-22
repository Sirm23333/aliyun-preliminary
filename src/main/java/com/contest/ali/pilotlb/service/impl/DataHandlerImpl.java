package com.contest.ali.pilotlb.service.impl;
import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.service.DataHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class DataHandlerImpl implements DataHandler {
    @Override
    public JSONObject dataReader(String path) {
        String input = null;
        try {
            input = FileUtils.readFileToString(new File(path), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("io error!");        }
        JSONObject jsonObject = JSONObject.parseObject(input);
        return jsonObject;
    }
}
