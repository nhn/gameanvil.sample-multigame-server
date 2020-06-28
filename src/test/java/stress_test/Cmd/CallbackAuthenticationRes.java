package stress_test.Cmd;

import com.nhn.gameflexcore.connector.callback.parent.IDispatchPacket;
import com.nhn.gameflexcore.connector.protocol.Packet;
import com.nhn.gameflexcore.connector.protocol.result.AuthenticationResult;
import java.util.concurrent.atomic.AtomicInteger;
import stress_test.Stress;
import stress_test.SampleUserClass;

import static org.junit.Assert.assertTrue;

public class CallbackAuthenticationRes implements IDispatchPacket<SampleUserClass> {

    private static AtomicInteger channel = new AtomicInteger();

    private static String getChannel() {
        return String.valueOf((channel.getAndIncrement() % 4) + 1);
    }

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {

        //응답확인
        AuthenticationResult result = user.parseAuthenticationResult(packet);
        assertTrue(result.isSuccess());
        user.login(Stress.UserType, getChannel());
        //user.login(Stress.UserType, "1");
    }

}
