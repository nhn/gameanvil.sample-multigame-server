package com.nhnent.tardis.sample.space.chat.room;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.node.game.RoomPacketHandler;
import com.nhn.gameanvil.packet.Packet;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.space.chat.user.ChatUser;
import org.slf4j.Logger;

public class CmdChatMessageToS implements RoomPacketHandler<ChatRoom, ChatUser> {

    private static final Logger logger = getLogger(CmdChatMessageToS.class);

    @Override
    public void execute(ChatRoom chatRoom, ChatUser chatUser, Packet packet) throws SuspendExecution {

        try {
            // parse.
            Sample.ChatMessageToS fromClient = Sample.ChatMessageToS.parseFrom(packet.getStream());

            // make.
            Sample.ChatMessageToC.Builder toClient = Sample.ChatMessageToC.newBuilder();
            if (chatUser != null) {
                toClient.setMessage("[" + chatUser.getNickName() + "] " + fromClient.getMessage());
            } else {
                toClient.setMessage(fromClient.getMessage());
            }

            // for send.
            for (ChatUser user : chatRoom.getUsers()) {
                user.send(new Packet(toClient));
            }

        } catch (Exception e) {
            logger.error("CmdChatMessageToS::execute()", e);
        }
    }

}
