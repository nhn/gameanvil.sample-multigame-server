package com.nhn.gameanvil.sample.service;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.node.support.BaseSupportNode;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.rest.RestObject;
import com.nhn.gameanvil.sample.Defines.StringValues;
import org.slf4j.Logger;

public class SampleSupportNode extends BaseSupportNode {
    private static final Logger logger = getLogger(SampleSupportNode.class);

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("SampleSupportNode.onInit");
        addTopic(StringValues.TopicService);
    }

    @Override
    public void onPrepare() throws SuspendExecution {
        logger.info("SampleSupportNode.onPrepare");
        setReady();
    }

    @Override
    public void onReady() throws SuspendExecution {
        logger.info("SampleSupportNode.onReady");
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("SampleSupportNode.onDispatch : {}",
            packet.getMsgName());
    }

    @Override
    public boolean onDispatch(RestObject restObject) throws SuspendExecution {
        logger.info("SampleSupportNode.onDispatch : {}", restObject.toString());
        return false;
    }

    @Override
    public void onPause(Payload payload) throws SuspendExecution {
        logger.info("SampleServiceNode.onPause");
    }

    @Override
    public void onResume(Payload outPayload) throws SuspendExecution {
        logger.info("SampleSupportNode.onResume");
    }

    @Override
    public void onShutdown() throws SuspendExecution {
        logger.info("SampleSupportNode.onShutdown");
    }
}
