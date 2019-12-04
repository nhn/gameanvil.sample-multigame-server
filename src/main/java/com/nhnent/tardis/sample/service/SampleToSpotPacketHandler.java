package com.nhnent.tardis.sample.service;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleToSpotPacketHandler implements IPacketHandler<SampleSpot> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(SampleSpot sampleSpot, Packet packet) throws SuspendExecution {
        try {
            Sample.SampleToSpot sampleToSpot = Sample.SampleToSpot.parseFrom(packet.getStream());
            logger.info("SampleToSpotPacketHandler - from : {}, msg : {}", sampleToSpot.getFrom(), sampleToSpot.getMessage());
            sampleSpot.onEvent(sampleToSpot.getFrom(), sampleToSpot.getMessage());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

}
