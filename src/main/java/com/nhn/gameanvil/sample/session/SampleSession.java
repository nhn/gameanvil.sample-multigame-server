package com.nhn.gameanvil.sample.session;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.node.gateway.BaseSession;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketDispatcher;
import com.nhn.gameanvil.sample.protocol.Sample;
import com.nhn.gameanvil.sample.session._handler._SessionSampleReq;
import org.slf4j.Logger;

public class SampleSession extends BaseSession {

    private static final Logger logger = getLogger(SampleSession.class);
    private static PacketDispatcher packetDispatcher = new PacketDispatcher();

    static {
        packetDispatcher.registerMsg(Sample.SampleReq.getDescriptor(), _SessionSampleReq.class);
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("SampleSession.onDispatch : {}",
            packet.getMsgName());
        packetDispatcher.dispatch(this, packet);
    }
}
