package unit_test;

import com.nhnent.tardis.connector.common.Config;
import com.nhnent.tardis.connector.protocol.Packet;
import com.nhnent.tardis.connector.protocol.result.AuthenticationResult;
import com.nhnent.tardis.connector.tcp.ConnectorSession;
import com.nhnent.tardis.connector.tcp.TardisConnector;
import com.nhnent.tardis.sample.Defines.Messages;
import com.nhnent.tardis.sample.protocol.Sample;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class SessionAgentTest {

    private static TardisConnector connector;
    private ConnectorSession session = null;

    @BeforeClass
    public static void configuration() {

        // 테스트 하려는 서버의 IP 와 Port 를 지정합니다.
        Config.addRemoteInfo("127.0.0.1", 11200);
        Config.WAIT_RECV_TIMEOUT_MSEC = 3000;

        // 커넥터와, Base 프로토콜 사용 편의를 위해 Helper 를 생성합니다.
        connector = TardisConnector.getInstance();

        // 컨텐츠 프로토콜 등록.
        connector.addProtoBufClass(0, Sample.class);

        // 컨텐츠 서비스 등록.
        connector.addService(0, "ChatService");
    }

    @Before
    public void setUp() throws TimeoutException {

        session = connector.addSession(connector.getIncrementedValue("account_"), connector.makeUniqueId());
    }

    @After
    public void tearDown() throws TimeoutException {

        session.disconnect();
    }

    //-------------------------------------------------------------------------------------
    @Test
    public void authenticateSuccess() throws IOException, TimeoutException {

        AuthenticationResult authResult = session.authentication(session.getAccountId());
        assertTrue(authResult.isSuccess());
        //assertNull(authResult.getPayload(Sample.SampleData.class));
    }

    @Test
    public void authenticateSuccessWithPayload() throws IOException, TimeoutException {

        Sample.SampleData.Builder msgSnd = Sample.SampleData.newBuilder();
        msgSnd.setMessage(Messages.AuthenticatePayload);
        AuthenticationResult authResult = session.authentication(session.getAccountId(), msgSnd);
        assertTrue(authResult.isSuccess());

        Packet packet = authResult.getPayload(Sample.SampleData.class);
        if (null != packet) {
            try {
                Sample.SampleData msgRcv = Sample.SampleData.parseFrom(packet.getStream());
                assertEquals(Messages.AuthenticatePayload, msgRcv.getMessage());
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }else{
            fail("Payload contains no Sample.SampleData.");
        }
    }

    @Test
    public void authenticateFail() throws IOException, TimeoutException {

        AuthenticationResult authResult = session.authentication();
        assertTrue(authResult.isFailure());
        Packet packet= authResult.getPayload(Sample.SampleData.class);
        if (null != packet) {
            try {
                Sample.SampleData msg = Sample.SampleData.parseFrom(packet.getStream());
                assertEquals(msg.getMessage(), Messages.AuthenticateFail);
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }else{
            fail("Payload contains no Sample.SampleData.");
        }
    }

    @Test
    public void SampleReqToSession() throws IOException, TimeoutException {

        AuthenticationResult authResult = session.authentication(session.getAccountId());
        assertTrue(authResult.isSuccess());

        String message = "SampleReq";
        try{
            Packet packetRes = session.requestToSession(new Packet(Sample.SampleReq.newBuilder().setMessage(message)), Sample.SampleRes.class);
            Sample.SampleRes msg = Sample.SampleRes.parseFrom(packetRes.getStream());
            assertEquals(msg.getMessage(), message);
        } catch(Exception e){
            fail(e.toString());
        }

    }

    @Test
    public void BeforeAuthenticateReqToSessionBeforeAuthenticate() throws IOException, TimeoutException {

        String message = "BeforeAuthenticateReq";
        try {
            Packet packetRes = session.requestToSession(new Packet(Sample.BeforeAuthenticateReq.newBuilder().setMessage(message)), Sample.BeforeAuthenticateRes.class);
            Sample.BeforeAuthenticateRes msg = Sample.BeforeAuthenticateRes.parseFrom(packetRes.getStream());
            assertEquals(msg.getMessage(), message);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
}
