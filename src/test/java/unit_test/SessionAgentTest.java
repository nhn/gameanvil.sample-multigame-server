package unit_test;

import com.nhnent.tardis.connector.common.Config;
import com.nhnent.tardis.connector.protocol.Packet;
import com.nhnent.tardis.connector.protocol.result.AuthenticationResult;
import com.nhnent.tardis.connector.protocol.result.ChannelListResult;
import com.nhnent.tardis.connector.tcp.ConnectorSession;
import com.nhnent.tardis.connector.tcp.TardisConnector;
import com.nhnent.tardis.sample.Defines.Messages;
import com.nhnent.tardis.sample.protocol.Sample;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
        // accountId와 password가 일치할 때 성공
        AuthenticationResult authResult = session.authentication(session.getAccountId());
        assertTrue(authResult.isSuccess());
        assertNull(authResult.getPayload(Sample.SampleData.class));
    }

    @Test
    public void authenticateSuccessWithPayload() throws IOException, TimeoutException {
        // payload를 보낼 경우, 보낸 값을 그대로 되돌려 받아야함.
        Sample.SampleData.Builder payloadSnd = Sample.SampleData.newBuilder();
        payloadSnd.setMessage(Messages.AuthenticatePayload);

        AuthenticationResult authResult = session.authentication(session.getAccountId(), payloadSnd);
        assertTrue(authResult.isSuccess());

        Packet packet = authResult.getPayload(Sample.SampleData.class);
        if (null != packet) {
            try {
                Sample.SampleData payloadRcv = Sample.SampleData.parseFrom(packet.getStream());
                assertEquals(Messages.AuthenticatePayload, payloadRcv.getMessage());
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }else{
            fail("Payload contains no Sample.SampleData.");
        }
    }

    @Test
    public void authenticateFail() throws IOException, TimeoutException {
        // accountId와 password가 일치하지 않으면 실패.
        AuthenticationResult authResult = session.authentication();
        assertTrue(authResult.isFailure());
        Packet packet= authResult.getPayload(Sample.SampleData.class);
        if (null != packet) {
            try {
                // 실패일 경우 payload에 실패 메시지를 보냄
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

        authenticateSuccess();

        String message = "SampleReq";
        try{
            //SampleReq 를 보낼 경우 보낸 값을 그대로 돌려 받음.
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
            // authenticate 하기 전에도 BeforeAuthenticateReq를 보내고 BeforeAuthenticateRes 응답을 받을 수 있음.
            // BeforeAuthenticateReq 를 보낼 경우 보낸 값을 그대로 돌려 받음.
            Packet packetRes = session.requestToSession(new Packet(Sample.BeforeAuthenticateReq.newBuilder().setMessage(message)), Sample.BeforeAuthenticateRes.class);
            Sample.BeforeAuthenticateRes msg = Sample.BeforeAuthenticateRes.parseFrom(packetRes.getStream());
            assertEquals(msg.getMessage(), message);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void SampleToSToSession() throws IOException, TimeoutException {

        authenticateSuccess();

        String message = "SampleToS";
        try{
            //SampleToS 를 보낼 경우 보낸 값을 그대로 돌려 받음.
            session.sendToSession(new Packet(Sample.SampleToS.newBuilder().setMessage(message)));
            Packet packetSampleToC = session.waitPacket(1, TimeUnit.SECONDS, Sample.SampleToC.class);
            Sample.SampleToC msg = Sample.SampleToC.parseFrom(packetSampleToC.getStream());
            assertEquals(msg.getMessage(), message);
        } catch(Exception e){
            fail(e.toString());
        }
    }

    @Test
    public void getChannelList() throws IOException, TimeoutException {

        authenticateSuccess();

        ChannelListResult result = session.channelList("ChatService");
        assertTrue(result.isSuccess());

        // TardisConfig에 설정된 체널 정보는 ["1","2","3","4","1","2","3","4"]
        // 응답에서는 중복제거, 정렬된 ["1","2","3","4"]로 내려와야함.
        List<String> list = result.getChannelList();
        assertEquals(4, list.size());
        assertEquals("1", list.get(0));
        assertEquals("2", list.get(1));
        assertEquals("3", list.get(2));
        assertEquals("4", list.get(3));
    }

    @Test
    public void getChannelInfo() throws IOException, TimeoutException {

        authenticateSuccess();

        // channelInfo를 얻어올 수 있는 api가 없음
    }
}
