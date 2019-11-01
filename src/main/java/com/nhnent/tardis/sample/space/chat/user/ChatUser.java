package com.nhnent.tardis.sample.space.chat.user;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.sample.protocol.Chat;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.ITimerHandler;
import com.nhnent.tardis.common.internal.ITimerObject;
import com.nhnent.tardis.common.internal.PauseType;
import com.nhnent.tardis.common.serializer.KryoSerializer;
import com.nhnent.tardis.console.PacketDispatcher;
import com.nhnent.tardis.console.space.IUser;
import com.nhnent.tardis.console.space.UserAgent;
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
        packetDispatcher.registerMsg(Chat.RegisterNickNameReq.class,CmdRegisterNickNameReq.class);
    }


    @Override
    public boolean onLogin(Payload payload, Payload payload1, Payload payload2) throws SuspendExecution {
        return true;
    }

    @Override
    public void onPostLogin() throws SuspendExecution {

    }

    @Override
    public boolean onReLogin(Payload payload, Payload payload1, Payload payload2) throws SuspendExecution {
        return true;
    }

    @Override
    public void onDisconnect() throws SuspendExecution {

    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        packetDispatcher.dispatch(this,packet);
    }

    @Override
    public void onPause(PauseType pauseType) throws SuspendExecution {

    }

    @Override
    public void onResume() throws SuspendExecution {

    }

    @Override
    public void onLogout(Payload payload, Payload outPayload) throws SuspendExecution {

    }

    @Override
    public boolean canLogout() throws SuspendExecution {
        return true;
    }

    @Override
    public boolean canTransfer() throws SuspendExecution {
        return false;
    }

    @Override
    public void onTimer(ITimerObject iTimerObject, Object o) throws SuspendExecution {

    }

    @Override
    public ByteBuffer onTransferOut() throws SuspendExecution {
        return KryoSerializer.write(nickName);
    }

    @Override
    public void onTransferIn(final InputStream inputStream) throws SuspendExecution {
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
