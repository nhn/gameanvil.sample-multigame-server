package com.nhnent.tardis.sample;

import com.nhnent.tardis.console.TardisBootstrap;
import com.nhnent.tardis.sample.Defines.StringValues;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.service.SampleServiceNodeAgent;
import com.nhnent.tardis.sample.session.SampleSessionAgent;
import com.nhnent.tardis.sample.session.SampleSessionNodeAgent;
import com.nhnent.tardis.sample.session.SampleSessionUserAgent;
import com.nhnent.tardis.sample.space.chat.ChatNode;
import com.nhnent.tardis.sample.space.chat.match.ChatRoomMatchInfo;
import com.nhnent.tardis.sample.space.chat.match.ChatRoomMatchMaker;
import com.nhnent.tardis.sample.space.chat.match.ChatUserMatchInfo;
import com.nhnent.tardis.sample.space.chat.match.ChatUserMatchMaker;
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

        bootstrap.setSpace(StringValues.ChatServiceName)
                .node(ChatNode.class)
                .user(StringValues.ChatUserType, ChatUser.class)
                .room(StringValues.ChatRoomType, ChatRoom.class)
                .roomMatchMaker(StringValues.ChatRoomType, ChatRoomMatchMaker.class, ChatRoomMatchInfo.class)
                .userMatchMaker(StringValues.ChatRoomType, ChatUserMatchMaker.class, ChatUserMatchInfo.class);

        bootstrap.setService(StringValues.SampleServiceName)
            .node(SampleServiceNodeAgent.class);
        bootstrap.run();
    }

}
