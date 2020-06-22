package com.contest.ali.pilotlb.service.impl.iter3_syp_0611.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
public class Pilot {
    public String name;
    public List<App> apps;
    public Set<Service> services;
    public void addApp(App app){
        apps.add(app);
        services.addAll(app.services);
    }
}
