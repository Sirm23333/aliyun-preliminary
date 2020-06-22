package com.contest.ali.pilotlb.service.impl.iter1;

import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.service.impl.iter1.model.App;
import com.contest.ali.pilotlb.service.impl.iter1.model.Pilot;
import com.contest.ali.pilotlb.service.impl.iter1.model.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;


interface DataHandler {

    /**
     * @author: sirm
     * @description: 读本地data.json
     * @date: 2020/5/29
     * @return
     */
    JSONObject dataReader(String path) ;
    /**
     * @author: sirm
     * @description: 将从包含apps和dependencies的json转为list
     * @date: 2020/5/29
     * @return
     */
    List<App> jsonToAppsList(JSONObject data);

    /**
     * @author: sirm
     * @description: 将接收的pilot列表转为pilot对象list
     * @date: 2020/5/29
     * @return
     */
    List<Pilot> listToPilotsList(List<String> pilots);

    /**
     * @author: sirm
     * @description: 由app列表获得所有的依赖服务集合
     * @date: 2020/5/30
     * @return
     */
    Set<Service> getServiceSet(List<App> apps);
    /**
     * @author: sirm
     * @description: 由app列表获得所有的依赖服务映射
     * @date: 2020/6/2
     * @return
     */
    Map<String, Service> getServiceMap(List<App> appList);
    /**
     * @author: sirm
     * @description: 打印数据特征
     * @date: 2020/6/5
     * @return
     */
    void printDatas();
}
