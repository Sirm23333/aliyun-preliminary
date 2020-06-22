package com.contest.ali.pilotlb.service.impl.iter6_syp_0618.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Service {
    public int id;
    public String name;
    public int count;

    @Override
    public boolean equals(Object obj) {
        return this.name.equals(((Service)obj).name);
    }
}
