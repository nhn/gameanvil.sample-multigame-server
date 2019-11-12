package com.nhnent.tardis.sample.space.chat.room;

import co.paralleluniverse.fibers.SuspendExecution;
import com.google.protobuf.Message;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.space.chat.match.ChatRoomMatchInfo;
import com.nhnent.tardis.sample.space.chat.match.ChatUserMatchInfo;
import com.nhnent.tardis.sample.space.chat.user.ChatUser;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.ITimerHandler;
import com.nhnent.tardis.common.internal.ITimerObject;
import com.nhnent.tardis.console.space.IRoom;
import com.nhnent.tardis.console.space.RoomAgent;
import com.nhnent.tardis.console.space.RoomPacketDispatcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatRoom extends RoomAgent implements IRoom<ChatUser>, ITimerHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static RoomPacketDispatcher dispatcher = new RoomPacketDispatcher();
    static {
        dispatcher.registerMsg(Sample.ChatMessageToS.class, CmdChatMessageToS.class);
    }

    private ChatRoomMatchInfo gameRoomMatchInfo = new ChatRoomMatchInfo();

    private Map<String, ChatUser> users = new HashMap<>();


    @Override
    public void onInit() throws SuspendExecution {
        logger.info("ChatRoom.onInit");
    }

    @Override
    public void onDestroy() throws SuspendExecution {
        logger.info("ChatRoom.onDestroy");
    }

    @Override
    public void onDispatch(ChatUser spaceUser, Packet packet) throws SuspendExecution {
        logger.info("ChatRoom.onDispatch : {}",
            TardisIndexer.getMsgName(packet.getDescId(), packet.getMsgIndex()));
        dispatcher.dispatch(this, spaceUser, packet);
    }

    @Override
    public boolean onCreateRoom(ChatUser chatUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("ChatRoom.onCreateRoom");
        try{
            users.put(chatUser.getUserId(), chatUser);

            gameRoomMatchInfo.setRoomId(getId());
            gameRoomMatchInfo.setUserCountCurr(users.size());
            updateRoomMatchInfo(gameRoomMatchInfo);

            String message = String.format("%s create room",chatUser.getNickName());
            chatUser.send(new Packet(Sample.ChatMessageToC.newBuilder().setMessage(message)));
            logger.info("ChatRoom.onCreateRoom - to {} : {}", chatUser.getNickName(), message);
            return true;
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    @Override
    public boolean onJoinRoom(ChatUser chatUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("ChatRoom.onJoinRoom");
        try{
            users.put(chatUser.getUserId(), chatUser);
            gameRoomMatchInfo.setUserCountCurr(users.size());
            updateRoomMatchInfo(gameRoomMatchInfo);

            String message = String.format("%s is join",chatUser.getNickName());
            for(ChatUser user:users.values()){
                user.send(new Packet(Sample.ChatMessageToC.newBuilder().setMessage(message)));
                logger.info("ChatRoom.onJoinRoom - to {} : {}",user.getNickName(), message);
            }

            return true;
        }catch (Exception e){
            users.remove(chatUser.getUserId());
            gameRoomMatchInfo.setUserCountCurr(users.size());
            logger.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    @Override
    public boolean onLeaveRoom(ChatUser chatUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("ChatRoom.onLeaveRoom");
        try{
            users.remove(chatUser.getUserId());
            gameRoomMatchInfo.setUserCountCurr(users.size());
            updateRoomMatchInfo(gameRoomMatchInfo);

            // MatchUser로 생성된 방인경우 matchRefill()을 사용해 결원을 채울 수 있음.
            if (isMadeByUserMatchMaker()) {

                try {
                    // Refill() 정보 등록
                    if (!matchRefill(new ChatUserMatchInfo("Refill", 100))) {
                        logger.warn("MatchRefill for the room({}) failure!", getId());
                    }
                } catch (Exception e) {
                    logger.error(ExceptionUtils.getStackTrace(e));
                }
            }
            return true;
        }catch(Exception e){
            users.put(chatUser.getUserId(), chatUser);
            gameRoomMatchInfo.setUserCountCurr(users.size());
            return false;
        }
    }

    @Override
    public void onPostLeaveRoom(ChatUser chatUser) throws SuspendExecution {
        logger.info("ChatRoom.onPostLeaveRoom");
        String message = String.format("%s is leave",chatUser.getNickName());
        for(ChatUser user:users.values()){
            user.send(new Packet(Sample.ChatMessageToC.newBuilder().setMessage(message)));
        }
    }

    @Override
    public void onRejoinRoom(ChatUser chatUser, Payload outPayload) throws SuspendExecution {
        logger.info("ChatRoom.onRejoinRoom");
        String message = String.format("%s is back",chatUser.getNickName());
        for(ChatUser user:users.values()){
            user.send(new Packet(Sample.ChatMessageToC.newBuilder().setMessage(message)));
        }
    }

    @Override
    public boolean canTransfer() throws SuspendExecution {
        logger.info("ChatRoom.canTransfer");
        return false;
    }

    @Override
    public void onTimer(ITimerObject iTimerObject, Object arg) throws SuspendExecution {
        logger.info("ChatRoom.onTimer");
    }

    //-------------------------------------------------------------------------

    public Collection<ChatUser> getUsers() {
        return users.values();
    }

}
