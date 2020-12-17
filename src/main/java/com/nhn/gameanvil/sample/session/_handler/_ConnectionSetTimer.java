package com.nhn.gameanvil.sample.session._handler;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketHandler;
import com.nhn.gameanvil.sample.session.SampleConnection;
import com.nhn.gameanvil.sample.session.SampleGatewayNode;
import com.nhn.gameanvil.sample.protocol.Sample;
import java.io.IOException;
import org.slf4j.Logger;

public class _ConnectionSetTimer implements PacketHandler<SampleConnection> {
    private static final Logger logger = getLogger(_ConnectionSetTimer.class);

    @Override
    public void execute(SampleConnection sampleConnection, Packet packet) throws SuspendExecution {
        try {
            logger.info("_ConnectionSetTimer");
            Sample.SetTimer msg = Sample.SetTimer.parseFrom(packet.getStream());
            ((SampleGatewayNode) SampleGatewayNode.getInstance()).setTimer(msg.getInterval(), msg.getMessage());
        } catch (IOException e) {
            logger.error("_ConnectionSetTimer", e);
        }
    }
}