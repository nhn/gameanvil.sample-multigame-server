package unit_test;

import com.nhnent.tardis.sample.protocol.Chat;
import com.nhnent.tardis.connector.common.Config;
import com.nhnent.tardis.connector.protocol.Packet;
import com.nhnent.tardis.connector.protocol.result.AuthenticationResult;
import com.nhnent.tardis.connector.protocol.result.LoginResult;
import com.nhnent.tardis.connector.protocol.result.NamedRoomResult;
import com.nhnent.tardis.connector.tcp.ConnectorSession;
import com.nhnent.tardis.connector.tcp.ConnectorUser;
import com.nhnent.tardis.connector.tcp.TardisConnector;
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

public class HelloTardis {

    private static TardisConnector connector;
    private List<ConnectorUser> users = new ArrayList<>();

    //-------------------------------------------------------------------------------------

    @BeforeClass
    public static void configuration() {

        // 테스트 하려는 서버의 IP 와 Port 를 지정합니다.
        Config.addRemoteInfo("127.0.0.1", 11200);
        Config.WAIT_RECV_TIMEOUT_MSEC = 3000;

        // 커넥터와, Base 프로토콜 사용 편의를 위해 Helper 를 생성합니다.
        connector = TardisConnector.getInstance();

        // 컨텐츠 프로토콜 등록.
        connector.addProtoBufClass(0, Chat.class);

        // 컨텐츠 서비스 등록.
        connector.addService(0, "ChatService");
    }

    //-------------------------------------------------------------------------------------

    @Before
    public void setUp() throws TimeoutException {

        for (int i=0; i<2; ++i) {

            // 커넥션을 생성하고 세션 정보가 담긴 객체를 리턴.
            ConnectorSession session = connector.addSession(connector.getIncrementedValue("account_"), connector.makeUniqueId());

            // 인증을 진행.
            AuthenticationResult authResult = session.authentication();
            assertTrue(authResult.isSuccess());

            // 세션에 유저를 등록하고, 각종 ID 정보가 담긴 유저 객체를 리턴.
            ConnectorUser user = session.addUser("ChatService");

            // 로그인을 진행.
            LoginResult loginResult = user.login("ChatUser");
            assertTrue(loginResult.isSuccess());

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

    @Test
    public void registerNickName() throws IOException, TimeoutException {

        ConnectorUser doctor = users.get(0);

        Chat.RegisterNickNameReq.Builder registerNickNameReq = Chat.RegisterNickNameReq.newBuilder().setNickName("doctor");

        Chat.RegisterNickNameRes registerNickNameRes = doctor.requestProto(registerNickNameReq,Chat.RegisterNickNameRes.class);

        assertTrue(registerNickNameRes.getIsSuccess());
    }

    @Test
    public void helloTardis() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser doctor = users.get(0);
        ConnectorUser dalek = users.get(1);

        registerNickName();

        // 채팅방 입장
        NamedRoomResult namedRoomResult1 = doctor.namedRoom("ChatRoom","Gallifrey");
        assertTrue(namedRoomResult1.isSuccess());

        Chat.ChatMessageToC doctorJoinMsg = doctor.waitProtoPacket(1, TimeUnit.SECONDS, Chat.ChatMessageToC.class);
        assertTrue(doctorJoinMsg.getMessage().contains("join"));

        NamedRoomResult namedRoomResult2 = dalek.namedRoom("ChatRoom","Gallifrey");
        assertTrue(namedRoomResult2.isSuccess());

        Chat.ChatMessageToC dalekJoinMsg = doctor.waitProtoPacket(1, TimeUnit.SECONDS, Chat.ChatMessageToC.class);
        assertTrue(dalekJoinMsg.getMessage().contains("join"));

        Chat.ChatMessageToC doctorGetDalekJoinMsg = dalek.waitProtoPacket(1, TimeUnit.SECONDS, Chat.ChatMessageToC.class);
        assertTrue(doctorGetDalekJoinMsg.getMessage().contains("join"));

        // 채팅 메시지 전송.
        doctor.send(new Packet(Chat.ChatMessageToS.newBuilder().setMessage("Hello Tardis!")));

        // 다른 유저에게도 응답이 왔는지 확인.
        Chat.ChatMessageToC chatMessageToC = dalek.waitProtoPacket(1, TimeUnit.SECONDS, Chat.ChatMessageToC.class);
        assertEquals("doctor : Hello Tardis!",chatMessageToC.getMessage());
    }

}
