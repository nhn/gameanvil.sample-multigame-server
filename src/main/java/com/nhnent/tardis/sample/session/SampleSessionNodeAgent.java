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
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.handlers.SessionNodeAgentSetTimerPacketHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleSessionNodeAgent extends SessionNodeAgent implements ISessionNode,
    ITimerHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private PacketDispatcher packetDispatcher = new PacketDispatcher();

    private Map<String, ITimerObject> mapTimers = new HashMap();

    public boolean AddTimer(String timerId, int interval, int repeatCount, String message) {
        if (mapTimers.containsKey(timerId)) {
            return false;
        }

        ITimerObject timerObject = addTimer(
            interval,
            TimeUnit.MILLISECONDS,
            repeatCount,
            this,
            message
        );

        mapTimers.put(timerId, timerObject);
        return true;
    }

    public boolean RemoveTimer(String timerId) {
        if (!mapTimers.containsKey(timerId)) {
            return false;
        }
        ITimerObject timerObject = mapTimers.remove(timerId);
        removeTimer(timerObject);
        return true;
    }

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("SampleSessionNodeAgent.onInit");
        packetDispatcher.registerMsg(Sample.SetTimer.class, SessionNodeAgentSetTimerPacketHandler.class);
    }

    @Override
    public void onPrepare() throws SuspendExecution {
        logger.info("SampleSessionNodeAgent.onPrepare");
    }

    @Override
    public void onReady() throws SuspendExecution {
        logger.info("SampleSessionNodeAgent.onReady");
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
    }

    @Override
    public void onTimer(ITimerObject timerObject, Object arg) throws SuspendExecution {
        logger.info("SampleSessionNodeAgent.onTimer");

    }
}
