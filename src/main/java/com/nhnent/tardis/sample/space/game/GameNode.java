package com.nhnent.tardis.sample.space.game;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.PauseType;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.console.space.ChannelUpdateType;
import com.nhnent.tardis.console.space.IChannelUserInfo;
import com.nhnent.tardis.console.space.IRoomInfo;
import com.nhnent.tardis.console.space.ISpaceNode;
import com.nhnent.tardis.console.space.SpaceNodeAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameNode extends SpaceNodeAgent implements ISpaceNode {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("GameNode.onInit");
    }

    @Override
    public void onPrepare() throws SuspendExecution {
        logger.info("GameNode.onPrepare");
        setReady();
    }

    @Override
    public void onReady() throws SuspendExecution {
        logger.info("GameNode.onReady");
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("GameNode.onDispatch : {}",
            TardisIndexer.getMsgName(packet.getDescId(), packet.getMsgIndex()));
    }

    @Override
    public void onPause(PauseType type, Payload payload) throws SuspendExecution {
        logger.info("GameNode.onPause");
    }

    @Override
    public void onResume(Payload payload) throws SuspendExecution {
        logger.info("GameNode.onResume");
    }

    @Override
    public void onShutdown() throws SuspendExecution {
        logger.info("GameNode.onShutdown");
    }

    @Override
    public void onChannelUserUpdate(ChannelUpdateType type, IChannelUserInfo channelUserInfo,
        String userId) throws SuspendExecution {
        logger.info("GameNode.onChannelUserUpdate - ChannelUpdateType : {}, UserId : {}", type,
            userId);
    }

    @Override
    public void onChannelRoomUpdate(ChannelUpdateType type, IRoomInfo channelRoomInfo,
        String roomId) throws SuspendExecution {
        logger.info("GameNode.onChannelRoomUpdate - ChannelUpdateType : {}, RoomId : {}", type,
            roomId);
    }
}
