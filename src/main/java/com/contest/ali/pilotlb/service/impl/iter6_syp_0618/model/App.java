package com.contest.ali.pilotlb.service.impl.iter6_syp_0618.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class App implements Comparable{
    public String name;
    public int count;
//    public List<Service> services ;
    public long[] srvDepend;
    public long srvMem;


    @Override
    public boolean equals(Object obj) {
        return name.equals(((App)obj).name);
    }

    @Override
    public int compareTo(Object o) {
        return (int) (((App)o).srvMem - this.srvMem);
    }
}
