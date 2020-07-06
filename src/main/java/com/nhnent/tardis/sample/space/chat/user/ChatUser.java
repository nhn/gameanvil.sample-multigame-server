package com.nhnent.tardis.sample.space.chat.user;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.define.PauseType;
import com.nhn.gameanvil.node.game.BaseUser;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketDispatcher;
import com.nhn.gameanvil.packet.Payload;
import com.nhn.gameanvil.serializer.KryoSerializer;
import com.nhn.gameanvil.timer.Timer;
import com.nhn.gameanvil.timer.TimerHandler;
import com.nhnent.tardis.sample.Defines.StringValues;
import com.nhnent.tardis.sample.protocol.Sample;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;

public class ChatUser extends BaseUser implements TimerHandler {

    private static final Logger logger = getLogger(ChatUser.class);
    private static PacketDispatcher<ChatUser> packetDispatcher = new PacketDispatcher();

    static {
        packetDispatcher.registerMsg(Sample.RegisterNickNameReq.getDescriptor(), CmdRegisterNickNameReq.class);
    }

    private String nickName = "";

    @Override
    public boolean onLogin(Payload payload, Payload sessionPayload, Payload outPayload) throws SuspendExecution {
        logger.info("ChatUser.onLogin - UserId : {}, NickName : {}", getUserId(), nickName);
        addClientTopics(Arrays.asList(StringValues.ChatServiceName));
        outPayload.add(new Packet(Sample.UserInfo.newBuilder().setNickName(nickName)));
        return true;
    }

    @Override
    public void onPostLogin() throws SuspendExecution {
        logger.info("ChatUser.onPostLogin - UserId : {}", getUserId());
    }

    @Override
    public boolean onReLogin(Payload payload, Payload sessionPayload, Payload outPayload) throws SuspendExecution {
        logger.info("ChatUser.onReLogin - UserId : {}, NickName : {}", getUserId(), nickName);
        outPayload.add(new Packet(Sample.UserInfo.newBuilder().setNickName(nickName)));
        return true;
    }

    @Override
    public void onDisconnect() throws SuspendExecution {
        logger.info("ChatUser.onDisconnect - UserId : {}", getUserId());
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("ChatUser.onDispatch : {} , {}",
            packet.getMsgName(), getUserId());
        packetDispatcher.dispatch(this, packet);
    }

    @Override
    public void onPause(PauseType pauseType) throws SuspendExecution {
        logger.info("ChatUser.onPause - UserId : {}", getUserId());
    }

    @Override
    public void onResume() throws SuspendExecution {
        logger.info("ChatUser.onResume - UserId : {}", getUserId());
    }

    @Override
    public void onLogout(Payload payload, Payload outPayload) throws SuspendExecution {
        logger.info("ChatUser.onLogout - UserId : {}", getUserId());
    }

    @Override
    public boolean canLogout() throws SuspendExecution {
        logger.info("ChatUser.canLogout - UserId : {}", getUserId());
        return true;
    }

    @Override
    public boolean canTransfer() throws SuspendExecution {
        logger.info("ChatUser.canTransfer - UserId : {}", getUserId());
        return false;
    }

    @Override
    public void onTimer(Timer timer, Object o) throws SuspendExecution {
        logger.info("ChatUser.onTimer : {}", getUserId());
    }

    @Override
    public ByteBuffer onTransferOut() throws SuspendExecution {
        logger.info("ChatUser.onTransferOut - UserId : {}", getUserId());
        return KryoSerializer.write(nickName);
    }

    @Override
    public void onTransferIn(final InputStream inputStream) throws SuspendExecution {
        logger.info("ChatUser.onTransferIn - UserId : {}", getUserId());
        try {
            nickName = (String) KryoSerializer.read(inputStream);
        } catch (Exception e) {
            logger.error("ChatUser::onTransferIn()", e);
        }
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        if (!nickName.isEmpty()) {
            return nickName;
        }
        return "" + getUserId();
    }
}
