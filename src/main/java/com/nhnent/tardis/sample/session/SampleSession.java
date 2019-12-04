package com.nhnent.tardis.sample.session;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.PauseType;
import com.nhnent.tardis.console.PacketDispatcher;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.console.session.ISession;
import com.nhnent.tardis.console.session.SessionAgent;
import com.nhnent.tardis.sample.Defines.StringValues;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.handlers.SessionAgentRemoveTimerPacketHandler;
import com.nhnent.tardis.sample.session.handlers.SessionAgentSampleReqPacketHandler;
import com.nhnent.tardis.sample.session.handlers.SessionAgentSampleToSPacketHandler;
import com.nhnent.tardis.sample.session.handlers.SessionAgentSetTimerPacketHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class SampleSession extends SessionAgent implements ISession<SampleSessionUser> {

    private static PacketDispatcher dispatcher = new PacketDispatcher();

    static {
        dispatcher.registerMsg(Sample.SampleReq.class, SessionAgentSampleReqPacketHandler.class);
        dispatcher.registerMsg(Sample.SampleToS.class, SessionAgentSampleToSPacketHandler.class);
        dispatcher.registerMsg(Sample.SetTimer.class, SessionAgentSetTimerPacketHandler.class);
        dispatcher.registerMsg(Sample.RemoveTimer.class, SessionAgentRemoveTimerPacketHandler.class);
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean onAuthenticate(String accountId, String password, String deviceId,
        Payload payload, Payload outPayload) throws SuspendExecution {
        logger.info(
            "SampleSession.onAuthenticate - accountId : {}, password : {}, deviceId : {}",
            accountId, password, deviceId);
        // 인증 로직 구현부. 여기서는 accountId와 password가 일치 할 경우 인증 성공.
        if (accountId.equals(password)) {
            logger.info("onAuthenticate success. id:{}, pw:{}, device:{}", accountId, password,
                deviceId);

            // payload 로부터 원하는 Packet 가져오기.
            Packet packetSampleData = payload.getPacket(Sample.SampleData.getDescriptor());
            if (null != packetSampleData) {
                try {
                    Sample.SampleData msg = Sample.SampleData
                        .parseFrom(packetSampleData.getStream());
                    String message = msg.getMessage();
                    logger.trace("\tpayload - SampleData : {}", message);

                    // client로 Packet 전달.
                    Packet packetToClient = new Packet(
                        Sample.SampleData.newBuilder().setMessage(message));
                    outPayload.add(packetToClient);
                } catch (IOException e) {
                    logger.error(ExceptionUtils.getStackTrace(e));
                }
            }

            SampleSessionNode sampleSessionNode = SampleSessionNode.getInstance();
            sampleSessionNode.addSampleSession(this);

            return true;
        } else {
            logger.info(
                "onAuthenticate fail. password must be same as accountId. id:{}, pw:{}, device:{}",
                accountId, password, deviceId);

            // client로 Packet 전달.
            Packet packetToClient = new Packet(
                Sample.SampleData.newBuilder().setMessage(StringValues.AuthenticateFail));
            outPayload.add(packetToClient);

            return false;
        }
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("SampleSession.onDispatch : {}",
            TardisIndexer.getMsgName(packet.getDescId(), packet.getMsgIndex()));
        dispatcher.dispatch(this, packet);
    }

    @Override
    public void onPreLogin(Payload outPayload) throws SuspendExecution {
        logger.info("SampleSession.onPreLogin");
        outPayload.add(new Packet(Sample.SampleData.newBuilder().setMessage("onPreLogin")));
    }

    @Override
    public void onPostLogin(SampleSessionUser session) throws SuspendExecution {
        logger.info("SampleSession.onPostLogin : {}", session.getUserId());
    }

    @Override
    public void onPostLogout(SampleSessionUser session) throws SuspendExecution {
        logger.info("SampleSession.onPostLogout : {}", session.getUserId());
    }

    @Override
    public void onPause(PauseType type) throws SuspendExecution {
        logger.info("SampleSession.onPause : {}", type);
    }

    @Override
    public void onResume() throws SuspendExecution {
        logger.info("SampleSession.onResume");
    }

    @Override
    public void onDisconnect() throws SuspendExecution {
        logger.info("SampleSession.onDisconnect");
        SampleSessionNode sampleSessionNode = SampleSessionNode.getInstance();
        sampleSessionNode.removeSampleSession(this);
    }
}
