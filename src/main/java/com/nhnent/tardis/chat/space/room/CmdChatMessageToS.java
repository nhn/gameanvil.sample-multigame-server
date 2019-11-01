package com.nhnent.tardis.chat.space.room;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.chat.protocol.Chat;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.space.IRoomPacketHandler;
import com.nhnent.tardis.chat.space.user.ChatUser;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdChatMessageToS implements IRoomPacketHandler<ChatRoom, ChatUser> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(ChatRoom chatRoom, ChatUser chatUser, Packet packet) throws SuspendExecution {

        try {
            // parse.
            Chat.ChatMessageToS fromClient = Chat.ChatMessageToS.parseFrom(packet.getStream());

            // make.
            Chat.ChatMessageToC.Builder toClient = Chat.ChatMessageToC.newBuilder();
            toClient.setMessage(chatUser.getNickName() + " : " + fromClient.getMessage());

            // for send.
            for (ChatUser user : chatRoom.getUsers()) {
               user.send(new Packet(toClient));
            }

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

}