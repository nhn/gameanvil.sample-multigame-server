package com.nhnent.tardis.sample.space.chat.user;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.ITimerHandler;
import com.nhnent.tardis.common.internal.ITimerObject;
import com.nhnent.tardis.common.internal.PauseType;
import com.nhnent.tardis.common.serializer.KryoSerializer;
import com.nhnent.tardis.console.PacketDispatcher;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.console.space.IUser;
import com.nhnent.tardis.console.space.UserAgent;
import com.nhnent.tardis.sample.Defines.StringValues;
import com.nhnent.tardis.sample.protocol.Sample;
import java.util.Arrays;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.ByteBuffer;

public class ChatUser extends UserAgent implements IUser, ITimerHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String nickName = "";

    static private PacketDispatcher<ChatUser> packetDispatcher = new PacketDispatcher();

    static{
        packetDispatcher.registerMsg(Sample.RegisterNickNameReq.class,
            RegisterNickNameReqPacketHandler.class);
    }


    @Override
    public boolean onLogin(Payload payload, Payload sessionPayload, Payload outPayload) throws SuspendExecution {
        logger.info("ChatUser.onLogin - UserId : {}", getUserId());
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
        logger.info("ChatUser.onReLogin - UserId : {}", getUserId());
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
            TardisIndexer.getMsgName(packet.getDescId(), packet.getMsgIndex()), getUserId());
        packetDispatcher.dispatch(this,packet);
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
    public void onTimer(ITimerObject iTimerObject, Object o) throws SuspendExecution {
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
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        if(!nickName.isEmpty()){
            return nickName;
        }
        return getUserId();
    }
}
