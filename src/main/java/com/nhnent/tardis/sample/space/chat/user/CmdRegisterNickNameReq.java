package com.nhnent.tardis.sample.space.chat.user;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import com.nhnent.tardis.sample.protocol.Sample;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdRegisterNickNameReq implements IPacketHandler<ChatUser> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(ChatUser chatUser, Packet packet) throws SuspendExecution {

        try{
            Sample.RegisterNickNameReq req = Sample.RegisterNickNameReq.parseFrom(packet.getStream());

            chatUser.setNickName(req.getNickName());

            Sample.RegisterNickNameRes.Builder res = Sample.RegisterNickNameRes.newBuilder().setIsSuccess(true);
            chatUser.reply(new Packet(res.build()));
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));

            Sample.RegisterNickNameRes.Builder res = Sample.RegisterNickNameRes.newBuilder().setIsSuccess(true);
            chatUser.reply(new Packet(res.build()));
        }
    }
}
