package com.nhnent.tardis.sample.session;

import static org.slf4j.LoggerFactory.getLogger;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhn.gameanvil.define.PauseType;
import com.nhn.gameanvil.node.gateway.BaseConnection;
import com.nhn.gameanvil.packet.Packet;
import com.nhn.gameanvil.packet.PacketDispatcher;
import com.nhn.gameanvil.packet.Payload;
import com.nhnent.tardis.sample.Defines.StringValues;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.session.cmd.CmdSessionAgentRemoveTimer;
import com.nhnent.tardis.sample.session.cmd.CmdSessionAgentSampleReq;
import com.nhnent.tardis.sample.session.cmd.CmdSessionAgentSampleToS;
import com.nhnent.tardis.sample.session.cmd.CmdSessionAgentSetTimer;
import java.io.IOException;
import org.slf4j.Logger;


public class SampleConnection extends BaseConnection<SampleSessionUser> {

    private static final Logger logger = getLogger(SampleConnection.class);
    private static PacketDispatcher packetDispatcher = new PacketDispatcher();

    static {
        packetDispatcher.registerMsg(Sample.SampleReq.getDescriptor(), CmdSessionAgentSampleReq.class);
        packetDispatcher.registerMsg(Sample.SampleToS.getDescriptor(), CmdSessionAgentSampleToS.class);
        packetDispatcher.registerMsg(Sample.SetTimer.getDescriptor(), CmdSessionAgentSetTimer.class);
        packetDispatcher.registerMsg(Sample.RemoveTimer.getDescriptor(), CmdSessionAgentRemoveTimer.class);
    }

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
                    logger.error("SampleSession::onAuthenticate()", e);
                }
            }

            SampleGatewayNode sampleGatewayNode = SampleGatewayNode.getInstance();
            sampleGatewayNode.addSampleSession(this);

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
            packet.getMsgName());
        packetDispatcher.dispatch(this, packet);
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
        SampleGatewayNode sampleGatewayNode = SampleGatewayNode.getInstance();
        sampleGatewayNode.removeSampleSession(this);
    }
}
