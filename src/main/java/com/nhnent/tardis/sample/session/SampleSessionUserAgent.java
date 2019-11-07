package com.nhnent.tardis.sample.session;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.PacketDispatcher;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.console.session.ISessionUser;
import com.nhnent.tardis.console.session.SessionUserAgent;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.handlers.SessionUserAgentSampleReqPacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleSessionUserAgent extends SessionUserAgent implements ISessionUser {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static PacketDispatcher dispatcher = new PacketDispatcher();
    static {
        dispatcher.registerMsg(Sample.SampleReq.class, SessionUserAgentSampleReqPacketHandler.class);
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("SampleSessionUserAgent.onDispatch : {}",
            TardisIndexer.getMsgName(packet.getDescId(), packet.getMsgIndex()));
        dispatcher.dispatch(this, packet);
    }
}
