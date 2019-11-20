package com.nhnent.tardis.sample.space.chat.match;

import com.nhnent.tardis.console.space.IRoomMatchInfo;
import java.io.Serializable;

public class ChatRoomMatchInfo implements Serializable, IRoomMatchInfo {
    private String roomId = "";
    private int userCountCurr = 0;
    private int userCountMax = 2;

    public void setRoomId(String id) {
        roomId = id;
    }

    @Override
    public String getRoomId() {
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
