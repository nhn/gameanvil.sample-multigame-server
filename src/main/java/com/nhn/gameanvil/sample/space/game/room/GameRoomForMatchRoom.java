package com.nhn.gameanvil.sample.space.game.room;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.sample.protocol.Sample;
import com.nhn.gameanvil.sample.space.game.match.GameRoomMatchInfo;
import com.nhn.gameanvil.sample.space.game.user.GameUser;
import org.slf4j.Logger;

public class GameRoomForMatchRoom extends GameRoom {
    private static final Logger logger = getLogger(GameRoomForMatchRoom.class);
    private GameRoomMatchInfo gameRoomMatchInfo = new GameRoomMatchInfo();

    @Override
    public boolean onCreateRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("GameRoomForMatchRoom.onCreateRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        try {
            users.put(gameUser.getUserId(), gameUser);

            gameRoomMatchInfo.setRoomId(getId());
            gameRoomMatchInfo.setUserCountCurr(users.size());
            updateRoomMatchInfo(gameRoomMatchInfo);

            return true;
        } catch (Exception e) {
            logger.error("GameRoomForMatchRoom::onCreateRoom()", e);
            return false;
        }
    }

    @Override
    public boolean onJoinRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("GameRoomForMatchRoom.onJoinRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        try {
            logger.info("GameRoomForMatchRoom.onJoinRoom - roomMatchMaking");
            String message = String.format("%s is join", gameUser.getUserId());
            for (GameUser user : users.values()) {
                user.send(new Packet(Sample.GameMessageToC.newBuilder().setMessage(message)));
                logger.info("GameRoomForMatchRoom.onJoinRoom - to {} : {}", user.getUserId(), message);
            }

            users.put(gameUser.getUserId(), gameUser);

            gameRoomMatchInfo.setUserCountCurr(users.size());
            updateRoomMatchInfo(gameRoomMatchInfo);

            return true;
        } catch (Exception e) {
            users.remove(gameUser.getUserId());

            gameRoomMatchInfo.setUserCountCurr(users.size());
            logger.error("GameRoomForMatchRoom::onJoinRoom()", e);
            return false;
        }
    }

    @Override
    public boolean onLeaveRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("GameRoomForMatchRoom.onLeaveRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        try {
            users.remove(gameUser.getUserId());
            gameRoomMatchInfo.setUserCountCurr(users.size());
            updateRoomMatchInfo(gameRoomMatchInfo);
            return true;
        } catch (Exception e) {
            users.put(gameUser.getUserId(), gameUser);
            gameRoomMatchInfo.setUserCountCurr(users.size());
            return false;
        }
    }


}

