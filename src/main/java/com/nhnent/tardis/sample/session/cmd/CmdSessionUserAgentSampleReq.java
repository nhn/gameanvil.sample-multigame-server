package com.nhnent.tardis.sample.session.cmd;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.SampleSessionUser;
import org.slf4j.Logger;

public class CmdSessionUserAgentSampleReq implements IPacketHandler<SampleSessionUser> {
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
