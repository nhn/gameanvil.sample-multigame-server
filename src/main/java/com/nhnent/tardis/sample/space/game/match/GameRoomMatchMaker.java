package com.nhnent.tardis.sample.space.game.match;

import com.nhnent.tardis.console.match.RoomMatchMaker;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameRoomMatchMaker extends RoomMatchMaker<GameRoomMatchInfo> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public GameRoomMatchInfo match(GameRoomMatchInfo terms, Object... args) {
        logger.info("GameRoomMatchMaker.match");
        String bypassRoomId = terms.getRoomId();
        logger.info("GameRoomMatchMaker.match - args : {}", args);
        List<GameRoomMatchInfo> rooms = getRooms();
        logger.info("GameRoomMatchMaker.match - rooms : {}", rooms.size());
        // rooms는 인원수가 적은 순서로 정렬되어있음.
        // roomId 가 bypassRoomId이 아닌 첫번째 room을 선택.
        for (GameRoomMatchInfo info : rooms) {
            if (info.getRoomId().equals(bypassRoomId)) {
                // moveRoom 옵션이 true 일 경우 참여중인 방은 제외하기
                logger.info("GameRoomMatchMaker.match - bypass : {}", bypassRoomId);
                continue;
            }

            // 최대 인원수가 terms와 다르면 pass!
            if (info.getUserCountMax() != terms.getUserCountMax()) {
                logger.info("GameRoomMatchMaker.match - userCountMax : {}", info.getUserCountMax());
                continue;
            }

            // 꽉 찼으면 pass!
            if (info.getUserCountMax() == info.getUserCountCurr()) {
                logger.info("GameRoomMatchMaker.match - userCountCurr : {}", info.getUserCountMax());
                continue;
            }

            // 매칭 성공!
            logger.info("GameRoomMatchMaker.match : {}", info.getRoomId());
            return info;
        }

        // 매칭할 방이 없어 매칭 실패.
        // create 옵션이 true일 경우 자동으로 방을 생성하면서 매칭 성공.
        return null;
    }

    @Override
    public Comparator<GameRoomMatchInfo> getComparator() {
        return new Comparator<GameRoomMatchInfo>() {
            @Override
            public int compare(GameRoomMatchInfo o1, GameRoomMatchInfo o2) {
                logger.info("GameRoomMatchMaker.match - compare : {}, {}", o1, o2);
                return o1.getUserCountCurr() - o2.getUserCountCurr();
            }
        };
    }
}
