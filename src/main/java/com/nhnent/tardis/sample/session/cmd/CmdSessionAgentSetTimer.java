package com.nhnent.tardis.sample.session.cmd;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.SampleSession;
import com.nhnent.tardis.sample.session.SampleSessionNode;
import java.io.IOException;
import org.slf4j.Logger;

public class CmdSessionAgentSetTimer implements IPacketHandler<SampleSession> {
    private static final Logger logger = getLogger(CmdSessionAgentSetTimer.class);

    @Override
    public void execute(SampleSession sampleSession, Packet packet) throws SuspendExecution {
        try {
            logger.info("CmdSessionAgentSetTimer");
            Sample.SetTimer msg = Sample.SetTimer.parseFrom(packet.getStream());
            ((SampleSessionNode) SampleSessionNode.getInstance()).setTimer(msg.getInterval(), msg.getMessage());
        } catch (IOException e) {
            logger.error("CmdSessionAgentSetTimer", e);
        }
    }
}