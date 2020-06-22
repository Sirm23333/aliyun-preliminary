package com.contest.ali.pilotlb.service.impl.iter4_syp_0614.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
public class Pilot {
    public int id;
    public String name;
    public List<App> apps;
    public Set<Service> services;
    public void addApp(App app){
        apps.add(app);
        services.addAll(app.services);
    }
}
