package com.contest.ali.pilotlb.service.impl.iter9_syp_0623.model;

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
