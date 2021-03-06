package com.nhn.gameanvil.sample.space.game.room;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.node.game.BaseRoom;
import com.nhn.gameanvil.node.game.RoomPacketDispatcher;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.sample.protocol.Sample;
import com.nhn.gameanvil.sample.space.game.user.GameUser;
import com.nhn.gameanvil.sample.space.game.match.GameUserMatchInfo;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;

public class PartyRoom extends BaseRoom<GameUser> {
    private static final Logger logger = getLogger(PartyRoom.class);

    protected static RoomPacketDispatcher dispatcher = new RoomPacketDispatcher();

    static {
        dispatcher.registerMsg(Sample.GameMessageToS.getDescriptor(), _GameMessageToS.class);
    }

    protected Map<Integer, GameUser> users = new HashMap<>();

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
            packet.getMsgName());
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
     * client ?????? partyMatch ??? ???????????? ?????? ???????????? callback
     *
     * @param user       : ?????? ????????? ????????? ??????
     * @param payload    : client ??? ????????? ??????????????? ???????????? data
     * @param outPayload : ???????????? client ??? ???????????? data
     * @return : true: user matching ?????? ??????,false: user matching ?????? ??????
     * @throws SuspendExecution
     */
    @Override
    public final boolean onMatchParty(final String roomType, final GameUser user, final Payload payload, Payload outPayload) throws SuspendExecution {
        logger.info("PartyRoom.onMatchParty - RoomId : {}, roomType : {}", getId(), roomType);
        try {

            String matchingGroup = roomType;
            GameUserMatchInfo term = new GameUserMatchInfo(getId(), 100, users.size());
            if (matchParty(matchingGroup, roomType, term, payload)) {
                logger.info("PartyRoom.onMatchParty - {} start Party Match for RoomType {}", user.getUserId(), roomType);
                return true;
            }

        } catch (Exception e) {
            logger.error("PartyRoom::onMatchParty()", e);
        }

        return false;
    }
}
