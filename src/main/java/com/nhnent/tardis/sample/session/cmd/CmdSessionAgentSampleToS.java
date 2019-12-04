package com.nhnent.tardis.sample.session.cmd;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.SampleSession;
import org.slf4j.Logger;

public class CmdSessionAgentSampleToS implements IPacketHandler<SampleSession> {
    private static final Logger logger = getLogger(CmdSessionAgentSampleToS.class);

    @Override
    public void execute(SampleSession sampleSession, Packet packet) throws SuspendExecution {
        try {
            String message = Sample.SampleToS.parseFrom(packet.getStream()).getMessage();
            logger.info("CmdSessionAgentSampleToS : {}", message);
            sampleSession.sendToClient(new Packet(Sample.SampleToC.newBuilder().setMessage(message)));
        } catch (Exception e) {
            logger.error("CmdSessionAgentSampleToS", e);
        }
    }
}
