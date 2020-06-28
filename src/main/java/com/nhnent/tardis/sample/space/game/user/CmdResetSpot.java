package com.nhnent.tardis.sample.space.game.user;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameflex.packet.Packet;
import com.nhn.gameflex.packet.PacketHandler;
import com.nhnent.tardis.sample.Defines.StringValues;
import org.slf4j.Logger;

public class CmdResetSpot implements PacketHandler<GameUser> {

    private static final Logger logger = getLogger(CmdResetSpot.class);

    @Override
    public void execute(GameUser gameUser, Packet packet) throws SuspendExecution {
        try {
            logger.info("CmdResetSpot - user : {}, packet : {}", gameUser.getUserId(), packet.getMsgName());

            // for send to spot.
            gameUser.sendToSpot(StringValues.SampleServiceName, StringValues.SampleSpotType, StringValues.SampleSpotId, packet);
        } catch (Exception e) {
            logger.error("CmdResetSpot::execute()", e);
        }
    }
}
