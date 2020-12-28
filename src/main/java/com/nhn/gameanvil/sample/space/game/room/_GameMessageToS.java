package com.nhn.gameanvil.sample.space.game.room;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.node.game.RoomPacketHandler;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.sample.protocol.Sample;
import com.nhn.gameanvil.sample.protocol.Sample.GameMessageToS;
import com.nhn.gameanvil.sample.space.game.user.GameUser;
import com.nhn.gameanvil.sample.Defines.StringValues;
import org.slf4j.Logger;

public class _GameMessageToS implements RoomPacketHandler<GameRoom, GameUser> {

    private static final Logger logger = getLogger(_GameMessageToS.class);

    @Override
    public void execute(GameRoom gameRoom, GameUser gameUser, Packet packet) throws SuspendExecution {

        try {
            // parse.
            GameMessageToS fromClient = Sample.GameMessageToS.parseFrom(packet.getStream());

            // make.
            Sample.GameMessageToC.Builder toClient = Sample.GameMessageToC.newBuilder();
            toClient.setMessage("[" + gameUser.getUserId() + "] " + fromClient.getMessage());
            logger.info("_GameMessageToS - from : {}, msg : {}", gameUser.getUserId(), fromClient.getMessage());

            // for send to user.
            for (GameUser user : gameRoom.getUsers()) {
                user.send(new Packet(toClient));
            }

            // for send to spot.
            Sample.SampleToSpot.Builder toSpot = Sample.SampleToSpot.newBuilder().setFrom("" + gameUser.getUserId()).setMessage(fromClient.getMessage());
            gameUser.sendToSpot(StringValues.SampleServiceName, StringValues.SampleSpotType, StringValues.SampleSpotId, new Packet(toSpot));

        } catch (Exception e) {
            logger.error("_GameMessageToS::execute()", e);
        }
    }

}
