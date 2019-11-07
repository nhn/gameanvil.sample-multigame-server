package com.nhnent.tardis.sample.session.handlers;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.SampleSessionAgent;
import com.nhnent.tardis.sample.session.SampleSessionNodeAgent;
import java.io.IOException;
import javax.xml.soap.Node;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionAgentSetTimerPacketHandler implements
    IPacketHandler<SampleSessionAgent> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public void execute(SampleSessionAgent sampleSessionAgent, Packet packet) throws SuspendExecution {
        logger.info("SessionAgentSetTimerPacketHandler");
        try {
            Sample.SetTimer msg = Sample.SetTimer.parseFrom(packet.getStream());
            ((SampleSessionNodeAgent)SampleSessionNodeAgent.getInstance()).setTimer(msg.getInterval(), msg.getMessage());
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}