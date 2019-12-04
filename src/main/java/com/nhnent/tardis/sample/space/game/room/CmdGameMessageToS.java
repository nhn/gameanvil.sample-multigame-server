package com.nhnent.tardis.sample.space.game.room;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.space.IRoomPacketHandler;
import com.nhnent.tardis.sample.Defines.StringValues;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.space.game.user.GameUser;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdGameMessageToS implements IRoomPacketHandler<GameRoom, GameUser> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(GameRoom gameRoom, GameUser gameUser, Packet packet) throws SuspendExecution {

        try {
            // parse.
            Sample.GameMessageToS fromClient = Sample.GameMessageToS.parseFrom(packet.getStream());

            // make.
            Sample.GameMessageToC.Builder toClient = Sample.GameMessageToC.newBuilder();
            toClient.setMessage("[" + gameUser.getUserId() + "] " + fromClient.getMessage());
            logger.info("CmdGameMessageToS - from : {}, msg : {}", gameUser.getUserId(), fromClient.getMessage());

            // for send to user.
            for (GameUser user : gameRoom.getUsers()) {
                user.send(new Packet(toClient));
            }

            // for send to spot.
            Sample.SampleToSpot.Builder toSpot = Sample.SampleToSpot.newBuilder().setFrom(gameUser.getUserId()).setMessage(fromClient.getMessage());
            gameUser.sendToSpot(StringValues.SampleServiceName, StringValues.SampleSpotType, StringValues.SampleSpotId, new Packet(toSpot));

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

}
