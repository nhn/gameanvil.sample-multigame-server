package com.nhnent.tardis.sample.space.game.user;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.Defines.StringValues;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

public class CmdResetSpot implements IPacketHandler<GameUser> {

    private static final Logger logger = getLogger(CmdResetSpot.class);

    @Override
    public void execute(GameUser gameUser, Packet packet) throws SuspendExecution {
        try {
            logger.info("CmdResetSpot - user : {}, packet : {}", gameUser.getUserId(), packet.getMsgName());

            // for send to spot.
            gameUser.sendToSpot(StringValues.SampleServiceName, StringValues.SampleSpotType, StringValues.SampleSpotId, packet);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
