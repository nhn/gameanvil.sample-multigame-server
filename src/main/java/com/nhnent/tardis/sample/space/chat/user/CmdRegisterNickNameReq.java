package com.nhnent.tardis.sample.space.chat.user;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.sample.protocol.Chat;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.console.IPacketHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdRegisterNickNameReq implements IPacketHandler<ChatUser> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(ChatUser chatUser, Packet packet) throws SuspendExecution {

        Chat.RegisterNickNameRes.Builder res = Chat.RegisterNickNameRes.newBuilder().setIsSuccess(false);
        try{
            Chat.RegisterNickNameReq req = Chat.RegisterNickNameReq.parseFrom(packet.getStream());

            chatUser.setNickName(req.getNickName());
            res.setIsSuccess(true);

        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
        }

        chatUser.reply(new Packet(res.build()));
    }
}
