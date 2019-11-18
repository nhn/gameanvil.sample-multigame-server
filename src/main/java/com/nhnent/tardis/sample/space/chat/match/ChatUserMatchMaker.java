package com.nhnent.tardis.sample.space.chat.match;

import com.nhnent.tardis.console.match.UserMatchMaker;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatUserMatchMaker extends UserMatchMaker<ChatUserMatchInfo> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int matchPoolFactorMax = 4; // match 정원의 몇 배수까지 인원을 모은 후에 rating 별로 정렬해서 매칭할 것인가?
    private int currentMatchPoolFactor = matchPoolFactorMax;
    private long lastMatchTime = System.currentTimeMillis();

    public ChatUserMatchMaker(){
        super(2, 5000);
    }

    @Override
    public void match() {
        logger.info("ChatUserMatchMaker.match()");
        // matchSize : 매치될 Room 인원 수
        // leastAmount : 매칭 계산에 필요한 인원 수.
        int leastAmount = matchSize * currentMatchPoolFactor;
        List<ChatUserMatchInfo> matchRequests = getMatchRequests(leastAmount);
        if (matchRequests == null) {
            // getMatchRequests : 매칭 요청자의 총 수가 leastAmount보다 적을 경우 null을 리턴한다.
            if (System.currentTimeMillis() - lastMatchTime >= 1000){
                // 1000 ms 동안  leastAmount를 체우지 못한 경우
                // currentMatchPoolFactor를 조정하여leastAmount의 크기를 줄인다.
                currentMatchPoolFactor = Math.max(currentMatchPoolFactor/2, 1);
                logger.info("ChatUserMatchMaker.match() - reduce currentMatchPoolFactor: {}", currentMatchPoolFactor);
            }
            return;
        }

        int matchingAmount = matchSingles(matchRequests);
        if (matchingAmount > 0) {
            logger.info("ChatUserMatchMaker.match() - {} match(s) made", matchingAmount);
            lastMatchTime = System.currentTimeMillis();
            currentMatchPoolFactor = matchPoolFactorMax;
        }
    }

    @Override
    public boolean refill(ChatUserMatchInfo req) {
        logger.info("ChatUserMatchMaker.refill()");
        try {
            // 전체 리필 요청 목록
            List<ChatUserMatchInfo> refillRequests = getRefillRequests();
            if (refillRequests.isEmpty()) {
                // 리필 요청 목록이 없을 경우
                logger.info("ChatUserMatchMaker.refill() - refillRequests.isEmpty");
                return false;
            }
            logger.info("ChatUserMatchMaker.refill() - RefillRequests : {}", refillRequests.size());
            for (ChatUserMatchInfo refillInfo : refillRequests) {
                // 100점 이상 차이나지 않으면 리필
                if (Math.abs(req.getRating() - refillInfo.getRating()) < 100) {
                    if (refillRoom(req, refillInfo)) { // 해당 매칭 요청을 리필이 필요한 방으로 매칭
                        logger.info("ChatUserMatchMaker.refill() - Refill success: {}", refillInfo.getId());
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return false;
    }
}
