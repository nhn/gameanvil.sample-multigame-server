package com.nhnent.tardis.sample.space.chat.room;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameflex.node.game.BaseRoom;
import com.nhn.gameflex.node.game.RoomPacketDispatcher;
import com.nhn.gameflex.packet.Packet;
import com.nhn.gameflex.packet.Payload;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.space.chat.user.ChatUser;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;

public class ChatRoom extends BaseRoom<ChatUser> {
    private static final Logger logger = getLogger(ChatRoom.class);

    private static RoomPacketDispatcher dispatcher = new RoomPacketDispatcher();

    static {
        dispatcher.registerMsg(Sample.ChatMessageToS.getDescriptor(), CmdChatMessageToS.class);
    }

    private Map<Integer, ChatUser> users = new HashMap<>();

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("ChatRoom.onInit - RoomId : {}", getId());
    }

    @Override
    public void onDestroy() throws SuspendExecution {
        logger.info("ChatRoom.onDestroy - RoomId : {}", getId());
    }

    @Override
    public void onDispatch(ChatUser chatUser, Packet packet) throws SuspendExecution {
        logger.info("ChatRoom.onDispatch : RoomId : {}, UserId : {}, {}",
            getId(),
            chatUser.getUserId(),
            packet.getMsgName());
        dispatcher.dispatch(this, chatUser, packet);
    }

    @Override
    public boolean onCreateRoom(ChatUser chatUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("ChatRoom.onCreateRoom - RoomId : {}, UserId : {}", getId(), chatUser.getUserId());
        try {
            users.put(chatUser.getUserId(), chatUser);

            return true;
        } catch (Exception e) {
            logger.error("ChatRoom::onCreateRoom()", e);
            return false;
        }
    }

    @Override
    public boolean onJoinRoom(ChatUser chatUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("ChatRoom.onJoinRoom - RoomId : {}, UserId : {}", getId(), chatUser.getUserId());
        try {
            String message = String.format("%s is join", chatUser.getNickName());
            for (ChatUser user : users.values()) {
                user.send(new Packet(Sample.ChatMessageToC.newBuilder().setMessage(message)));
                logger.info("ChatRoom.onJoinRoom - to {} : {}", user.getNickName(), message);
            }
            users.put(chatUser.getUserId(), chatUser);
            return true;
        } catch (Exception e) {
            users.remove(chatUser.getUserId());
            logger.error("ChatRoom::onJoinRoom()", e);
            return false;
        }
    }

    @Override
    public boolean onLeaveRoom(ChatUser chatUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("ChatRoom.onLeaveRoom - RoomId : {}, UserId : {}", getId(), chatUser.getUserId());
        try {
            users.remove(chatUser.getUserId());
            return true;
        } catch (Exception e) {
            users.put(chatUser.getUserId(), chatUser);
            return false;
        }
    }

    @Override
    public void onPostLeaveRoom(ChatUser chatUser) throws SuspendExecution {
        logger.info("ChatRoom.onPostLeaveRoom - RoomId : {}, UserId : {}", getId(), chatUser.getUserId());
        String message = String.format("%s is leave", chatUser.getNickName());
        for (ChatUser user : users.values()) {
            user.send(new Packet(Sample.ChatMessageToC.newBuilder().setMessage(message)));
        }
    }

    @Override
    public void onRejoinRoom(ChatUser chatUser, Payload outPayload) throws SuspendExecution {
        logger.info("ChatRoom.onRejoinRoom - RoomId : {}, UserId : {}", getId(), chatUser.getUserId());
        String message = String.format("%s is back", chatUser.getNickName());
        for (ChatUser user : users.values()) {
            user.send(new Packet(Sample.ChatMessageToC.newBuilder().setMessage(message)));
        }
    }

    @Override
    public boolean canTransfer() throws SuspendExecution {
        logger.info("ChatRoom.canTransfer - RoomId : {}", getId());
        return false;
    }

    public Collection<ChatUser> getUsers() {
        return users.values();
    }

}
