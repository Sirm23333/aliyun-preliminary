package com.contest.ali.pilotlb.service.impl.iter3_syp_0611.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Service {
    public String name;
    public int count;

    @Override
    public boolean equals(Object obj) {
        return this.name.equals(((Service)obj).name);
    }
}
