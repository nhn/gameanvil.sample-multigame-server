package com.nhn.gameanvil.sample.service;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketHandler;
import com.nhn.gameanvil.sample.protocol.Sample;
import org.slf4j.Logger;

public class _SampleToSpot implements PacketHandler<SampleSpot> {

    private static final Logger logger = getLogger(_SampleToSpot.class);

    @Override
    public void execute(SampleSpot sampleSpot, Packet packet) throws SuspendExecution {
        try {
            Sample.SampleToSpot sampleToSpot = Sample.SampleToSpot.parseFrom(packet.getStream());
            logger.info("_SampleToSpot - from : {}, msg : {}", sampleToSpot.getFrom(), sampleToSpot.getMessage());
            sampleSpot.onEvent(sampleToSpot.getFrom(), sampleToSpot.getMessage());
        } catch (Exception e) {
            logger.error("_SampleToSpot::execute()", e);
        }
    }

}
