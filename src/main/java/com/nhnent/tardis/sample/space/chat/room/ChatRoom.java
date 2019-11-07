package com.nhnent.tardis.sample.space.chat.room;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.sample.protocol.Sample;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatRoom extends RoomAgent implements IRoom<ChatUser>, ITimerHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static RoomPacketDispatcher dispatcher = new RoomPacketDispatcher();

    static {
        dispatcher.registerMsg(Sample.ChatMessageToS.class, CmdChatMessageToS.class);
    }

    private Map<String, ChatUser> users = new HashMap<>();

    //-------------------------------------------------------------------------

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
        users.put(chatUser.getUserId(), chatUser);

        String message = String.format("%s is join",chatUser.getNickName());
        chatUser.send(new Packet(Sample.ChatMessageToC.newBuilder().setMessage(message)));
        return true;
    }

    @Override
    public boolean onJoinRoom(ChatUser chatUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("ChatRoom.onJoinRoom");
        users.put(chatUser.getUserId(), chatUser);

        String message = String.format("%s is join",chatUser.getNickName());
        for(ChatUser user:users.values()){
            user.send(new Packet(Sample.ChatMessageToC.newBuilder().setMessage(message)));
        }
        return true;
    }

    @Override
    public boolean onLeaveRoom(ChatUser chatUser, Payload inPayload, Payload outPayload) throws SuspendExecution {
        logger.info("ChatRoom.onLeaveRoom");
        String message = String.format("%s is leave",chatUser.getNickName());
        for(ChatUser user:users.values()){
            user.send(new Packet(Sample.ChatMessageToC.newBuilder().setMessage(message)));
        }
        return true;
    }

    @Override
    public void onPostLeaveRoom(ChatUser chatUser) throws SuspendExecution {
        logger.info("ChatRoom.onPostLeaveRoom");
        users.remove(chatUser.getUserId());
    }

    @Override
    public void onRejoinRoom(ChatUser chatUser, Payload outPayload) throws SuspendExecution {
        logger.info("ChatRoom.onRejoinRoom");
        users.put(chatUser.getUserId(), chatUser);
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
