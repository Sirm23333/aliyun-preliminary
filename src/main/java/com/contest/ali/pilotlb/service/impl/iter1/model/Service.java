package com.contest.ali.pilotlb.service.impl.iter1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *  @author sirm
 *  @Date 2020/5/28 下午9:00
 *  @Description 服务类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Service implements Serializable {
    private Integer id;
    private String serviceName;
    private Integer count; // server node-count

    @Override
    public boolean equals(Object o){
        return ((Service)o).serviceName.equals(this.serviceName);
    }

}
