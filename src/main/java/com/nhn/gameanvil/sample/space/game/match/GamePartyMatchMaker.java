package com.nhn.gameanvil.sample.space.game.match;

import static org.slf4j.LoggerFactory.getLogger;

import com.nhn.gameanvil.node.match.UserMatchMaker;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;

public class GamePartyMatchMaker extends UserMatchMaker<GameUserMatchInfo> {
    private static final Logger logger = getLogger(GamePartyMatchMaker.class);

    private final int matchPoolFactorMax = 1; // match 정원의 몇 배수까지 인원을 모은 후에 rating 별로 정렬해서 매칭할 것인가?
    private int currentMatchPoolFactor = matchPoolFactorMax;
    private long lastMatchTime = System.currentTimeMillis();

    public GamePartyMatchMaker() {
        super(
            4, // 매치될 Room 인원 수
            5000 // Timeout
        );
    }

    @Override
    public void match() {
        // matchSize : 매치될 Room 인원 수
        // leastAmount : 매칭 계산에 필요한 최소 요청 수.
        int partySize = 2;
        int leastAmount = matchSize / partySize * currentMatchPoolFactor;
        List<GameUserMatchInfo> matchRequests = getMatchRequests(leastAmount);
        if (matchRequests == null) {
            // getMatchRequests : 매칭 요청자의 총 수가 leastAmount보다 적을 경우 null을 리턴한다.
            if (System.currentTimeMillis() - lastMatchTime >= 1000) {
                // 1000 ms 동안  leastAmount를 체우지 못한 경우
                // currentMatchPoolFactor를 조정하여leastAmount의 크기를 줄인다.
                if (currentMatchPoolFactor > 1) {
                    currentMatchPoolFactor = Math.max(currentMatchPoolFactor / 2, 1);
                    logger.info("GamePartyMatchMaker.match() - reduce currentMatchPoolFactor: {}", currentMatchPoolFactor);
                }
            }
            return;
        }

        logger.info("GamePartyMatchMaker.match() - leastAmount: {}", leastAmount);

        // 조건에 맞는 요청들을 모아 매칭시킨다.
        // 여기에서는 rating을 100 단위 그룹으로 묶어 매칭을 시킨다.
        // 0~99, 100~199, 200~299 ...
        // 요청이 많을 경우 여기에서 그룹을 묶는 작업을 하는 것이 서버에 부하가 될 수 있다.
        // 그룹 별로 RoomType을 나누어 별도의 MatchMaker를 사용하는 방법도 고려해 보자.
        Map<Integer, List<GameUserMatchInfo>> entries = new TreeMap<>();
        for (GameUserMatchInfo info : matchRequests) {
            int ratingGroup = info.getRating() / 100;
            if (!entries.containsKey(ratingGroup)) {
                entries.put(ratingGroup, new ArrayList<>());
            }

            List<GameUserMatchInfo> subEntries = entries.get(ratingGroup);
            subEntries.add(info);
        }

        int totalMatchCount = 0;
        for (Map.Entry<Integer, List<GameUserMatchInfo>> subEntries : entries.entrySet()) {
            LinkedList<GameUserMatchInfo> entries1 = new LinkedList<>();
            LinkedList<GameUserMatchInfo> entries2 = new LinkedList<>();
            for (GameUserMatchInfo entry : subEntries.getValue()) {
                if (entry.getPartySize() == 2) {
                    entries2.add(entry);
                } else {
                    entries1.add(entry);
                }
            }

            logger.info("entries1 : {}, entries2 : {}", entries1.size(), entries2.size());

            while (entries2.size() >= 2) {
                LinkedList<GameUserMatchInfo> roomEntries = new LinkedList<>();
                roomEntries.add(entries2.removeFirst());
                roomEntries.add(entries2.removeFirst());
                assignRoom(roomEntries);
                totalMatchCount++;
            }

            while (entries2.size() >= 1 && entries1.size() >= 2) {
                LinkedList<GameUserMatchInfo> roomEntries = new LinkedList<>();
                roomEntries.add(entries2.removeFirst());
                roomEntries.add(entries1.removeFirst());
                roomEntries.add(entries1.removeFirst());
                assignRoom(roomEntries);
                totalMatchCount++;
            }

            if (entries1.size() >= matchSize) {
                totalMatchCount += matchSingles(entries1);
            }
        }

        if (totalMatchCount > 0) {
            logger.info("{} match(s) made", totalMatchCount);

            lastMatchTime = System.currentTimeMillis();
            currentMatchPoolFactor = matchPoolFactorMax;
        }
    }

    @Override
    public boolean refill(GameUserMatchInfo req) {
        logger.info("GamePartyMatchMaker.refill()");
        try {
            // 전체 리필 요청 목록
            List<GameUserMatchInfo> refillRequests = getRefillRequests();
            if (refillRequests.isEmpty()) {
                // 리필 요청 목록이 없을 경우
                logger.info("GamePartyMatchMaker.refill() - refillRequests.isEmpty");
                return false;
            }
            logger.info("GamePartyMatchMaker.refill() - RefillRequests : {}", refillRequests.size());
            int ratingGroup = req.getRating() / 100;
            for (GameUserMatchInfo refillInfo : refillRequests) {
                // ratingGroup 값이 같은 경우 리필
                if (ratingGroup == refillInfo.getRating() / 100) {
                    if (refillRoom(req, refillInfo)) { // 해당 매칭 요청을 리필이 필요한 방으로 매칭
                        logger.info("GamePartyMatchMaker.refill() - Refill success: {}", refillInfo.getId());
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("GamePartyMatchMaker::refill()", e);
        }
        return false;
    }
}
