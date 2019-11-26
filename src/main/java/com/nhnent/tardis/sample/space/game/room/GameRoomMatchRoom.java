package com.nhnent.tardis.sample.space.game.room;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.space.game.match.GameRoomMatchInfo;
import com.nhnent.tardis.sample.space.game.user.GameUser;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class GameRoomMatchRoom extends GameRoom {
    private GameRoomMatchInfo gameRoomMatchInfo = new GameRoomMatchInfo();

    @Override
    public boolean onCreateRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("GameRoomMatchRoom.onCreateRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        try{
            users.put(gameUser.getUserId(), gameUser);

            gameRoomMatchInfo.setRoomId(getId());
            gameRoomMatchInfo.setUserCountCurr(users.size());
            updateRoomMatchInfo(gameRoomMatchInfo);

            return true;
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    @Override
    public boolean onJoinRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("GameRoomMatchRoom.onJoinRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        try{
            logger.info("GameRoomMatchRoom.onJoinRoom - roomMatchMaking");
            String message = String.format("%s is join",gameUser.getUserId());
            for(GameUser user:users.values()){
                user.send(new Packet(Sample.GameMessageToC.newBuilder().setMessage(message)));
                logger.info("GameRoomMatchRoom.onJoinRoom - to {} : {}",user.getUserId(), message);
            }

            users.put(gameUser.getUserId(), gameUser);

            gameRoomMatchInfo.setUserCountCurr(users.size());
            updateRoomMatchInfo(gameRoomMatchInfo);

            return true;
        }catch (Exception e){
            users.remove(gameUser.getUserId());

            gameRoomMatchInfo.setUserCountCurr(users.size());
            logger.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    @Override
    public boolean onLeaveRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("GameRoomMatchRoom.onLeaveRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        try{
            users.remove(gameUser.getUserId());
            gameRoomMatchInfo.setUserCountCurr(users.size());
            updateRoomMatchInfo(gameRoomMatchInfo);
            return true;
        }catch(Exception e){
            users.put(gameUser.getUserId(), gameUser);
            gameRoomMatchInfo.setUserCountCurr(users.size());
            return false;
        }
    }



}
