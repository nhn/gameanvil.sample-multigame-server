package com.nhn.gameanvil.sample.session._handler;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketHandler;
import com.nhn.gameanvil.sample.session.SampleSession;
import com.nhn.gameanvil.sample.protocol.Sample;
import org.slf4j.Logger;

public class _SessionSampleReq implements PacketHandler<SampleSession> {
    private static final Logger logger = getLogger(_SessionSampleReq.class);

    @Override
    public void execute(SampleSession sampleSession, Packet packet) throws SuspendExecution {
        try {
            String message = Sample.SampleReq.parseFrom(packet.getStream()).getMessage();
            logger.info("_SessionSampleReq : {}", message);
            sampleSession.reply(new Packet(Sample.SampleRes.newBuilder().setMessage(message)));
        } catch (Exception e) {
            logger.error("_SessionSampleReq", e);
        }
    }
}
