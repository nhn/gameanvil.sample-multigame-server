package unit_test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nhn.gameanvil.gamehammer.tester.Connection;
import com.nhn.gameanvil.gamehammer.tester.PacketResult;
import com.nhn.gameanvil.gamehammer.tester.RemoteInfo;
import com.nhn.gameanvil.gamehammer.tester.ResultAuthentication;
import com.nhn.gameanvil.gamehammer.tester.ResultConnect;
import com.nhn.gameanvil.gamehammer.tester.ResultLogin;
import com.nhn.gameanvil.gamehammer.tester.ResultNamedRoom;
import com.nhn.gameanvil.gamehammer.tester.Tester;
import com.nhn.gameanvil.gamehammer.tester.User;
import com.nhn.gameanvil.gamehammer.tool.UuidGenerator;
import com.nhn.gameanvil.sample.Defines.StringValues;
import com.nhn.gameanvil.sample.protocol.Sample;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ChatTest {

    static final String[] ChannelId = {"1", "2", "3", "4"};

    static final int Timeout = 3000;

    private static Tester tester;
    private List<User> users = new ArrayList<>();

    @BeforeClass
    public static void configuration() {

        tester = Tester.newBuilder()
            .addRemoteInfo("127.0.0.1", 11200) // 테스트 하려는 서버의 IP 와 Port 를 지정합니다.
            .setDefaultPacketTimeoutSeconds(3)
            .addProtoBufClass(0, Sample.getDescriptor())// 컨텐츠 프로토콜 등록.
            .addServiceInfo(1, StringValues.ChatServiceName, ChannelId)// 컨텐츠 서비스 등록.
            .Build();
    }

    @Before
    public void setUp() throws TimeoutException, ExecutionException, InterruptedException {
        UuidGenerator uuidActor = new UuidGenerator("AccountId");
        UuidGenerator uuidDevice = new UuidGenerator("DeviceId");
        for (int i = 0; i < 4; ++i) {

            // 커넥션을 생성하고 세션 정보가 담긴 객체를 리턴.
            Connection connection = tester.createConnection(i);
            ResultConnect resultConnect = connection.connect(new RemoteInfo("127.0.0.1", 11200)).get(3000, TimeUnit.MILLISECONDS);
            assertTrue(resultConnect.isSuccess());

            // 인증을 진행.
            String accountId = uuidActor.generateUniqueId();
            String deviceId = uuidDevice.generateUniqueId();
            ResultAuthentication result = connection.authentication(accountId, accountId, deviceId).get(Timeout, TimeUnit.MILLISECONDS);
            assertTrue(result.isSuccess());

            // 세션에 유저를 등록하고, 각종 ID 정보가 담긴 유저 객체를 리턴.
            User user = connection.createUser(StringValues.ChatServiceName, 1);

            // 로그인을 진행.
            ResultLogin resultLogin = user.login(StringValues.ChatUserType, ChannelId[i]).get(Timeout, TimeUnit.MILLISECONDS);
            assertTrue(resultLogin.isSuccess());

            // Test 단계에서 활용하도록 준비합니다.
            users.add(user);
        }
    }

    @After
    public void tearDown() throws TimeoutException, ExecutionException, InterruptedException {
        for (User user : users) {
            user.logout().get(Timeout, TimeUnit.MILLISECONDS);
            user.getConnection().close().get(Timeout, TimeUnit.MILLISECONDS);
        }
    }

    void registerNickName(User user, String nickName) throws IOException, TimeoutException, ExecutionException, InterruptedException {

        Sample.RegisterNickNameReq.Builder registerNickNameReq = Sample.RegisterNickNameReq.newBuilder().setNickName(nickName);

        PacketResult packetResult = user.request(registerNickNameReq.build()).get(Timeout, TimeUnit.MILLISECONDS);

        assertTrue(packetResult.isSuccess());
        Sample.RegisterNickNameRes registerNickNameRes = Sample.RegisterNickNameRes.parseFrom(packetResult.getStream());
        assertTrue(registerNickNameRes.getIsSuccess());
    }

    @Test
    public void registerNickName() throws IOException, TimeoutException, ExecutionException, InterruptedException {

        User doctor = users.get(0);
        registerNickName(doctor, "doctor");
    }

    @Test
    public void NamedRoomAndChat() throws TimeoutException, IOException, InterruptedException, ExecutionException {

        User doctor = users.get(0);
        User dalek = users.get(1);

        registerNickName(doctor, "doctor");
        registerNickName(dalek, "dalek");

        // 채팅방 입장
        //Future<PacketResult> future1 = doctor.waitFor(Sample.ChatMessageToC.getDescriptor());
        ResultNamedRoom resultNamedRoom = doctor.namedRoom(StringValues.ChatRoomType, StringValues.ChatRoomName, false).get(Timeout, TimeUnit.MILLISECONDS);
        assertTrue(resultNamedRoom.isSuccess());

        //PacketResult packetResult = future1.get(3, TimeUnit.SECONDS);
        //assertTrue(packetResult.isSuccess());
        //Sample.ChatMessageToC user1JoinMsg = Sample.ChatMessageToC.parseFrom(packetResult.getStream());
        //assertTrue(user1JoinMsg.getMessage().contains("join"));

        Future<PacketResult> future2 = doctor.waitFor(Sample.ChatMessageToC.getDescriptor());
//        Future<PacketResult> future3 = dalek.waitFor(Sample.ChatMessageToC.getDescriptor());
        ResultNamedRoom resultNamedRoom2 = dalek.namedRoom(StringValues.ChatRoomType, StringValues.ChatRoomName, false).get(Timeout, TimeUnit.MILLISECONDS);
        assertTrue(resultNamedRoom2.isSuccess());

        PacketResult packetResult = future2.get(3, TimeUnit.SECONDS);
        assertTrue(packetResult.isSuccess());
        Sample.ChatMessageToC user2JoinMsg = Sample.ChatMessageToC.parseFrom(packetResult.getStream());
        assertTrue(user2JoinMsg.getMessage().contains("join"));

//        packetResult = future3.get(3, TimeUnit.SECONDS);
//        assertTrue(packetResult.isSuccess());
//        user2JoinMsg = Sample.ChatMessageToC.parseFrom(packetResult.getStream());
//        assertTrue(user2JoinMsg.getMessage().contains("join"));

        // 채팅 메시지 전송.
        doctor.send(Sample.ChatMessageToS.newBuilder().setMessage("Hello GameAnvil!").build());

        // 다른 유저에게도 응답이 왔는지 확인.
        packetResult = dalek.waitFor(Sample.ChatMessageToC.getDescriptor()).get(1, TimeUnit.SECONDS);
        assertTrue(packetResult.isSuccess());
        Sample.ChatMessageToC chatMessageToC = Sample.ChatMessageToC.parseFrom(packetResult.getStream());
        assertEquals("[doctor] Hello GameAnvil!", chatMessageToC.getMessage());
    }
}
