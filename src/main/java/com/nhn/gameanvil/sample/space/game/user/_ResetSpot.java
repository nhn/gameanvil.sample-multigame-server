package com.nhn.gameanvil.sample.space.game.user;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketHandler;
import com.nhn.gameanvil.sample.Defines.StringValues;
import org.slf4j.Logger;

public class _ResetSpot implements PacketHandler<GameUser> {

    private static final Logger logger = getLogger(_ResetSpot.class);

    @Override
    public void execute(GameUser gameUser, Packet packet) throws SuspendExecution {
        try {
            logger.info("_ResetSpot - user : {}, packet : {}", gameUser.getUserId(), packet.getMsgName());

            // for send to spot.
            gameUser.sendToSpot(StringValues.SampleServiceName, StringValues.SampleSpotType, StringValues.SampleSpotId, packet);
        } catch (Exception e) {
            logger.error("_ResetSpot::execute()", e);
        }
    }
}
