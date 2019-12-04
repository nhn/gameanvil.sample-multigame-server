package com.nhnent.tardis.sample.session;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.ITimerHandler;
import com.nhnent.tardis.common.internal.ITimerObject;
import com.nhnent.tardis.common.internal.PauseType;
import com.nhnent.tardis.console.PacketDispatcher;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.console.session.ISessionNode;
import com.nhnent.tardis.console.session.SessionNodeAgent;
import com.nhnent.tardis.sample.protocol.Sample;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

public class SampleSessionNode extends SessionNodeAgent implements ISessionNode, ITimerHandler {

    private static final Logger logger = getLogger(SampleSessionNode.class);
    private List<SampleSession> sampleSessions = new LinkedList();

    public void addSampleSession(SampleSession sampleSession) {
        logger.info("SampleSessionNode.addSampleSession : {}", sampleSession.getAccountId());
        sampleSessions.add(sampleSession);
    }

    public void removeSampleSession(SampleSession sampleSession) {
        logger.info("SampleSessionNode.removeSampleSession : {}", sampleSession.getAccountId());
        sampleSessions.remove(sampleSession);
    }

    private ITimerObject timerObject = null;

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
            TardisIndexer.getMsgName(packet.getDescId(), packet.getMsgIndex()));
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
    public void onTimer(ITimerObject timerObject, Object arg) throws SuspendExecution {
        logger.info("SampleSessionNode.onTimer - message : {}", arg);
        for (SampleSession sampleSession : sampleSessions) {
            sampleSession.sendToClient(new Packet(Sample.SampleToC.newBuilder().setMessage((String) arg)));
        }
    }
}
