package stress_test.Cmd;

import com.nhnent.tardis.connector.callback.parent.IDispatchPacket;
import com.nhnent.tardis.connector.protocol.Packet;
import com.nhnent.tardis.connector.protocol.result.LogoutResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stress_test.SampleUserClass;

import static org.junit.Assert.assertTrue;

public class CallbackLogout implements IDispatchPacket<SampleUserClass> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public void dispatch(Packet packet, SampleUserClass user) {

        LogoutResult result = user.parseLogoutResult(packet);
        assertTrue(result.isSuccess());

        // 만약, 시나리오의 마지막 단계라면 finish 처리 해 줍니다.
        user.finish();
    }
}
