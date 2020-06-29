package com.nhnent.tardis.sample.space.game;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameflex.define.PauseType;
import com.nhn.gameflex.node.game.BaseGameNode;
import com.nhn.gameflex.node.game.data.ChannelUpdateType;
import com.nhn.gameflex.node.game.data.ChannelUserInfo;
import com.nhn.gameflex.node.game.data.RoomInfo;
import com.nhn.gameflex.packet.Packet;
import com.nhn.gameflex.packet.Payload;
import org.slf4j.Logger;

public class GameNode extends BaseGameNode {
    private static final Logger logger = getLogger(GameNode.class);

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
            packet.getMsgName());
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
    public void onChannelUserUpdate(ChannelUpdateType type, ChannelUserInfo channelUserInfo, int userId, String accountId) throws SuspendExecution {
        logger.info("GameNode.onChannelUserUpdate - ChannelUpdateType : {}, UserId : {}, AccountId : {}", type, userId, accountId);
    }

    @Override
    public void onChannelRoomUpdate(ChannelUpdateType type, RoomInfo channelRoomInfo, int roomId) throws SuspendExecution {
        logger.info("GameNode.onChannelRoomUpdate - ChannelUpdateType : {}, RoomId : {}", type, roomId);
    }

    @Override
    public void onChannelInfo(Payload outPayload) throws SuspendExecution {

    }
}
