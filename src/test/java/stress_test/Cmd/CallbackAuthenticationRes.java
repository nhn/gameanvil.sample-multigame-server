package stress_test.Cmd;

import com.nhnent.tardis.connector.callback.parent.IDispatchPacket;
import com.nhnent.tardis.connector.protocol.Packet;
import com.nhnent.tardis.connector.protocol.result.AuthenticationResult;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stress_test.Stress;
import stress_test.SampleUserClass;

import static org.junit.Assert.assertTrue;

public class CallbackAuthenticationRes implements IDispatchPacket<SampleUserClass> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private static int channel = 1;
    private static String getChannel(){
        String ret = String.valueOf(channel++);
        if(channel > 2)
            channel = 1;
        return ret;
    }

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {

        //응답확인
        AuthenticationResult result = user.parseAuthenticationResult(packet);
        assertTrue(result.isSuccess());

        try {
            //user.login(Stress.UserType, getChannel());
            user.login(Stress.UserType, "1");
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

}
