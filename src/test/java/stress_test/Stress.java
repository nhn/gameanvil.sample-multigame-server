package stress_test;

import com.nhn.gameanvilcore.connector.common.Config;
import com.nhn.gameanvilcore.connector.tcp.ConnectorSession;
import com.nhn.gameanvilcore.connector.tcp.GameAnvilConnector;
import com.nhn.gameanvilcore.connector.tcp.agent.parent.IAsyncConnectorUser;
import com.nhnent.tardis.sample.protocol.Sample;
import org.junit.BeforeClass;
import org.junit.Test;
import stress_test.Cmd.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Stress {

    public static String ServiceName = "ChatService";
    public static String UserType = "ChatUser";
    public static String RoomType = "ChatRoom";

    private static GameAnvilConnector connector;

    @BeforeClass
    public static void configuration() {

        // 테스트 하려는 서버의 IP 와 Port 를 지정합니다.
        Config.addRemoteInfo("127.0.0.1", 11200);

        Config.SHOW_UI = true;

        // 패킷 수신에 대한 타임아웃 시간을 지정합니다. (밀리초)
        Config.WAIT_RECV_TIMEOUT_MSEC = 10000; // [default 3000]

        // 커넥터의 run 매서드에 대한 강제종료 시간을 설정합니다. (초)
        Config.FORCE_EXIT_TIMEOUT_SEC = 60; // [default 300]

        // Ping 주기를 설정합니다. (밀리초)
        Config.PING_INTERVAL_MSEC = 3000; // [default 3000]

        Config.CONCURRENT_USER = 1000;

        // 부하 테스트 시작시, Bot 유저들에 딜레이를 두고 런칭 시킬 수 있습니다.
        //Config.RAMP_UP_DELAY_MSEC = 5; // [default 0]

        // 커넥터를 생성합니다.
        connector = GameAnvilConnector.getInstance();

        // 컨텐츠 프로토콜 등록.
        connector.addProtoBufClass(0, Sample.class);

        // 컨텐츠 서비스 등록.
        connector.addService(0, ServiceName);

        // 콜백 목록을 등록합니다.
        connector.addPacketCallbackAuthentication(new CallbackAuthenticationRes());
        connector.addPacketCallbackLogin(new CallbackLoginRes());
        connector.addPacketCallback(Sample.RegisterNickNameRes.class, new CallbackRegisterNickName(), 10, TimeUnit.MILLISECONDS); // 해당 콜백을 딜레이 시켜서 호출하고자 할 경우 파라미터로 옵션값을 지정할 수 있습니다.
        connector.addPacketCallbackNamedRoom(new CallbackNamedRoomRes());

        connector.addPacketCallback(Sample.ChatMessageToC.class, new CallbackChatMessageToC());
        connector.addPacketCallbackLeaveRoom(new CallbackLeaveRoomRes(), 10); // 해당 콜백을 딜레이 시켜서 호출하고자 할 경우 파라미터로 옵션값을 지정할 수 있습니다.
        connector.addPacketCallbackLogout(new CallbackLogout());

        connector.addPacketTimeoutCallback(new SampleTimeout());
    }

    @Test
    public void runMultiUser() throws TimeoutException {

        for (int i = 0; i < Config.CONCURRENT_USER; ++i) {

            // 커넥션을 생성하고 세션 정보가 담긴 객체를 리턴 받습니다.

            ConnectorSession session = connector.addSession(connector.getHostIncrementedValue("account"), connector.makeUniqueId());

            // 세션에 대해 유저를 등록하고, 각종 ID 정보가 담긴 유저객체를 리턴 받습니다.
            // 하나의 세션에 여러 유저를 등록할 수 있습니다.

            // 여기서는 커스텀 클래스를 지정하여, 등록한 콜백에서 쉽게 활용할 수 있도록 합니다.
            SampleUserClass sampleUser = session.addUser(ServiceName, SampleUserClass.class);
        }

        //connector.repeatByEntire(/* ... */);
        connector.repeatByIndividual(new GameAnvilConnector.InitialProtocol() {
            @Override
            public void send(IAsyncConnectorUser iUser) {
                iUser.authentication(iUser.getAccountId());
            }
        }, 3);
    }

}
