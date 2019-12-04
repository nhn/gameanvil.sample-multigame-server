package com.nhnent.tardis.sample.session.cmd;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.session.SampleSession;
import com.nhnent.tardis.sample.session.SampleSessionNode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdSessionAgentRemoveTimer implements IPacketHandler<SampleSession> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(SampleSession sampleSession, Packet packet) throws SuspendExecution {
        try {
            logger.info("CmdSessionAgentRemoveTimer");
            ((SampleSessionNode) SampleSessionNode.getInstance()).removeTimer();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
