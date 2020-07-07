package com.nhnent.tardis.sample.space.game.match;

import com.nhn.gameanvil.node.match.RoomMatchInfo;
import java.io.Serializable;

public class GameRoomMatchInfo implements Serializable, RoomMatchInfo {
    private int roomId = 0;
    private int userCountCurr = 0;
    private int userCountMax = 2;

    public void setRoomId(int id) {
        roomId = id;
    }

    @Override
    public int getRoomId() {
        return roomId;
    }

    public void setUserCountCurr(int count) {
        userCountCurr = count;
    }

    public int getUserCountCurr() {
        return userCountCurr;
    }

    public void setUserCountMax(int count) {
        userCountMax = count;
    }

    public int getUserCountMax() {
        return userCountMax;
    }
}
