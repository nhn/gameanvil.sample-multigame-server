package com.nhn.gameanvil.sample.space.game.room;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.sample.protocol.Sample;
import com.nhn.gameanvil.sample.space.game.match.GameUserMatchInfo;
import com.nhn.gameanvil.sample.space.game.user.GameUser;
import org.slf4j.Logger;

public class GameRoomForeMatchUser extends GameRoom {
    private static final Logger logger = getLogger(GameRoomForeMatchUser.class);
    private boolean isRefill = false;
    private static final int MaxUserCount = 2;

    @Override
    public boolean onCreateRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("GameRoomForeMatchUser.onCreateRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        try {
            users.put(gameUser.getUserId(), gameUser);
            return true;
        } catch (Exception e) {
            logger.error("GameRoomForeMatchUser::onCreateRoom()", e);
            return false;
        }
    }

    @Override
    public boolean onJoinRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("GameRoomForeMatchUser.onJoinRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        try {
            users.put(gameUser.getUserId(), gameUser);
            logger.info("GameRoomForeMatchUser.onJoinRoom - userMatchMaking");
            if (isRefill) {
                if (isRefill) {
                    logger.info("GameRoomForeMatchUser.onJoinRoom - roomMatchMaking");
                    String message = String.format("%s is join", gameUser.getUserId());
                    for (GameUser user : users.values()) {
                        if (gameUser.getUserId() == user.getUserId()) {
                            continue;
                        }

                        user.send(new Packet(Sample.GameMessageToC.newBuilder().setMessage(message)));
                        logger.info("GameRoomForeMatchUser.onJoinRoom - to {} : {}", user.getUserId(), message);
                    }

                    if (users.size() == MaxUserCount) {
                        isRefill = false;
                    }
                }
            } else {
                if (users.size() == MaxUserCount) {
                    logger.info("GameRoomForeMatchUser.onJoinRoom - userMatchMaking completed");
                    for (GameUser user : users.values()) {
                        String message = String.format("%s is join", user.getUserId());
                        for (GameUser to : users.values()) {
                            if (to.getUserId() == user.getUserId()) {
                                continue;
                            }

                            to.send(new Packet(Sample.GameMessageToC.newBuilder().setMessage(message)));
                            logger.info("GameRoomForeMatchUser.onJoinRoom - to {} : {}", to.getUserId(), message);
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            users.remove(gameUser.getUserId());
            logger.error("GameRoomForeMatchUser::onJoinRoom()", e);
            return false;
        }
    }

    @Override
    public boolean onLeaveRoom(GameUser gameUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("GameRoomForeMatchUser.onLeaveRoom - RoomId : {}, UserId : {}", getId(), gameUser.getUserId());
        try {
            users.remove(gameUser.getUserId());

            // MatchUser로 생성된 방인경우 matchRefill()을 사용해 결원을 채울 수 있음.
            if (isMadeByUserMatchMaker()) {

                try {
                    // Refill() 정보 등록
                    logger.info("GameRoomForeMatchUser.onLeaveRoom - refill");
                    isRefill = true;
                    if (!matchRefill(new GameUserMatchInfo(getId(), 100, 1))) {
                        logger.warn("MatchRefill for the room({}) failure!", getId());
                    }
                } catch (Exception e) {
                    isRefill = false;
                    logger.error("GameRoomForeMatchUser::onLeaveRoom()", e);
                }
            }
            return true;
        } catch (Exception e) {
            users.put(gameUser.getUserId(), gameUser);
            return false;
        }
    }
}


