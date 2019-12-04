package com.nhnent.tardis.sample.session;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.PacketDispatcher;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.console.session.ISessionUser;
import com.nhnent.tardis.console.session.SessionUserAgent;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.cmd.CmdSessionUserAgentSampleReq;
import org.slf4j.Logger;

public class SampleSessionUser extends SessionUserAgent implements ISessionUser {

    private static final Logger logger = getLogger(SampleSessionUser.class);
    private static PacketDispatcher packetDispatcher = new PacketDispatcher();

    static {
        packetDispatcher.registerMsg(Sample.SampleReq.class, CmdSessionUserAgentSampleReq.class);
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("SampleSessionUser.onDispatch : {}",
            TardisIndexer.getMsgName(packet.getDescId(), packet.getMsgIndex()));
        packetDispatcher.dispatch(this, packet);
    }
}
