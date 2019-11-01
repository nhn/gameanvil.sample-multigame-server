package com.nhnent.tardis.chat;

import com.nhnent.tardis.chat.protocol.Chat;
import com.nhnent.tardis.console.TardisBootstrap;
import com.nhnent.tardis.chat.space.ChatNode;
import com.nhnent.tardis.chat.space.room.ChatRoom;
import com.nhnent.tardis.chat.space.user.ChatUser;

public class Main {

    public static void main(String[] args) {

        TardisBootstrap bootstrap = TardisBootstrap.getInstance();

        bootstrap.addProtoBufClass(0, Chat.class);

        bootstrap.setSpace("ChatService")
                .node(ChatNode.class)
                .user("ChatUser",ChatUser.class)
                .room("ChatRoom",ChatRoom.class);

        bootstrap.run();
    }

}
