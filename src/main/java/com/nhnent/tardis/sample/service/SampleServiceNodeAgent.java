package com.nhnent.tardis.sample.service;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.ITimerHandler;
import com.nhnent.tardis.common.internal.ITimerObject;
import com.nhnent.tardis.common.internal.PauseType;
import com.nhnent.tardis.common.internal.RestObject;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.console.service.IServiceNode;
import com.nhnent.tardis.console.service.ServiceNodeAgent;
import com.nhnent.tardis.sample.Defines.StringValues;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.space.internal.SpaceNode;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleServiceNodeAgent extends ServiceNodeAgent implements IServiceNode{
    private Logger logger = LoggerFactory.getLogger(getClass());
    private int count = 0;
    @Override
    public void onInit() throws SuspendExecution {
        logger.info("SampleServiceNodeAgent.onInit");
    }

    @Override
    public void onPrepare() throws SuspendExecution {
        logger.info("SampleServiceNodeAgent.onPrepare");
        setReady();
    }

    @Override
    public void onReady() throws SuspendExecution {
        logger.info("SampleServiceNodeAgent.onReady");
        count = 0;
        addTimer(1, TimeUnit.SECONDS, 0, new ITimerHandler() {
            @Override
            public void onTimer(ITimerObject timerObject, Object arg) throws SuspendExecution {
                logger.info("SampleServiceNodeAgent.onTimer");
                String msg = "Message from SampleServiceNodeAgent : " + count++;
                publishToClient(StringValues.ChatServiceName, new Packet(Sample.SampleToC.newBuilder().setMessage(msg)));
            }
        },this);
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("SampleServiceNodeAgent.onDispatch : {}",
            TardisIndexer.getMsgName(packet.getDescId(), packet.getMsgIndex()));
    }

    @Override
    public boolean onDispatch(RestObject restObject) throws SuspendExecution {
        logger.info("SampleServiceNodeAgent.onDispatch : {}", restObject.toString());
        return false;
    }

    @Override
    public void onPause(PauseType type, Payload payload) throws SuspendExecution {
        logger.info("SampleServiceNodeAgent.onPause");
    }

    @Override
    public void onResume(Payload outPayload) throws SuspendExecution {
        logger.info("SampleServiceNodeAgent.onResume");
    }

    @Override
    public void onShutdown() throws SuspendExecution {
        logger.info("SampleServiceNodeAgent.onShutdown");
        removeAllTimer();
    }
}
