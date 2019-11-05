package com.nhnent.tardis.sample.session;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.ITimerHandler;
import com.nhnent.tardis.common.internal.ITimerObject;
import com.nhnent.tardis.common.internal.PauseType;
import com.nhnent.tardis.console.PacketDispatcher;
import com.nhnent.tardis.console.session.ISession;
import com.nhnent.tardis.console.session.SessionAgent;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.Defines.Messages;
import com.nhnent.tardis.sample.session.handlers.BeforeAuthenticateReqSessionAgentHandler;
import com.nhnent.tardis.sample.session.handlers.SampleReqSessionAgentPacketHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;



public class SampleSessionAgent extends SessionAgent implements ISession<SampleSessionUserAgent>, ITimerHandler {

    private static PacketDispatcher dispatcher = new PacketDispatcher();
    static{
        dispatcher.registerMsg(Sample.BeforeAuthenticateReq.class, BeforeAuthenticateReqSessionAgentHandler.class);
        dispatcher.registerMsg(Sample.SampleReq.class, SampleReqSessionAgentPacketHandler.class);
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean checkPreAccess(final Packet packet) throws SuspendExecution {
        return packet.msgEquals(Sample.BeforeAuthenticateReq.getDescriptor().getFullName());
    }

    @Override
    public boolean onAuthenticate(String accoutId, String password, String deviceId, Payload payload, Payload outPayload) throws SuspendExecution {
        // 인증 로직 구현부. 여기서는 accountId와 password가 일치 할 경우 인증 성공.
        if(accoutId.equals(password)){
            logger.info("onAuthenticate success. id:{}, pw:{}, device:{}", accoutId, password, deviceId);

            // payload 로부터 원하는 Packet 가져오기.
            Packet packetSampleData = payload.getPacket(Sample.SampleData.getDescriptor());
            if (null != packetSampleData) {
                try {
                    Sample.SampleData msg = Sample.SampleData.parseFrom(packetSampleData.getStream());
                    String message = msg.getMessage();
                    logger.trace("\tpayload - SampleData : {}", message);

                    // client로 Packet 전달.
                    Packet packetToClient = new Packet(Sample.SampleData.newBuilder().setMessage(message));
                    outPayload.add(packetToClient);
                } catch (IOException e) {
                    logger.error(ExceptionUtils.getStackTrace(e));
                }
            }

            return true;
        }else{
            logger.info("onAuthenticate fail. password must be same as accountId. id:{}, pw:{}, device:{}", accoutId, password, deviceId);

            // client로 Packet 전달.
            Packet packetToClient = new Packet(Sample.SampleData.newBuilder().setMessage(Messages.AuthenticateFail));
            outPayload.add(packetToClient);

            return false;
        }
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        dispatcher.dispatch(this, packet);
    }

    @Override
    public void onPreLogin(Payload outPayload) throws SuspendExecution {
        logger.info("onPreLogin");
    }

    @Override
    public void onPostLogin(SampleSessionUserAgent session) throws SuspendExecution {
        logger.info("onPostLogin");
    }

    @Override
    public void onPostLogout(SampleSessionUserAgent session) throws SuspendExecution {
        logger.info("onPostLogout");
    }

    @Override
    public void onPause(PauseType type) throws SuspendExecution {
        logger.info("onPause");
    }

    @Override
    public void onResume() throws SuspendExecution {
        logger.info("onResume");
    }

    @Override
    public void onDisconnect() throws SuspendExecution {
        logger.info("onDisconnect");
    }

    @Override
    public void onTimer(ITimerObject timerObject, Object arg) throws SuspendExecution {
        logger.info("onTimer");
    }
}
