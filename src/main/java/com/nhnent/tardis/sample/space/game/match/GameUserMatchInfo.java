package com.nhnent.tardis.sample.space.game.match;

import com.nhnent.tardis.console.space.UserMatchInfo;
import java.io.Serializable;

public class GameUserMatchInfo extends UserMatchInfo implements Serializable{

    private String id;
    private int partySize = 0;
    private int rating;

    public GameUserMatchInfo(){};
    public GameUserMatchInfo(String id, int rating, int partySize){
        this.id = id;
        this.rating = rating;
        this.partySize = partySize;
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
}
