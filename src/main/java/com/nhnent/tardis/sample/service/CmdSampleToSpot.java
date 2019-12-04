package com.nhnent.tardis.sample.service;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import org.slf4j.Logger;

public class CmdSampleToSpot implements IPacketHandler<SampleSpot> {

    private static final Logger logger = getLogger(CmdSampleToSpot.class);

    @Override
    public void execute(SampleSpot sampleSpot, Packet packet) throws SuspendExecution {
        try {
            Sample.SampleToSpot sampleToSpot = Sample.SampleToSpot.parseFrom(packet.getStream());
            logger.info("CmdSampleToSpot - from : {}, msg : {}", sampleToSpot.getFrom(), sampleToSpot.getMessage());
            sampleSpot.onEvent(sampleToSpot.getFrom(), sampleToSpot.getMessage());
        } catch (Exception e) {
            logger.error("CmdSampleToSpot::execute()", e);
        }
    }

}
