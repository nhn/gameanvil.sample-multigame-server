package com.nhnent.tardis.sample.service;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameflex.packet.Packet;
import com.nhn.gameflex.packet.PacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdResetSpot implements PacketHandler<SampleSpot> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(SampleSpot sampleSpot, Packet packet) throws SuspendExecution {
        try {
            Sample.ResetSpot resetSpot = Sample.ResetSpot.parseFrom(packet.getStream());
            logger.info("CmdResetSpot - count : {}", resetSpot.getCount());
            sampleSpot.resetEventCount(resetSpot.getCount());
        } catch (Exception e) {
            logger.error("CmdResetSpot::execute()", e);
        }
    }

}
