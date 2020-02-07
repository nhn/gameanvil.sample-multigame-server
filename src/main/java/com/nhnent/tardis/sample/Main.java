package com.nhnent.tardis.sample;

import com.nhnent.tardis.console.TardisBootstrap;
import com.nhnent.tardis.sample.Defines.StringValues;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.service.SampleServiceNode;
import com.nhnent.tardis.sample.service.SampleSpot;
import com.nhnent.tardis.sample.session.SampleSession;
import com.nhnent.tardis.sample.session.SampleSessionNode;
import com.nhnent.tardis.sample.session.SampleSessionUser;
import com.nhnent.tardis.sample.space.chat.ChatNode;
import com.nhnent.tardis.sample.space.game.GameNode;
import com.nhnent.tardis.sample.space.game.match.GameRoomMatchInfo;
import com.nhnent.tardis.sample.space.game.match.GameRoomMatchMaker;
import com.nhnent.tardis.sample.space.game.match.GameUserMatchInfo;
import com.nhnent.tardis.sample.space.game.match.GameUserMatchMaker;
import com.nhnent.tardis.sample.space.chat.room.ChatRoom;
import com.nhnent.tardis.sample.space.chat.user.ChatUser;
import com.nhnent.tardis.sample.space.game.match.GamePartyMatchMaker;
import com.nhnent.tardis.sample.space.game.room.GameRoomForMatchRoom;
import com.nhnent.tardis.sample.space.game.room.GameRoomForeMatchUser;
import com.nhnent.tardis.sample.space.game.room.GameRoomForeMatchParty;
import com.nhnent.tardis.sample.space.game.room.PartyRoom;
import com.nhnent.tardis.sample.space.game.user.GameUser;

public class Main {

    public static void main(String[] args) {

        TardisBootstrap bootstrap = TardisBootstrap.getInstance();

        bootstrap.addProtoBufClass(0, Sample.getDescriptor());

        bootstrap.setSession()
            .session(SampleSession.class)
            .user(SampleSessionUser.class)
            .node(SampleSessionNode.class)
            .enableWhiteModules();

        bootstrap.setSpace(StringValues.ChatServiceName)
            .node(ChatNode.class)
            .user(StringValues.ChatUserType, ChatUser.class)
            .room(StringValues.ChatRoomType, ChatRoom.class);

        bootstrap.setSpace(StringValues.GameServiceName)
            .node(GameNode.class)
            .user(StringValues.GameUserType, GameUser.class)

            .room(StringValues.GameRoomType_MatchRoom, GameRoomForMatchRoom.class)
            .roomMatchMaker(StringValues.GameRoomType_MatchRoom, GameRoomMatchMaker.class, GameRoomMatchInfo.class)

            .room(StringValues.GameRoomType_MatchUser, GameRoomForeMatchUser.class)
            .userMatchMaker(StringValues.GameRoomType_MatchUser, GameUserMatchMaker.class, GameUserMatchInfo.class)

            .room(StringValues.PartyRoomType, PartyRoom.class)
            .room(StringValues.GameRoomType_MatchParty, GameRoomForeMatchParty.class)
            .userMatchMaker(StringValues.GameRoomType_MatchParty, GamePartyMatchMaker.class, GameUserMatchInfo.class);

        bootstrap.setService(StringValues.SampleServiceName)
            .node(SampleServiceNode.class)
            .spot(StringValues.SampleSpotType, SampleSpot.class);

        bootstrap.run();
    }

}
