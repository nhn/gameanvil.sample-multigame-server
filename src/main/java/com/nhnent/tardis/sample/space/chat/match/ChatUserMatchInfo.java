package com.nhnent.tardis.sample.space.chat.match;

import com.nhnent.tardis.console.space.UserMatchInfo;
import java.io.Serializable;

public class ChatUserMatchInfo extends UserMatchInfo implements Serializable, Comparable<ChatUserMatchInfo> {

    private String id;
    private int partySize = 0;
    private int rating;

    public ChatUserMatchInfo(){}
    public ChatUserMatchInfo(String id, int rating){
        this.id = id;
        this.rating = rating;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getPartySize() {
        return partySize;
    }

    public int getRating(){
        return rating;
    }

    public void setRating(int rating){
        this.rating = rating;
    }

    @Override
    public int compareTo(ChatUserMatchInfo o) {
        return rating - o.rating;
    }
}
