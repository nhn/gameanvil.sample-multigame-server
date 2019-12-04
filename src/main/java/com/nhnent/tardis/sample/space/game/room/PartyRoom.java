package com.nhnent.tardis.sample.space.game.room;

import static com.nhnent.tardis.common.internal.BaseNode.getServiceName;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.console.space.IRoom;
import com.nhnent.tardis.console.space.RoomAgent;
import com.nhnent.tardis.console.space.RoomPacketDispatcher;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.space.game.match.GameUserMatchInfo;
import com.nhnent.tardis.sample.space.game.user.GameUser;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartyRoom extends RoomAgent implements IRoom<GameUser> {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected static RoomPacketDispatcher dispatcher = new RoomPacketDispatcher();

    static {
        dispatcher.registerMsg(Sample.GameMessageToS.class, CmdGameMessageToS.class);
    }

    protected Map<String, GameUser> users = new HashMap<>();

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("PartyRoom.onInit - RoomId : {}", getId());
    }

    @Override
    public void onDestroy() throws SuspendExecution {
        logger.info("PartyRoom.onDestroy - RoomId : {}", getId());
    }

    @Override
    public void onDispatch(GameUser gameUser, Packet packet) throws SuspendExecution {
        logger.info("PartyRoom.onDispatch : RoomId : {}, UserId : {}, {}",
            getId(),
            gameUser.getUserId(),
            TardisIndexer.getMsgName(packet.getDescId(), packet.getMsgIndex()));
        dispatcher.dispatch(this, gameUser, packet);
    }

    @Override
    public boolean onCreateRoom(GameUser gameUser, Payload inPayload, Payload outPayload)
        throws SuspendExecution {
        logger.info("PartyRoom.onCreateRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
        users.put(gameUser.getUserId(), gameUser);
        return true;
    }

    @Override
    public boolean onJoinRoom(GameUser gameUser, Payload inPayload, Payload outPayload)
        throws SuspendExecution {
        logger.info("PartyRoom.onJoinRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
        if (isInProgressOfUserMatchMaking()) {
            return false;
        }
        String message = String.format("%s is join", gameUser.getUserId());
        for (GameUser user : users.values()) {
            user.send(new Packet(Sample.GameMessageToC.newBuilder().setMessage(message)));
            logger.info("PartyRoom.onJoinRoom - to {} : {}", user.getUserId(), message);
        }
        users.put(gameUser.getUserId(), gameUser);
        return true;
    }

    @Override
    public boolean onLeaveRoom(GameUser gameUser, Payload inPayload, Payload outPayload)
        throws SuspendExecution {
        logger.info("PartyRoom.onLeaveRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
        if (isInProgressOfUserMatchMaking()) {
            return false;
        }

        users.remove(gameUser.getUserId());
        return true;
    }

    @Override
    public void onPostLeaveRoom(GameUser gameUser) throws SuspendExecution {
        logger.info("PartyRoom.onPostLeaveRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
    }

    @Override
    public void onRejoinRoom(GameUser gameUser, Payload outPayload) throws SuspendExecution {
        logger.info("PartyRoom.onRejoinRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
        String message = String.format("%s is back", gameUser.getUserId());
        for (GameUser user : users.values()) {
            user.send(new Packet(Sample.GameMessageToC.newBuilder().setMessage(message)));
        }
    }

    @Override
    public boolean canTransfer() throws SuspendExecution {
        logger.info("PartyRoom.canTransfer - RoomId : {}", getId());
        return false;
    }

    /**
     * client 에서 partyMatch 를 요청했을 경우 호출되는 callback
     *
     * @param user       : 파티 매칭을 요청한 방장
     * @param payload    : client 의 요청시 추가적으로 전달되는 data
     * @param outPayload : 서버에서 client 로 전달되는 data
     * @return : true: user matching 요청 성공,false: user matching 요청 실패
     * @throws SuspendExecution
     */
    @Override
    public final boolean onMatchParty(final String roomType, final GameUser user, final Payload payload, Payload outPayload) throws SuspendExecution {
        logger.info("PartyRoom.onMatchParty - RoomId : {}, roomType : {}", getId(), roomType);
        try {

            String matchingGroup = getServiceName();
            ;
            GameUserMatchInfo term = new GameUserMatchInfo(getId(), 100, users.size());
            if (matchParty(matchingGroup, roomType, term, payload)) {
                logger.info("PartyRoom.onMatchParty - {} start Party Match for RoomType {}", user.getUserId(), roomType);
                return true;
            }

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }

        return false;
    }
}
