package com.nhnent.tardis.sample.space.chat;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.PauseType;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.console.space.*;
import org.slf4j.Logger;

public class ChatNode extends SpaceNodeAgent implements ISpaceNode {
    private static final Logger logger = getLogger(ChatNode.class);

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("ChatNode.onInit");
    }

    @Override
    public void onPrepare() throws SuspendExecution {
        logger.info("ChatNode.onPrepare");
        setReady();
    }

    @Override
    public void onReady() throws SuspendExecution {
        logger.info("ChatNode.onReady");
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("ChatNode.onDispatch : {}",
            packet.getMsgName());
    }

    @Override
    public void onPause(PauseType type, Payload payload) throws SuspendExecution {
        logger.info("ChatNode.onPause");
    }

    @Override
    public void onResume(Payload payload) throws SuspendExecution {
        logger.info("ChatNode.onResume");
    }

    @Override
    public void onShutdown() throws SuspendExecution {
        logger.info("ChatNode.onShutdown");
    }

    @Override
    public void onChannelUserUpdate(ChannelUpdateType type, IChannelUserInfo channelUserInfo, String userId) throws SuspendExecution {
        logger.info("ChatNode.onChannelUserUpdate - ChannelUpdateType : {}, UserId : {}", type, userId);
    }

    @Override
    public void onChannelRoomUpdate(ChannelUpdateType type, IRoomInfo channelRoomInfo, String roomId) throws SuspendExecution {
        logger.info("ChatNode.onChannelRoomUpdate - ChannelUpdateType : {}, RoomId : {}", type, roomId);
    }
}
