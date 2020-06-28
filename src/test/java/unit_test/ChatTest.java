package unit_test;

import com.nhn.gameflexcore.connector.common.Config;
import com.nhn.gameflexcore.connector.protocol.Packet;
import com.nhn.gameflexcore.connector.protocol.result.AuthenticationResult;
import com.nhn.gameflexcore.connector.protocol.result.LoginResult;
import com.nhn.gameflexcore.connector.protocol.result.NamedRoomResult;
import com.nhn.gameflexcore.connector.tcp.ConnectorSession;
import com.nhn.gameflexcore.connector.tcp.ConnectorUser;
import com.nhn.gameflexcore.connector.tcp.TardisConnector;
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
import static org.junit.Assert.assertTrue;

public class ChatTest {

    public static String ServiceName = "ChatService";
    public static String UserType = "ChatUser";
    public static String RoomType = "ChatRoom";

    private static TardisConnector connector;
    private List<ConnectorUser> users = new ArrayList<>();

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

    @Before
    public void setUp() throws TimeoutException {

        for (int i = 0; i < 4; ++i) {

            // 커넥션을 생성하고 세션 정보가 담긴 객체를 리턴.
            ConnectorSession session = connector.addSession(connector.getIncrementedValue("account_"), connector.makeUniqueId());

            // 인증을 진행.
            AuthenticationResult authResult = session.authentication(session.getAccountId());
            assertTrue("Authentication fail", authResult.isSuccess());

            // 세션에 유저를 등록하고, 각종 ID 정보가 담긴 유저 객체를 리턴.
            ConnectorUser user = session.addUser(ServiceName);

            // 로그인을 진행.
            LoginResult loginResult = user.login(UserType, String.valueOf(i + 1));
            assertTrue("Login fail", loginResult.isSuccess());

            // Test 단계에서 활용하도록 준비합니다.
            users.add(user);
        }
    }

    @After
    public void tearDown() throws TimeoutException {

        for (ConnectorUser user : users) {
            user.logout();
            user.getSession().disconnect();
        }
    }

    void registerNickName(ConnectorUser user, String nickName) throws IOException, TimeoutException {

        Sample.RegisterNickNameReq.Builder registerNickNameReq = Sample.RegisterNickNameReq.newBuilder().setNickName(nickName);

        Sample.RegisterNickNameRes registerNickNameRes = user.requestProto(registerNickNameReq, Sample.RegisterNickNameRes.class);

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
        NamedRoomResult namedRoomResult1 = doctor.namedRoom(RoomType, "Gallifrey");
        assertTrue(namedRoomResult1.isSuccess());
        assertTrue(namedRoomResult1.isMatchRoomCreated());

        NamedRoomResult namedRoomResult2 = dalek.namedRoom(RoomType, "Gallifrey");
        assertTrue(namedRoomResult2.isSuccess());
        assertTrue(namedRoomResult2.isMatchRoomJoined());

        Sample.ChatMessageToC doctorGetDalekJoinMsg = doctor.waitProtoPacket(1, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("dalek is join", doctorGetDalekJoinMsg.getMessage());

        // 채팅 메시지 전송.
        doctor.send(new Packet(Sample.ChatMessageToS.newBuilder().setMessage("Hello Tardis!")));

        // 다른 유저에게도 응답이 왔는지 확인.
        Sample.ChatMessageToC chatMessageToC = dalek.waitProtoPacket(1, TimeUnit.SECONDS, Sample.ChatMessageToC.class);
        assertEquals("[doctor] Hello Tardis!", chatMessageToC.getMessage());
    }
}
