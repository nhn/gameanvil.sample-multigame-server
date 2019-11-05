package com.nhnent.tardis.sample.session.handlers;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.SampleSessionUserAgent;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleReqSessionUserAgentPacketHandler implements IPacketHandler<SampleSessionUserAgent> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(SampleSessionUserAgent sessionUserAgent, Packet packet) throws SuspendExecution {
        try{
            String message = Sample.SampleReq.parseFrom(packet.getStream()).getMessage();
            logger.info("SampleReqSessionUserAgentPacketHandler : {}", message);
            sessionUserAgent.reply(new Packet(Sample.SampleRes.newBuilder().setMessage(message)));
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
