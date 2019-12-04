package com.nhnent.tardis.sample.session.cmd;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.session.SampleSession;
import com.nhnent.tardis.sample.session.SampleSessionNode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

public class CmdSessionAgentRemoveTimer implements IPacketHandler<SampleSession> {
    private static final Logger logger = getLogger(CmdSessionAgentRemoveTimer.class);

    @Override
    public void execute(SampleSession sampleSession, Packet packet) throws SuspendExecution {
        try {
            logger.info("CmdSessionAgentRemoveTimer");
            ((SampleSessionNode) SampleSessionNode.getInstance()).removeTimer();
        } catch (Exception e) {
            logger.error("CmdSessionAgentRemoveTimer", e);
        }
    }
}
