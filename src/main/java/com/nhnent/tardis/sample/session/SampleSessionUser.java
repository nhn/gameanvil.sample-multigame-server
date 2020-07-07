package com.nhnent.tardis.sample.session;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.node.gateway.BaseSession;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketDispatcher;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.cmd.CmdSessionUserAgentSampleReq;
import org.slf4j.Logger;

public class SampleSessionUser extends BaseSession {

    private static final Logger logger = getLogger(SampleSessionUser.class);
    private static PacketDispatcher packetDispatcher = new PacketDispatcher();

    static {
        packetDispatcher.registerMsg(Sample.SampleReq.getDescriptor(), CmdSessionUserAgentSampleReq.class);
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("SampleSessionUser.onDispatch : {}",
            packet.getMsgName());
        packetDispatcher.dispatch(this, packet);
    }
}
