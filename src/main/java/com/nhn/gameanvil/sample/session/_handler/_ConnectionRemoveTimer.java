package com.nhn.gameanvil.sample.session._handler;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketHandler;
import com.nhn.gameanvil.sample.session.SampleConnection;
import com.nhn.gameanvil.sample.session.SampleGatewayNode;
import org.slf4j.Logger;

public class _ConnectionRemoveTimer implements PacketHandler<SampleConnection> {
    private static final Logger logger = getLogger(_ConnectionRemoveTimer.class);

    @Override
    public void execute(SampleConnection sampleConnection, Packet packet) throws SuspendExecution {
        try {
            logger.info("_ConnectionRemoveTimer");
            ((SampleGatewayNode) SampleGatewayNode.getInstance()).removeTimer();
        } catch (Exception e) {
            logger.error("_ConnectionRemoveTimer", e);
        }
    }
}
