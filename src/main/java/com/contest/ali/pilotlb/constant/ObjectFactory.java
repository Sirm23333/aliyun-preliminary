package com.contest.ali.pilotlb.constant;

import com.contest.ali.pilotlb.service.DataHandler;
import com.contest.ali.pilotlb.service.MainRun;
import com.contest.ali.pilotlb.service.impl.iter10_syp_0624.Iter10MainRunImpl;
import com.contest.ali.pilotlb.service.impl.iter11_syp_0625.Iter11MainRunImpl;
import com.contest.ali.pilotlb.service.impl.iter4_syp_0614.Iter4DataHandler;

public class ObjectFactory {
    public static DataHandler dataHandler;
    public static MainRun mainRun;

    static {
        dataHandler = new Iter4DataHandler();
        mainRun = new Iter11MainRunImpl();
    }

}
