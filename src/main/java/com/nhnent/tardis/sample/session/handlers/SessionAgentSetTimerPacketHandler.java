package com.nhnent.tardis.sample.session.handlers;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.SampleSession;
import com.nhnent.tardis.sample.session.SampleSessionNode;
import java.io.IOException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionAgentSetTimerPacketHandler implements
    IPacketHandler<SampleSession> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public void execute(SampleSession sampleSession, Packet packet) throws SuspendExecution {
        try {
            logger.info("SessionAgentSetTimerPacketHandler");
            Sample.SetTimer msg = Sample.SetTimer.parseFrom(packet.getStream());
            ((SampleSessionNode) SampleSessionNode.getInstance()).setTimer(msg.getInterval(), msg.getMessage());
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}