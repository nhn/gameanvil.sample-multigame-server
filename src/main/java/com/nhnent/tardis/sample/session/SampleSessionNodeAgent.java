package com.nhnent.tardis.sample.session;

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
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleSessionNodeAgent extends SessionNodeAgent implements ISessionNode,
    ITimerHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private PacketDispatcher packetDispatcher = new PacketDispatcher();

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
        logger.info("SampleSessionNodeAgent.onInit");
    }

    @Override
    public void onPrepare() throws SuspendExecution {
        logger.info("SampleSessionNodeAgent.onPrepare");
        setReady();
    }

    @Override
    public void onReady() throws SuspendExecution {
        logger.info("SampleSessionNodeAgent.onReady");
        setTimer(5, "Timer Event");
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("SampleSessionNodeAgent.onDispatch : {}",
            TardisIndexer.getMsgName(packet.getDescId(), packet.getMsgIndex()));
        packetDispatcher.dispatch(this, packet);
    }

    @Override
    public void onPause(PauseType type, Payload payload) throws SuspendExecution {
        logger.info("SampleSessionNodeAgent.onPause");
    }

    @Override
    public void onResume(Payload payload) throws SuspendExecution {
        logger.info("SampleSessionNodeAgent.onResume");
    }

    @Override
    public void onShutdown() throws SuspendExecution {
        logger.info("SampleSessionNodeAgent.onShutdown");
        removeTimer();
    }

    @Override
    public void onTimer(ITimerObject timerObject, Object arg) throws SuspendExecution {
        logger.info("SampleSessionNodeAgent.onTimer - message : {}", arg);
    }
}
