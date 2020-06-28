package com.nhnent.tardis.sample.space.game.match;

import com.nhn.gameflex.node.match.UserMatchInfo;
import java.io.Serializable;

public class GameUserMatchInfo extends UserMatchInfo implements Serializable {

    private int id;
    private int partySize = 0;
    private int rating;

    public GameUserMatchInfo() {
    }

    public GameUserMatchInfo(int id, int rating, int partySize) {
        this.id = id;
        this.rating = rating;
        this.partySize = partySize;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getPartySize() {
        return partySize;
    }

    public int getRating() {
        return rating;
    }
}
