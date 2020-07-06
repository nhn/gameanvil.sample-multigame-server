package unit_test;

import com.nhn.gameanvilcore.connector.common.Config;
import com.nhn.gameanvilcore.connector.protocol.Packet;
import com.nhn.gameanvilcore.connector.protocol.result.AuthenticationResult;
import com.nhn.gameanvilcore.connector.protocol.result.ChannelListResult;
import com.nhn.gameanvilcore.connector.protocol.result.LoginResult;
import com.nhn.gameanvilcore.connector.tcp.ConnectorSession;
import com.nhn.gameanvilcore.connector.tcp.ConnectorUser;
import com.nhn.gameanvilcore.connector.tcp.GameAnvilConnector;
import com.nhnent.tardis.sample.Defines.StringValues;
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

    public static String ServiceName = "ChatService";
    public static String UserType = "ChatUser";
    public static String RoomType = "ChatRoom";

    private static GameAnvilConnector connector;
    private ConnectorSession session = null;

    @BeforeClass
    public static void configuration() {

        // 테스트 하려는 서버의 IP 와 Port 를 지정합니다.
        Config.addRemoteInfo("127.0.0.1", 11200);
        Config.WAIT_RECV_TIMEOUT_MSEC = 3000;

        // 커넥터와, Base 프로토콜 사용 편의를 위해 Helper 를 생성합니다.
        connector = GameAnvilConnector.getInstance();

        // 컨텐츠 프로토콜 등록.
        connector.addProtoBufClass(0, Sample.class);

        // 컨텐츠 서비스 등록.
        connector.addService(0, ServiceName);
    }

    @Before
    public void setUp() throws TimeoutException {

        session = connector.addSession(connector.getIncrementedValue("account_"), connector.makeUniqueId());
    }

    @After
    public void tearDown() throws TimeoutException {

        session.disconnect();
    }

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
        payloadSnd.setMessage(StringValues.AuthenticatePayload);

        AuthenticationResult authResult = session.authentication(session.getAccountId(), payloadSnd);
        assertTrue(authResult.isSuccess());

        Packet packet = authResult.getPayload(Sample.SampleData.class);
        if (null != packet) {
            try {
                Sample.SampleData payloadRcv = Sample.SampleData.parseFrom(packet.getStream());
                assertEquals(StringValues.AuthenticatePayload, payloadRcv.getMessage());
            } catch (IOException e) {
                fail(e.getMessage());
            }
        } else {
            fail("Payload contains no Sample.SampleData.");
        }
    }

    @Test
    public void authenticateFail() throws IOException, TimeoutException {
        // accountId와 password가 일치하지 않으면 실패.
        AuthenticationResult authResult = session.authentication();
        assertTrue(authResult.isFailure());
        Packet packet = authResult.getPayload(Sample.SampleData.class);
        if (null != packet) {
            try {
                // 실패일 경우 payload에 실패 메시지를 보냄
                Sample.SampleData msg = Sample.SampleData.parseFrom(packet.getStream());
                assertEquals(msg.getMessage(), StringValues.AuthenticateFail);
            } catch (IOException e) {
                fail(e.getMessage());
            }
        } else {
            fail("Payload contains no Sample.SampleData.");
        }
    }

    @Test
    public void SampleReqToSession() throws IOException, TimeoutException {

        authenticateSuccess();

        String message = "SampleReq";
        try {
            //SampleReq 를 보낼 경우 보낸 값을 그대로 돌려 받음.
            Packet packetRes = session.requestToSession(new Packet(Sample.SampleReq.newBuilder().setMessage(message)), Sample.SampleRes.class);
            Sample.SampleRes msg = Sample.SampleRes.parseFrom(packetRes.getStream());
            assertEquals(msg.getMessage(), message);
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void SampleToSToSession() throws IOException, TimeoutException {

        authenticateSuccess();

        String message = "SampleToS";
        try {
            //SampleToS 를 보낼 경우 보낸 값을 그대로 돌려 받음.
            session.sendToSession(new Packet(Sample.SampleToS.newBuilder().setMessage(message)));
            Packet packetSampleToC = session.waitPacket(1, TimeUnit.SECONDS, Sample.SampleToC.class);
            Sample.SampleToC msg = Sample.SampleToC.parseFrom(packetSampleToC.getStream());
            assertEquals(msg.getMessage(), message);
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void SampleReqToSessionUser() throws IOException, TimeoutException {

        authenticateSuccess();
        ConnectorUser user = session.addUser(ServiceName);
        LoginResult result = user.login(UserType, "1");
        assertTrue(result.isSuccess());

        String message = "SampleReqToSessionUser";
        try {
            //SampleReq 를 보낼 경우 보낸 값을 그대로 돌려 받음.
            Packet packetRes = user.requestToSessionActor(new Packet(Sample.SampleReq.newBuilder().setMessage(message)), Sample.SampleRes.class);
            Sample.SampleRes msg = Sample.SampleRes.parseFrom(packetRes.getStream());
            assertEquals(msg.getMessage(), message);
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void getChannelList() throws IOException, TimeoutException {

        authenticateSuccess();

        ChannelListResult result = session.channelList(ServiceName);
        assertTrue(result.isSuccess());

        // TardisConfig에 설정된 체널 정보는 ["1","1","2","2","3","3","4","4"]
        // 응답에서는 중복제거, 정렬된 ["1", "2", "3", "4"]로 내려와야함.
        List<String> list = result.getChannelList();
        assertEquals(4, list.size());
        assertEquals("1", list.get(0));
    }

    @Test
    public void getChannelInfo() throws IOException, TimeoutException {

        authenticateSuccess();

        // channelInfo를 얻어올 수 있는 api가 없음
    }

    @Test
    public void SetRemoveTimerToSession() throws IOException, TimeoutException {

        authenticateSuccess();

        String message = "SetTimer";
        try {
            //SetTimer 를 보낼 경우 SampleSessionNode에 Timer가 동작.
            session.sendToSession(new Packet(Sample.SetTimer.newBuilder().setInterval(1).setMessage(message)));
            Packet packet = session.waitPacket(2, TimeUnit.SECONDS, Sample.SampleToC.class);
            Sample.SampleToC msg = Sample.SampleToC.parseFrom(packet.getStream());
            assertEquals(msg.getMessage(), message);
        } catch (Exception e) {
            fail(e.toString());
        }

        try {
            //RemoveTimer 를 보낼 경우 SampleSessionNode에 Timer가 해제.
            session.sendToSession(new Packet(Sample.RemoveTimer.newBuilder()));
            session.waitPacket(2, TimeUnit.SECONDS, Sample.SampleToC.class);
            fail("RemoveTimer fail");
        } catch (TimeoutException e) {
            // Timer가 해제 되었기 때문에 SampleToC가 오지 않는다.
        } catch (Exception e) {
            fail(e.toString());
        }
    }
}
