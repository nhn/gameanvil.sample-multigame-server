package com.nhnent.tardis.sample;

import com.nhnent.tardis.console.TardisBootstrap;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.SampleSessionAgent;
import com.nhnent.tardis.sample.session.SampleSessionNodeAgent;
import com.nhnent.tardis.sample.session.SampleSessionUserAgent;
import com.nhnent.tardis.sample.space.chat.ChatNode;
import com.nhnent.tardis.sample.space.chat.room.ChatRoom;
import com.nhnent.tardis.sample.space.chat.user.ChatUser;

public class Main {

    public static void main(String[] args) {

        TardisBootstrap bootstrap = TardisBootstrap.getInstance();

        bootstrap.addProtoBufClass(0, Sample.class);

        bootstrap.setSession()
                .session(SampleSessionAgent.class)
                .user(SampleSessionUserAgent.class)
                .node(SampleSessionNodeAgent.class)
                .enableWhiteModules();

        bootstrap.setSpace("ChatService")
                .node(ChatNode.class)
                .user("ChatUser",ChatUser.class)
                .room("ChatRoom",ChatRoom.class);

        bootstrap.run();
    }

}
