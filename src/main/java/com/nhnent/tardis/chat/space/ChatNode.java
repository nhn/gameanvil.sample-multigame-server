package com.nhnent.tardis.chat.space;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.PauseType;
import com.nhnent.tardis.console.space.*;

public class ChatNode extends SpaceNodeAgent implements ISpaceNode  {
    @Override
    public void onInit() throws SuspendExecution {

    }

    @Override
    public void onPrepare() throws SuspendExecution {
        setReady();
    }

    @Override
    public void onReady() throws SuspendExecution {

    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {

    }

    @Override
    public void onPause(PauseType type, Payload payload) throws SuspendExecution {

    }

    @Override
    public void onResume(Payload payload) throws SuspendExecution {

    }

    @Override
    public void onShutdown() throws SuspendExecution {

    }

    @Override
    public void onChannelUserUpdate(ChannelUpdateType type, IChannelUserInfo channelUserInfo, String userId) throws SuspendExecution {

    }

    @Override
    public void onChannelRoomUpdate(ChannelUpdateType type, IRoomInfo channelRoomInfo, String roomId) throws SuspendExecution {

    }
}
