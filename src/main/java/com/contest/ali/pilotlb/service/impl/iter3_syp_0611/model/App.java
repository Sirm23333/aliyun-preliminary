package com.contest.ali.pilotlb.service.impl.iter3_syp_0611.model;

import lombok.AllArgsConstructor;
import java.util.Set;

@AllArgsConstructor
public class App {
    public String name;
    public int count;
    public Set<Service> services ;

    @Override
    public boolean equals(Object obj) {
        return name.equals(((App)obj).name);
    }
}
