package stress_test;

import com.nhn.gameanvil.gamehammer.scenario.ScenarioMachine;
import com.nhn.gameanvil.gamehammer.scenario.ScenarioTest;
import com.nhn.gameanvil.gamehammer.tester.Tester;
import com.nhn.gameanvil.gamehammer.tester.TimeoutStatistics;
import com.nhn.gameanvil.sample.protocol.Sample;
import java.util.concurrent.TimeoutException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stress_test.states.AuthState;
import stress_test.states.ChatState;
import stress_test.states.ConnectState;
import stress_test.states.LeaveRoomState;
import stress_test.states.LoginState;
import stress_test.states.LogoutState;
import stress_test.states.NamedRoomState;
import stress_test.states.RegisterNickNameState;

public class Stress {

    static final String ServiceName = "ChatService";
    static final String[] ChannelId = {"1", "2", "3", "4"};
    static final String UserType = "ChatUser";
    static final String RoomType = "ChatRoom";

    private static Tester tester;
    private static ScenarioTest<SampleActor> scenarioTest;
    private Logger logger = LoggerFactory.getLogger(getClass());

    //-------------------------------------------------------------------------------------

    @BeforeClass
    public static void configuration() {

        tester = Tester.newBuilder()
            .addRemoteInfo("127.0.0.1", 11200)  // 테스트 하려는 서버의 IP 와 Port 를 지정합니다.
            .setDefaultPacketTimeoutSeconds(3)  // 패킷 수신에 대한 타임아웃 시간을 지정합니다. (밀리초)
            .addProtoBufClass(0, Sample.getDescriptor()) // 컨텐츠 프로토콜을 등록합니다..
            .addServiceInfo(1, ServiceName, ChannelId) // 컨텐츠 서비스 정보를 등록합니다.
            .setScenarioLoopCount(-1) // 테스트 반복 횟수를 지정합니다. (-1인 경우 무한 반복)
            .setTestTimeSeconds(180) // 총 테스트 시간을 설정합니다. (초)
            .setActorCount(3000) // 테스트에 사용할 가상 유저의 수를 지정합니다.
            .setTcpNoDelay(false)
            .Build();

        ScenarioMachine<SampleActor> scenario = new ScenarioMachine<>("Sample A");
        scenario.addState(new ConnectState());
        scenario.addState(new AuthState());
        scenario.addState(new LoginState());
        scenario.addState(new LogoutState());
        scenario.addState(new RegisterNickNameState());
        scenario.addState(new NamedRoomState());
        scenario.addState(new LeaveRoomState());
        scenario.addState(new ChatState());

        scenarioTest = new ScenarioTest<>(scenario);
    }

    //-------------------------------------------------------------------------------------

    @Test
    public void runMultiUser() throws TimeoutException {

        logger.info("Test Start!!!");
        scenarioTest.start(tester,
            SampleActor.class,
            ConnectState.class,
            true
        );

        logger.info(scenarioTest.printStatistics("Finished"));
        logger.info(TimeoutStatistics.getInstance().printClientTimeout());
    }

}
