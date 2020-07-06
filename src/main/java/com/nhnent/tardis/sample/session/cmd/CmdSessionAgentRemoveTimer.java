package com.nhnent.tardis.sample.session.cmd;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketHandler;
import com.nhnent.tardis.sample.session.SampleConnection;
import com.nhnent.tardis.sample.session.SampleGatewayNode;
import org.slf4j.Logger;

public class CmdSessionAgentRemoveTimer implements PacketHandler<SampleConnection> {
    private static final Logger logger = getLogger(CmdSessionAgentRemoveTimer.class);

    @Override
    public void execute(SampleConnection sampleConnection, Packet packet) throws SuspendExecution {
        try {
            logger.info("CmdSessionAgentRemoveTimer");
            ((SampleGatewayNode) SampleGatewayNode.getInstance()).removeTimer();
        } catch (Exception e) {
            logger.error("CmdSessionAgentRemoveTimer", e);
        }
    }
}
