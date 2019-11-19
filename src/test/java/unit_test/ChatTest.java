package unit_test;

import com.nhnent.tardis.common.protocol.Base;
import com.nhnent.tardis.common.protocol.Base.ResultCodeMatchUserDone;
import com.nhnent.tardis.connector.common.Config;
import com.nhnent.tardis.connector.protocol.Packet;
import com.nhnent.tardis.connector.protocol.result.AuthenticationResult;
import com.nhnent.tardis.connector.protocol.result.CreateRoomResult;
import com.nhnent.tardis.connector.protocol.result.LeaveRoomResult;
import com.nhnent.tardis.connector.protocol.result.LoginResult;
import com.nhnent.tardis.connector.protocol.result.MatchRoomResult;
import com.nhnent.tardis.connector.protocol.result.MatchUserCancelResult;
import com.nhnent.tardis.connector.protocol.result.MatchUserStartResult;
import com.nhnent.tardis.connector.protocol.result.NamedRoomResult;
import com.nhnent.tardis.connector.protocol.result.ReconnectResult;
import com.nhnent.tardis.connector.tcp.ConnectorSession;
import com.nhnent.tardis.connector.tcp.ConnectorUser;
import com.nhnent.tardis.connector.tcp.TardisConnector;
import com.nhnent.tardis.sample.protocol.Sample;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ChatTest {

    public static String ServiceName = "ChatService";
    public static String UserType = "ChatUser";
    public static String RoomType = "ChatRoom";

    private static TardisConnector connector;
    private List<ConnectorUser> users = new ArrayList<>();

    //-------------------------------------------------------------------------------------

    @BeforeClass
    public static void configuration() {

        // 테스트 하려는 서버의 IP 와 Port 를 지정합니다.
        Config.addRemoteInfo("127.0.0.1", 11200);
        Config.WAIT_RECV_TIMEOUT_MSEC = 5000;

        // 커넥터와, Base 프로토콜 사용 편의를 위해 Helper 를 생성합니다.
        connector = TardisConnector.getInstance();

        // 컨텐츠 프로토콜 등록.
        connector.addProtoBufClass(0, Sample.class);

        // 컨텐츠 서비스 등록.
        connector.addService(0, ServiceName);
    }

    //-------------------------------------------------------------------------------------

    @Before
    public void setUp() throws TimeoutException {

        for (int i=0; i<4; ++i) {

            // 커넥션을 생성하고 세션 정보가 담긴 객체를 리턴.
            ConnectorSession session = connector.addSession(connector.getIncrementedValue("account_"), connector.makeUniqueId());

            // 인증을 진행.
            AuthenticationResult authResult = session.authentication(session.getAccountId());
            assertTrue("Authentication fail", authResult.isSuccess());

            // 세션에 유저를 등록하고, 각종 ID 정보가 담긴 유저 객체를 리턴.
            ConnectorUser user = session.addUser(ServiceName);

            // 로그인을 진행.
            LoginResult loginResult = user.login(UserType, String.valueOf(i+1));
            assertTrue("Login fail", loginResult.isSuccess());

            // Test 단계에서 활용하도록 준비합니다.
            users.add(user);
        }
    }

    @After
    public void tearDown() throws TimeoutException {

        for(ConnectorUser user : users){
            user.logout();
            user.getSession().disconnect();
        }
    }

    //-------------------------------------------------------------------------------------

    void registerNickName(ConnectorUser user, String nickName) throws IOException, TimeoutException {

        Sample.RegisterNickNameReq.Builder registerNickNameReq = Sample.RegisterNickNameReq.newBuilder().setNickName(nickName);

        Sample.RegisterNickNameRes registerNickNameRes = user.requestProto(registerNickNameReq,Sample.RegisterNickNameRes.class);

        assertTrue(registerNickNameRes.getIsSuccess());
    }

    @Test
    public void registerNickName() throws IOException, TimeoutException {

        ConnectorUser doctor = users.get(0);
        registerNickName(doctor, "doctor");
    }

    @Test
    public void NamedRoomAndChat() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser doctor = users.get(0);
        ConnectorUser dalek = users.get(1);

        registerNickName(doctor, "doctor");
        registerNickName(dalek, "dalek");

        // 채팅방 입장
        NamedRoomResult namedRoomResult1 = doctor.namedRoom(RoomType,"Gallifrey");
        assertTrue(namedRoomResult1.isSuccess());
        assertTrue(namedRoomResult1.isMatchRoomCreated());

        NamedRoomResult namedRoomResult2 = dalek.namedRoom(RoomType,"Gallifrey");
        assertTrue(namedRoomResult2.isSuccess());
        assertTrue(namedRoomResult2.isMatchRoomJoined());

        Sample.ChatMessageToC doctorGetDalekJoinMsg = doctor.waitProtoPacket(1, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("dalek is join", doctorGetDalekJoinMsg.getMessage());

        // 채팅 메시지 전송.
        doctor.send(new Packet(Sample.ChatMessageToS.newBuilder().setMessage("Hello Tardis!")));

        // 다른 유저에게도 응답이 왔는지 확인.
        Sample.ChatMessageToC chatMessageToC = dalek.waitProtoPacket(1, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("[doctor] Hello Tardis!",chatMessageToC.getMessage());
    }

    @Test
    public void MatchRoomAndChat() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser doctor = users.get(0);
        ConnectorUser dalek = users.get(1);

        registerNickName(doctor, "doctor");
        registerNickName(dalek, "dalek");

        // 채팅방 입장
        MatchRoomResult matchRoomResult1 = doctor.matchRoom(RoomType);
        assertTrue(matchRoomResult1.isSuccess());
        assertTrue(matchRoomResult1.isMatchRoomCreated());

        MatchRoomResult matchRoomResult2 = dalek.matchRoom(RoomType);
        assertTrue(matchRoomResult2.isSuccess());
        assertTrue(matchRoomResult2.isMatchRoomJoined());

        Sample.ChatMessageToC doctorGetDalekJoinMsg = doctor.waitProtoPacket(5, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("dalek is join", doctorGetDalekJoinMsg.getMessage());

        // 채팅 메시지 전송.
        doctor.send(new Packet(Sample.ChatMessageToS.newBuilder().setMessage("Hello Tardis!")));

        // 다른 유저에게도 응답이 왔는지 확인.
        Sample.ChatMessageToC chatMessageToC = dalek.waitProtoPacket(1, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("[doctor] Hello Tardis!",chatMessageToC.getMessage());
    }

    @Test
    public void MatchRoomMoveAndChat() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser doctor = users.get(0);
        ConnectorUser dalek = users.get(1);
        ConnectorUser bobby = users.get(2);

        registerNickName(doctor, "doctor");
        registerNickName(dalek, "dalek");
        registerNickName(bobby, "bobby");

        MatchRoomResult matchRoomResult1 = doctor.matchRoom(RoomType);
        assertTrue(matchRoomResult1.isSuccess());
        assertTrue(matchRoomResult1.isMatchRoomCreated());
        String roomId = matchRoomResult1.getRoomId();

        MatchRoomResult matchRoomResult2 = dalek.matchRoom(RoomType);
        assertTrue(matchRoomResult2.isSuccess());
        assertTrue(matchRoomResult2.isMatchRoomJoined());
        assertEquals(roomId, matchRoomResult2.getRoomId());

        Sample.ChatMessageToC doctorGetDalekJoinMsg = doctor.waitProtoPacketByFirstReceived(5, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("dalek is join", doctorGetDalekJoinMsg.getMessage());

        // 채팅방 입장
        MatchRoomResult matchRoomResult3 = doctor.matchRoom(true, RoomType, true);
        assertTrue(matchRoomResult3.isSuccess());
        assertTrue(matchRoomResult3.isMatchRoomCreated());
        assertNotEquals(roomId, matchRoomResult3.getRoomId());

        LeaveRoomResult leaveRoomResult = dalek.leaveRoom();
        assertTrue(leaveRoomResult.isSuccess());

        MatchRoomResult matchRoomResult4 = bobby.matchRoom(RoomType);
        assertTrue(matchRoomResult4.isSuccess());
        assertTrue(matchRoomResult4.isMatchRoomJoined());
        assertEquals(matchRoomResult3.getRoomId(), matchRoomResult4.getRoomId());

        Sample.ChatMessageToC doctorGetBobbyJoinMsg = doctor.waitProtoPacketByFirstReceived(5, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("bobby is join", doctorGetBobbyJoinMsg.getMessage());

        // 채팅 메시지 전송.
        doctor.send(new Packet(Sample.ChatMessageToS.newBuilder().setMessage("Hello Tardis!")));

        // 다른 유저에게도 응답이 왔는지 확인.
        Sample.ChatMessageToC chatMessageToC = bobby.waitProtoPacket(1, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("[doctor] Hello Tardis!",chatMessageToC.getMessage());
    }

    @Test
    public void MatchRoomAndDisconnect() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser doctor = users.get(0);
        ConnectorUser dalek = users.get(1);

        registerNickName(doctor, "doctor");
        registerNickName(dalek, "dalek");

        // 채팅방 입장
        MatchRoomResult matchRoomResult1 = doctor.matchRoom(RoomType);
        assertTrue(matchRoomResult1.isSuccess());
        assertTrue(matchRoomResult1.isMatchRoomCreated());

        MatchRoomResult matchRoomResult2 = dalek.matchRoom(RoomType);
        assertTrue(matchRoomResult2.isSuccess());
        assertTrue(matchRoomResult2.isMatchRoomJoined());

        Sample.ChatMessageToC doctorGetDalekJoinMsg = doctor.waitProtoPacket(1, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("dalek is join", doctorGetDalekJoinMsg.getMessage());

        String accountId = doctor.getSession().getAccountId();
        String deviceId = doctor.getSession().getDeviceId();
        doctor.getSession().disconnect();

        // TearDown에서 제외하기 위해 users에서 제외
        users.remove(doctor);
    }

    @Test
    public void MatchUserAndChat() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser doctor = users.get(0);
        ConnectorUser dalek = users.get(1);

        registerNickName(doctor, "doctor");
        registerNickName(dalek, "dalek");

        // 채팅방 입장
        MatchUserStartResult matchUserStartResult1 = doctor.matchUserStart(RoomType);
        assertTrue(matchUserStartResult1.isSuccess());

        MatchUserStartResult matchUserStartResult2 = dalek.matchUserStart(RoomType);
        assertTrue(matchUserStartResult2.isSuccess());

        Base.MatchUserDone matchUserDone1 = doctor.waitProtoPacket(5, TimeUnit.SECONDS, Base.MatchUserDone.class);
        Base.MatchUserDone matchUserDone2 = dalek.waitProtoPacket(5, TimeUnit.SECONDS, Base.MatchUserDone.class);

        Sample.ChatMessageToC doctorGetDalekJoinMsg = doctor.waitProtoPacket(5, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("dalek is join", doctorGetDalekJoinMsg.getMessage());

        Sample.ChatMessageToC dalekGetDoctorJoinMsg = dalek.waitProtoPacket(5, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("doctor is join", dalekGetDoctorJoinMsg.getMessage());

        // 채팅 메시지 전송.
        doctor.send(new Packet(Sample.ChatMessageToS.newBuilder().setMessage("Hello Tardis!")));

        // 다른 유저에게도 응답이 왔는지 확인.
        Sample.ChatMessageToC chatMessageToC = dalek.waitProtoPacket(1, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("[doctor] Hello Tardis!",chatMessageToC.getMessage());
    }

    @Test
    public void MatchUserAndRefillAndChat() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser doctor = users.get(0);
        ConnectorUser dalek = users.get(1);
        ConnectorUser bobby = users.get(2);

        registerNickName(doctor, "doctor");
        registerNickName(dalek, "dalek");
        registerNickName(bobby, "bobby");

        // 채팅방 입장
        MatchUserStartResult matchUserStartResult1 = doctor.matchUserStart(RoomType);
        assertTrue(matchUserStartResult1.isSuccess());

        MatchUserStartResult matchUserStartResult2 = dalek.matchUserStart(RoomType);
        assertTrue(matchUserStartResult2.isSuccess());

        Base.MatchUserDone matchUserDone1 = doctor.waitProtoPacket(10, TimeUnit.SECONDS, Base.MatchUserDone.class);
        Base.MatchUserDone matchUserDone2 = dalek.waitProtoPacket(1, TimeUnit.SECONDS, Base.MatchUserDone.class);

        Sample.ChatMessageToC doctorGetDalekJoinMsg = doctor.waitProtoPacket(5, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("dalek is join", doctorGetDalekJoinMsg.getMessage());

        Sample.ChatMessageToC dalekJoinMsg = dalek.waitProtoPacket(1, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("doctor is join", dalekJoinMsg.getMessage());

        LeaveRoomResult leaveRoomResult = dalek.leaveRoom();
        assertTrue(leaveRoomResult.isSuccess());

        Thread.sleep(500);

        MatchUserStartResult matchUserStartResult3 = bobby.matchUserStart(RoomType);
        assertTrue(matchUserStartResult3.isSuccess());

        Base.MatchUserDone matchUserDone3 = bobby.waitProtoPacket(5, TimeUnit.SECONDS, Base.MatchUserDone.class);
        assertTrue(matchUserDone3.getResultCode() == ResultCodeMatchUserDone.MATCH_USER_DONE_SUCCESS);

        doctorGetDalekJoinMsg = doctor.waitProtoPacket(5, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("bobby is join", doctorGetDalekJoinMsg.getMessage());

        // 채팅 메시지 전송.
        doctor.send(new Packet(Sample.ChatMessageToS.newBuilder().setMessage("Hello Tardis!")));

        // 다른 유저에게도 응답이 왔는지 확인.
        Sample.ChatMessageToC chatMessageToC = bobby.waitProtoPacket(1, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("[doctor] Hello Tardis!",chatMessageToC.getMessage());
    }

    @Test
    public void MatchUserAndCancel() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser doctor = users.get(0);
        ConnectorUser dalek = users.get(1);

        registerNickName(doctor, "doctor");
        registerNickName(dalek, "dalek");

        // 채팅방 입장
        MatchUserStartResult matchUserStartResult1 = doctor.matchUserStart(RoomType);
        assertTrue(matchUserStartResult1.isSuccess());

        MatchUserStartResult matchUserStartResult2 = dalek.matchUserStart(RoomType);
        assertTrue(matchUserStartResult2.isSuccess());

        MatchUserCancelResult matchUserCancelResult = dalek.matchUserCancel(RoomType);
        assertTrue(matchUserCancelResult.isSuccess());

        Base.MatchUserTimeout matchUserTimeout = doctor.waitProtoPacket(6, TimeUnit.SECONDS, Base.MatchUserTimeout.class);
    }

    @Test
    public void MessageFromServiceNodeAgent() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser doctor = users.get(0);
        ConnectorUser dalek = users.get(1);
        ConnectorUser bobby = users.get(2);
        ConnectorUser jhone = users.get(3);

        Sample.SampleToC sampleToC4 = jhone.waitProtoPacket(5, TimeUnit.SECONDS, Sample.SampleToC.class);
        Sample.SampleToC sampleToC3 = bobby.waitProtoPacket(5, TimeUnit.SECONDS, Sample.SampleToC.class);
        Sample.SampleToC sampleToC2 = dalek.waitProtoPacket(5, TimeUnit.SECONDS, Sample.SampleToC.class);
        Sample.SampleToC sampleToC1 = doctor.waitProtoPacket(5, TimeUnit.SECONDS, Sample.SampleToC.class);

        assertNotEquals(doctor.getChannelId(), dalek.getChannelId());
        assertNotEquals(doctor.getChannelId(), bobby.getChannelId());
        assertNotEquals(doctor.getChannelId(), jhone.getChannelId());


        assertEquals(sampleToC1.getMessage(), sampleToC2.getMessage());
        assertEquals(sampleToC1.getMessage(), sampleToC3.getMessage());
        assertEquals(sampleToC1.getMessage(), sampleToC4.getMessage());
    }

}
