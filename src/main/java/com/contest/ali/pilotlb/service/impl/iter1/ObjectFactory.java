package com.contest.ali.pilotlb.service.impl.iter1;

import com.contest.ali.pilotlb.service.MainRun;

/**
 *
 */
public class ObjectFactory {
    public static DataHandler dataHandler;
    public static ScoreHandler scoreHandler;
    public static MainRun mainRun;
    static {
        dataHandler = new DataHandlerImpl();
        scoreHandler = new ScoreHandlerImpl();
        mainRun = new MainRunImpl_2();
    }

}
