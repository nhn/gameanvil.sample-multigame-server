package com.nhnent.tardis.sample.session.handlers;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.session.SampleSession;
import com.nhnent.tardis.sample.session.SampleSessionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionAgentRemoveTimerPacketHandler implements
    IPacketHandler<SampleSession> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(SampleSession sampleSession, Packet packet) throws SuspendExecution {
        logger.info("SessionAgentRemoveTimerPacketHandler");
        ((SampleSessionNode) SampleSessionNode.getInstance()).removeTimer();
    }
}
