package com.nhnent.tardis.sample.session;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.PacketDispatcher;
import com.nhnent.tardis.console.session.ISessionUser;
import com.nhnent.tardis.console.session.SessionUserAgent;

public class SampleSessionUserAgent extends SessionUserAgent implements ISessionUser {
    private static PacketDispatcher dispatcher = new PacketDispatcher();
    static {

    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        dispatcher.dispatch(this, packet);
    }
}