package com.nhnent.tardis.sample.service;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameflex.define.PauseType;
import com.nhn.gameflex.node.support.BaseSupportNode;
import com.nhn.gameflex.packet.Packet;
import com.nhn.gameflex.packet.Payload;
import com.nhn.gameflex.rest.RestObject;
import com.nhnent.tardis.sample.Defines.StringValues;
import org.slf4j.Logger;

public class SampleServiceNode extends BaseSupportNode {
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
            packet.getMsgName());
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
