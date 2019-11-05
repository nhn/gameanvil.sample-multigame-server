package com.nhnent.tardis.sample.session.handlers;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.SampleSessionAgent;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionAgentSampleToSPacketHandler implements IPacketHandler<SampleSessionAgent> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(SampleSessionAgent sessionAgent, Packet packet) throws SuspendExecution {
        try{
            String message = Sample.SampleToS.parseFrom(packet.getStream()).getMessage();
            logger.info("SessionAgentSampleToSPacketHandler : {}", message);
            sessionAgent.reply(new Packet(Sample.SampleToC.newBuilder().setMessage(message)));
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
