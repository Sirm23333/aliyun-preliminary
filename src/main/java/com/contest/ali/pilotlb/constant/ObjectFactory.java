package com.contest.ali.pilotlb.constant;

import com.contest.ali.pilotlb.service.DataHandler;
import com.contest.ali.pilotlb.service.MainRun;
import com.contest.ali.pilotlb.service.impl.iter4_syp_0614.Iter4DataHandler;
import com.contest.ali.pilotlb.service.impl.iter4_syp_0614.Iter4MainRinImpl;
import com.contest.ali.pilotlb.service.impl.iter5_syp_0616.Iter5MainRinImpl;
import com.contest.ali.pilotlb.service.impl.iter6_syp_0618.Iter6MainRinImpl;
import com.contest.ali.pilotlb.service.impl.iter7_syp_0620.Iter7MainRinImpl;

public class ObjectFactory {
    public static DataHandler dataHandler;
    public static MainRun mainRun;

    static {
        dataHandler = new Iter4DataHandler();
        mainRun = new Iter7MainRinImpl();
    }

}
