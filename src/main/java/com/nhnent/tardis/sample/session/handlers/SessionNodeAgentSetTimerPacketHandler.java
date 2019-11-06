package com.nhnent.tardis.sample.session.handlers;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.internal.ITimerObject;
import com.nhnent.tardis.common.internal.TimerObject;
import com.nhnent.tardis.common.protocol.Internal;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.SampleSessionNodeAgent;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionNodeAgentSetTimerPacketHandler implements
    IPacketHandler<SampleSessionNodeAgent> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public void execute(SampleSessionNodeAgent sampleSessionNodeAgent, Packet packet) throws SuspendExecution {
        try {
            Sample.SetTimer sendMsg = Sample.SetTimer.parseFrom(packet.getStream());

            ITimerObject timerObject = sampleSessionNodeAgent.addTimer(
                sendMsg.getInterval(),
                TimeUnit.MILLISECONDS,
                sendMsg.getRepeatCount(),
                sampleSessionNodeAgent,
                null
            );



        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
