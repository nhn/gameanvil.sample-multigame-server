package com.nhnent.tardis.sample.session;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.define.PauseType;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.timer.TimerHandler;
import com.nhn.gameanvil.timer.Timer;
import com.nhn.gameanvil.node.gateway.BaseGatewayNode;
import com.nhnent.tardis.sample.protocol.Sample;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

public class SampleGatewayNode extends BaseGatewayNode implements TimerHandler {

    private static final Logger logger = getLogger(SampleGatewayNode.class);
    private List<SampleConnection> sampleConnections = new LinkedList();

    public void addSampleSession(SampleConnection sampleConnection) {
        logger.info("SampleSessionNode.addSampleSession : {}", sampleConnection.getAccountId());
        sampleConnections.add(sampleConnection);
    }

    public void removeSampleSession(SampleConnection sampleConnection) {
        logger.info("SampleSessionNode.removeSampleSession : {}", sampleConnection.getAccountId());
        sampleConnections.remove(sampleConnection);
    }

    private Timer timerObject = null;

    public boolean setTimer(int interval, String message) {
        if (timerObject != null) {
            return false;
        }

        timerObject = addTimer(
            interval,
            TimeUnit.SECONDS,
            0,
            this,
            message
        );

        return true;
    }

    public boolean removeTimer() {
        if (timerObject == null) {
            return false;
        }
        removeTimer(timerObject);
        timerObject = null;
        return true;
    }

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("SampleSessionNode.onInit");
    }

    @Override
    public void onPrepare() throws SuspendExecution {
        logger.info("SampleSessionNode.onPrepare");
        setReady();
    }

    @Override
    public void onReady() throws SuspendExecution {
        logger.info("SampleSessionNode.onReady");
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("SampleSessionNode.onDispatch : {}",
            packet.getMsgName());
    }

    @Override
    public void onPause(PauseType type, Payload payload) throws SuspendExecution {
        logger.info("SampleSessionNode.onPause");
    }

    @Override
    public void onResume(Payload payload) throws SuspendExecution {
        logger.info("SampleSessionNode.onResume");
    }

    @Override
    public void onShutdown() throws SuspendExecution {
        logger.info("SampleSessionNode.onShutdown");
    }

    @Override
    public void onTimer(Timer timerObject, Object arg) throws SuspendExecution {
        logger.info("SampleSessionNode.onTimer - message : {}", arg);
        for (SampleConnection sampleConnection : sampleConnections) {
            sampleConnection.sendToClient(new Packet(Sample.SampleToC.newBuilder().setMessage((String) arg)));
        }
    }
}
