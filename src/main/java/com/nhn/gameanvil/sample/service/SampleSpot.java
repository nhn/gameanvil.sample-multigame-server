package com.nhn.gameanvil.sample.service;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.node.support.BaseSpot;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketDispatcher;
import com.nhn.gameanvil.rest.RestObject;
import com.nhn.gameanvil.serializer.TransferPack;
import com.nhn.gameanvil.sample.Defines.StringValues;
import com.nhn.gameanvil.sample.protocol.Sample;
import org.slf4j.Logger;

public class SampleSpot extends BaseSpot {

    private static final Logger logger = getLogger(SampleSpot.class);

    private static PacketDispatcher packetDispatcher = new PacketDispatcher();

    static {
        packetDispatcher.registerMsg(Sample.SampleToSpot.getDescriptor(), _SampleToSpot.class);
        packetDispatcher.registerMsg(Sample.ResetSpot.getDescriptor(), _ResetSpot.class);
    }

    private int count;
    private int eventCount;


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
    public void onPause() throws SuspendExecution {
        logger.info("SampleSpot.onPause");
    }

    @Override
    public void onResume() throws SuspendExecution {
        logger.info("SampleSpot.onResume");
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("SampleSpot.onDispatch : {}", packet.getMsgName());
        if (packetDispatcher.isRegisteredMessage(packet)) {
            packetDispatcher.dispatch(this, packet);
        }
    }

    @Override
    public boolean onDispatch(RestObject restObject) throws SuspendExecution {
        return false;
    }

    @Override
    public void onTransferOut(TransferPack transferPack) throws SuspendExecution {
        logger.info("SampleSpot.onTransferOut");
    }

    @Override
    public void onTransferIn(final TransferPack transferPack) throws SuspendExecution {
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
