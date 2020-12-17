package com.nhn.gameanvil.sample.session._handler;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketHandler;
import com.nhn.gameanvil.sample.session.SampleConnection;
import com.nhn.gameanvil.sample.protocol.Sample;
import org.slf4j.Logger;

public class _ConnectionSampleToS implements PacketHandler<SampleConnection> {
    private static final Logger logger = getLogger(_ConnectionSampleToS.class);

    @Override
    public void execute(SampleConnection sampleConnection, Packet packet) throws SuspendExecution {
        try {
            String message = Sample.SampleToS.parseFrom(packet.getStream()).getMessage();
            logger.info("_ConnectionSampleToS : {}", message);
            sampleConnection.sendToClient(new Packet(Sample.SampleToC.newBuilder().setMessage(message)));
        } catch (Exception e) {
            logger.error("_ConnectionSampleToS", e);
        }
    }
}
