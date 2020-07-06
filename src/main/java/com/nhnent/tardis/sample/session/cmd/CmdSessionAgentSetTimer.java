package com.nhnent.tardis.sample.session.cmd;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.SampleConnection;
import com.nhnent.tardis.sample.session.SampleGatewayNode;
import java.io.IOException;
import org.slf4j.Logger;

public class CmdSessionAgentSetTimer implements PacketHandler<SampleConnection> {
    private static final Logger logger = getLogger(CmdSessionAgentSetTimer.class);

    @Override
    public void execute(SampleConnection sampleConnection, Packet packet) throws SuspendExecution {
        try {
            logger.info("CmdSessionAgentSetTimer");
            Sample.SetTimer msg = Sample.SetTimer.parseFrom(packet.getStream());
            ((SampleGatewayNode) SampleGatewayNode.getInstance()).setTimer(msg.getInterval(), msg.getMessage());
        } catch (IOException e) {
            logger.error("CmdSessionAgentSetTimer", e);
        }
    }
}