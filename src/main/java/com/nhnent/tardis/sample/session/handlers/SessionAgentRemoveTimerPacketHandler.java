package com.nhnent.tardis.sample.session.handlers;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.SampleSessionAgent;
import com.nhnent.tardis.sample.session.SampleSessionNodeAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionAgentRemoveTimerPacketHandler implements
    IPacketHandler<SampleSessionAgent> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(SampleSessionAgent sampleSessionAgent, Packet packet) throws SuspendExecution {
        logger.info("SessionAgentRemoveTimerPacketHandler");
        ((SampleSessionNodeAgent)SampleSessionNodeAgent.getInstance()).removeTimer();
    }
}
