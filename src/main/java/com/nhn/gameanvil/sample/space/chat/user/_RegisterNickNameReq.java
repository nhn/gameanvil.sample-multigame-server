package com.nhn.gameanvil.sample.space.chat.user;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketHandler;
import com.nhn.gameanvil.sample.protocol.Sample;
import com.nhn.gameanvil.sample.protocol.Sample.RegisterNickNameReq;
import org.slf4j.Logger;

public class _RegisterNickNameReq implements PacketHandler<ChatUser> {

    private static final Logger logger = getLogger(_RegisterNickNameReq.class);

    @Override
    public void execute(ChatUser chatUser, Packet packet) throws SuspendExecution {

        try {
            RegisterNickNameReq req = Sample.RegisterNickNameReq.parseFrom(packet.getStream());

            chatUser.setNickName(req.getNickName());

            Sample.RegisterNickNameRes.Builder res = Sample.RegisterNickNameRes.newBuilder().setIsSuccess(true);
            chatUser.reply(new Packet(res.build()));
        } catch (Exception e) {
            logger.error("_RegisterNickNameReq::execute()", e);

            Sample.RegisterNickNameRes.Builder res = Sample.RegisterNickNameRes.newBuilder().setIsSuccess(true);
            chatUser.reply(new Packet(res.build()));
        }
    }
}
