package com.nhnent.tardis.sample.service;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.PauseType;
import com.nhnent.tardis.common.internal.RestObject;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.console.service.IServiceNode;
import com.nhnent.tardis.console.service.ServiceNodeAgent;
import com.nhnent.tardis.sample.Defines.StringValues;
import org.slf4j.Logger;

public class SampleServiceNode extends ServiceNodeAgent implements IServiceNode{
    private static final Logger logger = getLogger(SampleServiceNode.class);

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("SampleServiceNode.onInit");
        addTopic(StringValues.TopicService);
    }

    @Override
    public void onPrepare() throws SuspendExecution {
        logger.info("SampleServiceNode.onPrepare");
        setReady();
    }

    @Override
    public void onReady() throws SuspendExecution {
        logger.info("SampleServiceNode.onReady");
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("SampleServiceNode.onDispatch : {}",
            TardisIndexer.getMsgName(packet.getDescId(), packet.getMsgIndex()));
    }

    @Override
    public boolean onDispatch(RestObject restObject) throws SuspendExecution {
        logger.info("SampleServiceNode.onDispatch : {}", restObject.toString());
        return false;
    }

    @Override
    public void onPause(PauseType type, Payload payload) throws SuspendExecution {
        logger.info("SampleServiceNode.onPause");
    }

    @Override
    public void onResume(Payload outPayload) throws SuspendExecution {
        logger.info("SampleServiceNode.onResume");
    }

    @Override
    public void onShutdown() throws SuspendExecution {
        logger.info("SampleServiceNode.onShutdown");
    }
}
