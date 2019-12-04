package com.nhnent.tardis.sample.session.cmd;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.SampleSession;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

public class CmdSessionAgentSampleReq implements IPacketHandler<SampleSession> {
    private static final Logger logger = getLogger(CmdSessionAgentSampleReq.class);

    @Override
    public void execute(SampleSession sampleSession, Packet packet) throws SuspendExecution {
        try {
            String message = Sample.SampleReq.parseFrom(packet.getStream()).getMessage();
            logger.info("CmdSessionAgentSampleReq : {}", message);
            sampleSession.reply(new Packet(Sample.SampleRes.newBuilder().setMessage(message)));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
