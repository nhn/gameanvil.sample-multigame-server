package com.nhnent.tardis.sample.space.game.room;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.space.game.match.GameUserMatchInfo;
import com.nhnent.tardis.sample.space.game.user.GameUser;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class GameRoomMatchUserParty extends GameRoom {
    private  boolean isRefill = false;
    private static final int matchSize = 4;

    @Override
    public boolean onCreateRoom(
        GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("GameRoomMatchUserParty.onCreateRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        try{
            users.put(gameUser.getUserId(), gameUser);
            return true;
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    @Override
    public boolean onJoinRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("GameRoomMatchUserParty.onJoinRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        try{
            users.put(gameUser.getUserId(), gameUser);
            logger.info("GameRoomMatchUserParty.onJoinRoom - userMatchMaking");
            if(isRefill){
                if(isRefill){
                    logger.info("GameRoomMatchUserParty.onJoinRoom - roomMatchMaking");
                    String message = String.format("%s is join",gameUser.getUserId());
                    for(GameUser user:users.values()){
                        if(gameUser.getUserId().equals(user.getUserId()))
                            continue;

                        user.send(new Packet(Sample.GameMessageToC.newBuilder().setMessage(message)));
                        logger.info("GameRoomMatchUserParty.onJoinRoom - to {} : {}",user.getUserId(), message);
                    }

                    if(users.size() == matchSize){
                        isRefill = false;
                    }
                }
            }else{
                if(users.size() == matchSize){
                    logger.info("GameRoomMatchUserParty.onJoinRoom - userMatchMaking completed");
                    for(GameUser user:users.values()) {
                        String message = String.format("%s is join",user.getUserId());
                        for(GameUser to:users.values()){
                            if(to.getUserId().equals(user.getUserId()))
                                continue;

                            to.send(new Packet(Sample.GameMessageToC.newBuilder().setMessage(message)));
                            logger.info("GameRoomMatchUserParty.onJoinRoom - to {} : {}",to.getUserId(), message);
                        }
                    }
                }
            }
            return true;
        }catch (Exception e){
            users.remove(gameUser.getUserId());
            logger.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    @Override
    public boolean onLeaveRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("GameRoomMatchUserParty.onLeaveRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        try{
            users.remove(gameUser.getUserId());

            // MatchUser로 생성된 방인경우 matchRefill()을 사용해 결원을 채울 수 있음.
            if (isMadeByUserMatchMaker()) {

                try {
                    // Refill() 정보 등록
                    logger.info("GameRoomMatchUserParty.onLeaveRoom - refill");
                    isRefill = true;
                    if (!matchRefill(new GameUserMatchInfo("Refill", 100, 1))) {
                        logger.warn("MatchRefill for the room({}) failure!", getId());
                    }
                } catch (Exception e) {
                    isRefill = false;
                    logger.error(ExceptionUtils.getStackTrace(e));
                }
            }
            return true;
        }catch(Exception e){
            users.put(gameUser.getUserId(), gameUser);
            return false;
        }
    }
}
