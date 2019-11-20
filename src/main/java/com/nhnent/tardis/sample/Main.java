package com.nhnent.tardis.sample;

import com.nhnent.tardis.console.TardisBootstrap;
import com.nhnent.tardis.sample.Defines.StringValues;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.service.SampleServiceNodeAgent;
import com.nhnent.tardis.sample.session.SampleSessionAgent;
import com.nhnent.tardis.sample.session.SampleSessionNodeAgent;
import com.nhnent.tardis.sample.session.SampleSessionUserAgent;
import com.nhnent.tardis.sample.space.chat.ChatNode;
import com.nhnent.tardis.sample.space.game.GameNode;
import com.nhnent.tardis.sample.space.game.match.GameRoomMatchInfo;
import com.nhnent.tardis.sample.space.game.match.GameRoomMatchMaker;
import com.nhnent.tardis.sample.space.game.match.GameUserMatchInfo;
import com.nhnent.tardis.sample.space.game.match.GameUserMatchMaker;
import com.nhnent.tardis.sample.space.chat.room.ChatRoom;
import com.nhnent.tardis.sample.space.chat.user.ChatUser;
import com.nhnent.tardis.sample.space.game.room.GameRoomMatchRoom;
import com.nhnent.tardis.sample.space.game.room.GameRoomMatchUser;
import com.nhnent.tardis.sample.space.game.user.GameUser;

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
                .room(StringValues.ChatRoomType, ChatRoom.class);

        bootstrap.setSpace(StringValues.GameServiceName)
            .node(GameNode.class)
            .user(StringValues.GameUserType, GameUser.class)
            .room(StringValues.GameRoomType_RoomMatch, GameRoomMatchRoom.class)
            .roomMatchMaker(StringValues.GameRoomType_RoomMatch, GameRoomMatchMaker.class, GameRoomMatchInfo.class)
            .room(StringValues.GameRoomType_UserMatch, GameRoomMatchUser.class)
            .userMatchMaker(StringValues.GameRoomType_UserMatch, GameUserMatchMaker.class, GameUserMatchInfo.class);

        bootstrap.setService(StringValues.SampleServiceName)
            .node(SampleServiceNodeAgent.class);
        bootstrap.run();
    }

}
