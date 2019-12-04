package com.nhnent.tardis.sample.space.game.room;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.ITimerHandler;
import com.nhnent.tardis.common.internal.ITimerObject;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.console.space.IRoom;
import com.nhnent.tardis.console.space.RoomAgent;
import com.nhnent.tardis.console.space.RoomPacketDispatcher;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.space.game.user.GameUser;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameRoom extends RoomAgent implements IRoom<GameUser>, ITimerHandler {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected static RoomPacketDispatcher dispatcher = new RoomPacketDispatcher();

    static {
        dispatcher.registerMsg(Sample.GameMessageToS.class, CmdGameMessageToS.class);
    }

    protected Map<String, GameUser> users = new TreeMap<>();

    @Override
    public void onInit() throws SuspendExecution {
        logger.info("GameRoom.onInit - RoomId : {}", getId());
    }

    @Override
    public void onDestroy() throws SuspendExecution {
        logger.info("GameRoom.onDestroy - RoomId : {}", getId());
    }

    @Override
    public void onDispatch(GameUser gameUser, Packet packet) throws SuspendExecution {
        logger.info("GameRoom.onDispatch : RoomId : {}, UserId : {}, {}",
            getId(),
            gameUser.getUserId(),
            TardisIndexer.getMsgName(packet.getDescId(), packet.getMsgIndex()));
        dispatcher.dispatch(this, gameUser, packet);
    }

    @Override
    public boolean onCreateRoom(GameUser gameUser, Payload inPayload, Payload outPayload)
        throws SuspendExecution {
        logger.info("GameRoom.onCreateRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
        return false;
    }

    @Override
    public boolean onJoinRoom(GameUser gameUser, Payload inPayload, Payload outPayload)
        throws SuspendExecution {
        logger.info("GameRoom.onJoinRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
        return false;
    }

    @Override
    public boolean onLeaveRoom(GameUser gameUser, Payload inPayload, Payload outPayload)
        throws SuspendExecution {
        logger.info("GameRoom.onLeaveRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
        return false;
    }

    @Override
    public void onPostLeaveRoom(GameUser gameUser) throws SuspendExecution {
        logger.info("GameRoom.onPostLeaveRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
        String message = String.format("%s is leave", gameUser.getUserId());
        for (GameUser user : users.values()) {
            user.send(new Packet(Sample.GameMessageToC.newBuilder().setMessage(message)));
        }
    }

    @Override
    public void onRejoinRoom(GameUser gameUser, Payload outPayload) throws SuspendExecution {
        logger.info("GameRoom.onRejoinRoom - RoomId : {}, UserId : {}", getId(),
            gameUser.getUserId());
        String message = String.format("%s is back", gameUser.getUserId());
        for (GameUser user : users.values()) {
            user.send(new Packet(Sample.GameMessageToC.newBuilder().setMessage(message)));
        }
    }

    @Override
    public boolean canTransfer() throws SuspendExecution {
        logger.info("GameRoom.canTransfer - RoomId : {}", getId());
        return false;
    }

    @Override
    public void onTimer(ITimerObject iTimerObject, Object arg) throws SuspendExecution {
        logger.info("GameRoom.onTimer - RoomId : {}", getId());
    }

    //-------------------------------------------------------------------------

    public Collection<GameUser> getUsers() {
        return users.values();
    }
}
