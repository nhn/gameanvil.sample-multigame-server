package unit_test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.nhn.gameanvil.gamehammer.tester.Connection;
import com.nhn.gameanvil.gamehammer.tester.Packet;
import com.nhn.gameanvil.gamehammer.tester.PacketResult;
import com.nhn.gameanvil.gamehammer.tester.RemoteInfo;
import com.nhn.gameanvil.gamehammer.tester.ResultAuthentication;
import com.nhn.gameanvil.gamehammer.tester.ResultAuthentication.ResultCodeAuthentication;
import com.nhn.gameanvil.gamehammer.tester.ResultChannelList;
import com.nhn.gameanvil.gamehammer.tester.ResultLogin;
import com.nhn.gameanvil.gamehammer.tester.Tester;
import com.nhn.gameanvil.gamehammer.tester.User;
import com.nhn.gameanvil.sample.Defines.StringValues;
import com.nhn.gameanvil.sample.protocol.Sample;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SessionAgentTest {

    static final String[] ChannelId = {"1", "2", "3", "4"};
    static final String AccountId = "AccountId";
    static final String Password = "AccountId";
    static final String DeviceId = "DeviceId";

    static final int Timeout = 3000;

    private static Tester tester;
    private static Connection connection;

    @BeforeClass
    public static void configuration() {

        tester = Tester.newBuilder()
            .addRemoteInfo("127.0.0.1", 11200) // 테스트 하려는 서버의 IP 와 Port 를 지정합니다.
            .setDefaultPacketTimeoutSeconds(3)
            .addProtoBufClass(0, Sample.getDescriptor())// 컨텐츠 프로토콜 등록.
            .addServiceInfo(1, "ChatService", ChannelId)// 컨텐츠 서비스 등록.
            .addServiceInfo(2, StringValues.GameServiceName, ChannelId)// 컨텐츠 서비스 등록.
            .Build();
    }

    @Before
    public void setUp() throws TimeoutException, ExecutionException, InterruptedException {
        connection = tester.createConnection(1);
        connection.connect(new RemoteInfo("127.0.0.1", 11200)).get(Timeout, TimeUnit.MILLISECONDS);
    }

    @After
    public void tearDown() throws TimeoutException, ExecutionException, InterruptedException {

        connection.close().get(Timeout, TimeUnit.MILLISECONDS);
    }

    @Test
    public void authenticateSuccess() throws TimeoutException, ExecutionException, InterruptedException {
        // accountId와 password가 일치할 때 성공
        ResultAuthentication resultAuthentication = connection.authentication(AccountId, Password, DeviceId).get(Timeout, TimeUnit.MILLISECONDS);
        assertTrue(resultAuthentication.isSuccess());
        assertNull(resultAuthentication.getPacketFromPayload(Sample.SampleData.getDescriptor()));
    }

    @Test
    public void authenticateSuccessWithPayload() throws TimeoutException, IOException, ExecutionException, InterruptedException {
        // payload를 보낼 경우, 보낸 값을 그대로 되돌려 받아야함.
        Sample.SampleData.Builder payloadSnd = Sample.SampleData.newBuilder();
        payloadSnd.setMessage(StringValues.AuthenticatePayload);

        ResultAuthentication resultAuthentication = connection.authentication(AccountId, Password, DeviceId, payloadSnd).get(Timeout, TimeUnit.MILLISECONDS);
        assertTrue(resultAuthentication.isSuccess());

        Packet packet = resultAuthentication.getPacketFromPayload(Sample.SampleData.getDescriptor());
        assertNotNull(packet);
        Sample.SampleData payloadRcv = Sample.SampleData.parseFrom(packet.getStream());
        assertEquals(StringValues.AuthenticatePayload, payloadRcv.getMessage());
    }

    @Test
    public void authenticateFail() throws IOException, TimeoutException, ExecutionException, InterruptedException {
        // accountId와 password가 일치하지 않으면 실패.
        ResultAuthentication resultAuthentication = connection.authentication(AccountId, Password + "fail", DeviceId).get(Timeout, TimeUnit.MILLISECONDS);
        assertFalse(resultAuthentication.isSuccess());
        assertEquals(ResultCodeAuthentication.AUTH_FAIL_CONTENT, resultAuthentication.getResultCode());
        Packet packet = resultAuthentication.getPacketFromPayload(Sample.SampleData.getDescriptor());
        assertNotNull(packet);
        Sample.SampleData msg = Sample.SampleData.parseFrom(packet.getStream());
        assertEquals(msg.getMessage(), StringValues.AuthenticateFail);
    }

    @Test
    public void SampleReqToConnection() throws IOException, TimeoutException, ExecutionException, InterruptedException {

        authenticateSuccess();

        String message = "SampleReq";
        //SampleReq 를 보낼 경우 보낸 값을 그대로 돌려 받음.
        PacketResult packetResult = connection.request(Sample.SampleReq.newBuilder().setMessage(message).build()).get(Timeout, TimeUnit.MILLISECONDS);
        Sample.SampleRes msg = Sample.SampleRes.parseFrom(packetResult.getStream());
        assertEquals(msg.getMessage(), message);
    }

    @Test
    public void SampleToSToConnection() throws IOException, TimeoutException, ExecutionException, InterruptedException {

        authenticateSuccess();

        String message = "SampleToS";
        //SampleToS 를 보낼 경우 보낸 값을 그대로 돌려 받음.
        Future<PacketResult> future= connection.waitFor(Sample.SampleToC.getDescriptor());
        System.out.println("test1");
        connection.send(Sample.SampleToS.newBuilder().setMessage(message).build());
        System.out.println("test2");
        PacketResult packetResult = future.get(Timeout, TimeUnit.MILLISECONDS);
        System.out.println("test3");
        Sample.SampleToC msg = Sample.SampleToC.parseFrom(packetResult.getStream());
        assertEquals(msg.getMessage(), message);
    }

    @Test
    public void SampleReqToSession() throws IOException, TimeoutException, ExecutionException, InterruptedException {

        authenticateSuccess();
        User user = connection.createUser(StringValues.GameServiceName, 1);
        ResultLogin resultLoginFuture = user.login(StringValues.GameUserType, "1").get(Timeout, TimeUnit.MILLISECONDS);
        assertTrue(resultLoginFuture.isSuccess());

        String message = "SampleReqToSessionUser";
        //SampleReq 를 보낼 경우 보낸 값을 그대로 돌려 받음.
        PacketResult packetResult = user.requestToSession(Sample.SampleReq.newBuilder().setMessage(message).build()).get(Timeout, TimeUnit.MILLISECONDS);
        Sample.SampleRes msg = Sample.SampleRes.parseFrom(packetResult.getStream());
        assertEquals(msg.getMessage(), message);

//        user.waitFor(Sample.SampleToC.getDescriptor()).get(Timeout, TimeUnit.MILLISECONDS);

        user.logout().get(Timeout, TimeUnit.MILLISECONDS);
    }

    @Test
    public void getChannelList() throws IOException, TimeoutException, ExecutionException, InterruptedException {

        authenticateSuccess();

        ResultChannelList resultChannelList = connection.getChannelList(StringValues.GameServiceName).get(Timeout, TimeUnit.MILLISECONDS);
        assertTrue(resultChannelList.isSuccess());

        // GameAnvilConfig에 설정된 체널 정보는 ["1","1","2","2","3","3","4","4"]
        // 응답에서는 중복제거, 정렬된 ["1", "2", "3", "4"]로 내려와야함.
        List<String> list = resultChannelList.getChannelList();
        assertEquals(4, list.size());
        assertEquals("1", list.get(0));
    }

    @Test
    public void getChannelInfo() throws IOException, TimeoutException, ExecutionException, InterruptedException {

        authenticateSuccess();

        // channelInfo를 얻어올 수 있는 api가 없음
    }

    @Test
    public void SetRemoveTimerToSession() throws IOException, TimeoutException, ExecutionException, InterruptedException {

        authenticateSuccess();

        String message = "SetTimer";
        Future<PacketResult> future = connection.waitFor(Sample.SampleToC.getDescriptor());
        connection.send(Sample.SetTimer.newBuilder().setInterval(1).setMessage(message).build());
        PacketResult packetResult = future.get(Timeout, TimeUnit.MILLISECONDS);
        Sample.SampleToC msg = Sample.SampleToC.parseFrom(packetResult.getStream());
        assertEquals(msg.getMessage(), message);

        try {
            //RemoveTimer 를 보낼 경우 SampleSessionNode에 Timer가 해제.
            future = connection.waitFor(Sample.SampleToC.getDescriptor());
            connection.send(Sample.RemoveTimer.newBuilder().build());
            future.get(Timeout, TimeUnit.MILLISECONDS);
            Assert.fail("RemoveTimer fail");
        } catch (TimeoutException e) {
            // Timer가 해제 되었기 때문에 SampleToC가 오지 않는다.
            System.out.println("Timeout!!");
        }
    }
}
