package com.contest.ali.pilotlb.service.impl.iter4_syp_0614.model;

import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class App implements Comparable{
    public String name;
    public int count;
    public Set<Service> services ;
    public long srvMem;

    @Override
    public boolean equals(Object obj) {
        return name.equals(((App)obj).name);
    }

    @Override
    public int compareTo(Object o) {
        return ((App)o).services.size() - this.services.size();
    }
}
