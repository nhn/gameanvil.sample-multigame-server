package com.nhnent.tardis.sample.session.cmd;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.SampleSessionUser;
import org.slf4j.Logger;

public class CmdSessionUserAgentSampleReq implements PacketHandler<SampleSessionUser> {
    private static final Logger logger = getLogger(CmdSessionUserAgentSampleReq.class);

    @Override
    public void execute(SampleSessionUser sampleSessionUser, Packet packet) throws SuspendExecution {
        try {
            String message = Sample.SampleReq.parseFrom(packet.getStream()).getMessage();
            logger.info("CmdSessionUserAgentSampleReq : {}", message);
            sampleSessionUser.reply(new Packet(Sample.SampleRes.newBuilder().setMessage(message)));
        } catch (Exception e) {
            logger.error("CmdSessionUserAgentSampleReq", e);
        }
    }
}
