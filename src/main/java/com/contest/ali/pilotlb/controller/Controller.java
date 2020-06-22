package com.contest.ali.pilotlb.controller;
import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.constant.GlobalConstant;
import com.contest.ali.pilotlb.constant.ObjectFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class Controller {

    @Value("${data_path}")
    private String dataPath;

    @GetMapping(value = "/ready")
    public void ready() {
        log.info("ready...");
    }

    @PostMapping(value = "/p1_start")
    public Map<String, List<String>> p1Start(
            @RequestBody JSONObject jsonPilots){
        if(!jsonPilots.containsKey(GlobalConstant.KEY_PILOTS)){
            log.error("bad request parameters : no {}" , GlobalConstant.KEY_PILOTS);
            throw new RuntimeException("bad request parameters!");
        }
        return ObjectFactory.mainRun.stage1Run((List<String>) jsonPilots.get(GlobalConstant.KEY_PILOTS) , dataPath);
    }

    @PostMapping(value = "/p2_start")
    public Map<String, List<String>>  p2Start(
            @RequestBody JSONObject jsonAppsDependencies){
        if(!jsonAppsDependencies.containsKey(GlobalConstant.KEY_APPS)){
            log.error("bad request parameters : no {}" , GlobalConstant.KEY_APPS);
            throw new RuntimeException("bad request parameters!");
        }
        if(!jsonAppsDependencies.containsKey(GlobalConstant.KEY_DEPENDENCIES)){
            log.error("bad request parameters : no {}" , GlobalConstant.KEY_DEPENDENCIES);
            throw new RuntimeException("bad request parameters!");
        }
        return ObjectFactory.mainRun.stage2Run(jsonAppsDependencies);
    }

}
