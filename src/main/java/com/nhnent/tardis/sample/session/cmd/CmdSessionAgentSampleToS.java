package com.nhnent.tardis.sample.session.cmd;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.SampleConnection;
import org.slf4j.Logger;

public class CmdSessionAgentSampleToS implements PacketHandler<SampleConnection> {
    private static final Logger logger = getLogger(CmdSessionAgentSampleToS.class);

    @Override
    public void execute(SampleConnection sampleConnection, Packet packet) throws SuspendExecution {
        try {
            String message = Sample.SampleToS.parseFrom(packet.getStream()).getMessage();
            logger.info("CmdSessionAgentSampleToS : {}", message);
            sampleConnection.sendToClient(new Packet(Sample.SampleToC.newBuilder().setMessage(message)));
        } catch (Exception e) {
            logger.error("CmdSessionAgentSampleToS", e);
        }
    }
}
