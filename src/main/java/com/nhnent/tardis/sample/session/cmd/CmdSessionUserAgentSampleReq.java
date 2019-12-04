package com.nhnent.tardis.sample.session.cmd;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.SampleSessionUser;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdSessionUserAgentSampleReq implements IPacketHandler<SampleSessionUser> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(SampleSessionUser sampleSessionUser, Packet packet) throws SuspendExecution {
        try{
            String message = Sample.SampleReq.parseFrom(packet.getStream()).getMessage();
            logger.info("CmdSessionUserAgentSampleReq : {}", message);
            sampleSessionUser.reply(new Packet(Sample.SampleRes.newBuilder().setMessage(message)));
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
