package com.nhnent.tardis.sample.space.chat.match;

import com.nhnent.tardis.console.match.RoomMatchMaker;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatRoomMatchMaker extends RoomMatchMaker<ChatRoomMatchInfo> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ChatRoomMatchInfo match(ChatRoomMatchInfo terms, Object... args) {
        logger.info("ChatRoomMatchMaker.match");
        String bypassRoomId = (String)args[0];
        List<ChatRoomMatchInfo>  rooms = getRooms();
        logger.info("ChatRoomMatchMaker.match - rooms : {}", rooms.size());
        // rooms는 인원수가 적은 순서로 정렬되어있음.
        // roomId 가 bypassRoomId이 아닌 첫번째 room을 선택.
        for (ChatRoomMatchInfo info : rooms) {
            if (info.getRoomId().equals(bypassRoomId)){
                logger.info("ChatRoomMatchMaker.match - bypass : {}", bypassRoomId);
                continue;
            }

            // 최대 인원수가 terms와 다르면 pass!
            if(info.getUserCountMax() != terms.getUserCountMax()){
                logger.info("ChatRoomMatchMaker.match - userCountMax : {}", info.getUserCountMax());
                continue;
            }

            // 꽉 찼으면 pass!
            if(info.getUserCountMax() == info.getUserCountCurr()){
                logger.info("ChatRoomMatchMaker.match - userCountCurr : {}", info.getUserCountMax());
                continue;
            }

            // 매칭 성공!
            logger.info("ChatRoomMatchMaker.match : {}", info.getRoomId());
            return info;
        }

        // 매칭할 방이 없어 매칭 실패.
        // create 옵션이 true일 경우 자동으로 방을 생성하면서 매칭 성공.
        return null;
    }

    @Override
    public Comparator<ChatRoomMatchInfo> getComparator() {
        return new Comparator<ChatRoomMatchInfo>() {
            @Override
            public int compare(ChatRoomMatchInfo o1, ChatRoomMatchInfo o2) {
                logger.info("ChatRoomMatchMaker.match - compare : {}, {}", o1, o2);
                return o1.getUserCountCurr() - o2.getUserCountCurr();
            }
        };
    }
}
