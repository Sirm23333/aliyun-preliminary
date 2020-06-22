package com.contest.ali.pilotlb.service.impl.iter1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 *  @author sirm
 *  @Date 2020/5/28 下午9:00
 *  @Description 应用类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class App implements Serializable {
    Integer id;
    String appName;
    Integer count; // app count
    Set<Service> dependencies; // dependencies list
}
