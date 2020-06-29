package com.nhnent.tardis.sample.session.cmd;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameflex.packet.Packet;
import com.nhn.gameflex.packet.PacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.SampleConnection;
import org.slf4j.Logger;

public class CmdSessionAgentSampleReq implements PacketHandler<SampleConnection> {
    private static final Logger logger = getLogger(CmdSessionAgentSampleReq.class);

    @Override
    public void execute(SampleConnection sampleConnection, Packet packet) throws SuspendExecution {
        try {
            String message = Sample.SampleReq.parseFrom(packet.getStream()).getMessage();
            logger.info("CmdSessionAgentSampleReq : {}", message);
            sampleConnection.reply(new Packet(Sample.SampleRes.newBuilder().setMessage(message)));
        } catch (Exception e) {
            logger.error("CmdSessionAgentSampleReq", e);
        }
    }
}
