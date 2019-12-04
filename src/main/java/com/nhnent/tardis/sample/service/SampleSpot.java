package com.nhnent.tardis.sample.service;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.internal.PauseType;
import com.nhnent.tardis.common.internal.RestObject;
import com.nhnent.tardis.console.PacketDispatcher;
import com.nhnent.tardis.console.service.ISpot;
import com.nhnent.tardis.console.service.SpotAgent;
import com.nhnent.tardis.sample.Defines.StringValues;
import com.nhnent.tardis.sample.protocol.Sample;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleSpot extends SpotAgent implements ISpot {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static PacketDispatcher dispatcher = new PacketDispatcher();

    private int count;
    private int eventCount;

    static {
        dispatcher.registerMsg(Sample.SampleToSpot.class, CmdSampleToSpot.class);
        dispatcher.registerMsg(Sample.ResetSpot.class, CmdResetSpot.class);
    }

    @Override
    public boolean onInit() throws SuspendExecution {
        logger.info("SampleSpot.onInit - {}", getId());
        //addTopic(StringValues.TopicSpot);
        resetEventCount(0);
        return true;
    }

    @Override
    public void onDestroy() throws SuspendExecution {
        logger.info("SampleSpot.onDestroy");
    }

    @Override
    public void onPause(PauseType type) throws SuspendExecution {
        logger.info("SampleSpot.onPause - type :", type);
    }

    @Override
    public void onResume() throws SuspendExecution {
        logger.info("SampleSpot.onResume");
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("SampleSpot.onDispatch : {}", packet.getMsgName());
        if (dispatcher.isRegisteredMessage(packet)) {
            dispatcher.dispatch(this, packet);
        }
    }

    @Override
    public boolean onDispatch(RestObject restObject) throws SuspendExecution {
        return false;
    }

    @Override
    public ByteBuffer onTransferOut() throws SuspendExecution {
        logger.info("SampleSpot.onTransferOut");
        return null;
    }

    @Override
    public void onTransferIn(final InputStream inputStream) throws SuspendExecution {
        logger.info("SampleSpot.onTransferIn");
    }

    public void resetEventCount(int resetCount) {
        logger.info("SampleSpot.resetEventCount - count : {}", resetCount);
        count = 0;
        eventCount = resetCount;
    }

    public void onEvent(String from, String message) throws SuspendExecution {
        if (eventCount <= 0) {
            return;
        }
        count++;
        if (count == eventCount) {
            logger.info("SampleSpot.onEvent - Event!!");
            publishToClient(StringValues.TopicSpot, new Packet(Sample.SampleToC.newBuilder().setMessage(String.format("[Event : %s] %s ", from, message))));
            resetEventCount(eventCount);
            publishToClient(StringValues.TopicSpot, new Packet(Sample.SampleToC.newBuilder().setMessage(String.format("[Event] Next Event will be on count %d ", eventCount))));
        }
    }
}
