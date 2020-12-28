package com.nhn.gameanvil.sample;

import com.nhn.gameanvil.GameAnvilBootstrap;
import com.nhn.gameanvil.sample.protocol.Sample;
import com.nhn.gameanvil.sample.service.SampleSpot;
import com.nhn.gameanvil.sample.service.SampleSupportNode;
import com.nhn.gameanvil.sample.session.SampleConnection;
import com.nhn.gameanvil.sample.session.SampleGatewayNode;
import com.nhn.gameanvil.sample.session.SampleSession;
import com.nhn.gameanvil.sample.Defines.StringValues;
import com.nhn.gameanvil.sample.space.chat.ChatNode;
import com.nhn.gameanvil.sample.space.game.GameNode;
import com.nhn.gameanvil.sample.space.game.match.GameRoomMatchInfo;
import com.nhn.gameanvil.sample.space.game.match.GameRoomMatchMaker;
import com.nhn.gameanvil.sample.space.game.match.GameUserMatchInfo;
import com.nhn.gameanvil.sample.space.game.match.GameUserMatchMaker;
import com.nhn.gameanvil.sample.space.chat.room.ChatRoom;
import com.nhn.gameanvil.sample.space.chat.user.ChatUser;
import com.nhn.gameanvil.sample.space.game.match.GamePartyMatchMaker;
import com.nhn.gameanvil.sample.space.game.room.GameRoomForMatchRoom;
import com.nhn.gameanvil.sample.space.game.room.GameRoomForeMatchUser;
import com.nhn.gameanvil.sample.space.game.room.GameRoomForMatchParty;
import com.nhn.gameanvil.sample.space.game.room.PartyRoom;
import com.nhn.gameanvil.sample.space.game.user.GameUser;

public class Main {

    public static void main(String[] args) {

        GameAnvilBootstrap bootstrap = GameAnvilBootstrap.getInstance();

        bootstrap.addProtoBufClass(0, Sample.getDescriptor());

        bootstrap.setGateway()
            .connection(SampleConnection.class)
            .session(SampleSession.class)
            .node(SampleGatewayNode.class)
            .enableWhiteModules();

        bootstrap.setGame(StringValues.ChatServiceName)
            .node(ChatNode.class)
            .user(StringValues.ChatUserType, ChatUser.class)
            .room(StringValues.ChatRoomType, ChatRoom.class);

        bootstrap.setGame(StringValues.GameServiceName)
            .node(GameNode.class)
            .user(StringValues.GameUserType, GameUser.class)

            .room(StringValues.GameRoomType_MatchRoom, GameRoomForMatchRoom.class)
            .roomMatchMaker(StringValues.GameRoomType_MatchRoom, GameRoomMatchMaker.class, GameRoomMatchInfo.class)

            .room(StringValues.GameRoomType_MatchUser, GameRoomForeMatchUser.class)
            .userMatchMaker(StringValues.GameRoomType_MatchUser, GameUserMatchMaker.class, GameUserMatchInfo.class)

            .room(StringValues.PartyRoomType, PartyRoom.class)
            .room(StringValues.GameRoomType_MatchParty, GameRoomForMatchParty.class)
            .userMatchMaker(StringValues.GameRoomType_MatchParty, GamePartyMatchMaker.class, GameUserMatchInfo.class);

        bootstrap.setSupport(StringValues.SampleServiceName)
            .node(SampleSupportNode.class)
            .spot(StringValues.SampleSpotType, SampleSpot.class);

        bootstrap.run();
    }

}
