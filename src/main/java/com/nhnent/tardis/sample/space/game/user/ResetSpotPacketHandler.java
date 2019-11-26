package com.nhnent.tardis.sample.space.game.user;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.Defines.StringValues;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResetSpotPacketHandler implements IPacketHandler<GameUser> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(GameUser gameUser, Packet packet) throws SuspendExecution {
        logger.info("ResetSpotPacketHandler - user : {}, packet : {}", gameUser.getUserId(), packet.getMsgName());
        try {
            // for send to spot.
            gameUser.sendToSpot(StringValues.SampleServiceName, StringValues.SampleSpotType, StringValues.SampleSpotId, packet);

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

}
