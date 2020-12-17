package com.nhn.gameanvil.sample.service;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketHandler;
import com.nhn.gameanvil.sample.protocol.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class _ResetSpot implements PacketHandler<SampleSpot> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(SampleSpot sampleSpot, Packet packet) throws SuspendExecution {
        try {
            Sample.ResetSpot resetSpot = Sample.ResetSpot.parseFrom(packet.getStream());
            logger.info("_ResetSpot - count : {}", resetSpot.getCount());
            sampleSpot.resetEventCount(resetSpot.getCount());
        } catch (Exception e) {
            logger.error("_ResetSpot::execute()", e);
        }
    }

}
