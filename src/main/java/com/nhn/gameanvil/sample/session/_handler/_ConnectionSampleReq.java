package com.nhn.gameanvil.sample.session._handler;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketHandler;
import com.nhn.gameanvil.sample.session.SampleConnection;
import com.nhn.gameanvil.sample.protocol.Sample;
import org.slf4j.Logger;

public class _ConnectionSampleReq implements PacketHandler<SampleConnection> {
    private static final Logger logger = getLogger(_ConnectionSampleReq.class);

    @Override
    public void execute(SampleConnection sampleConnection, Packet packet) throws SuspendExecution {
        try {
            String message = Sample.SampleReq.parseFrom(packet.getStream()).getMessage();
            logger.info("_ConnectionSampleReq : {}", message);
            sampleConnection.reply(new Packet(Sample.SampleRes.newBuilder().setMessage(message)));
        } catch (Exception e) {
            logger.error("_ConnectionSampleReq", e);
        }
    }
}
