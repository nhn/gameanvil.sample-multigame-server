package stress_test.Cmd;

import com.nhnent.tardis.connector.callback.parent.IDispatchPacket;
import com.nhnent.tardis.connector.protocol.Packet;
import com.nhnent.tardis.connector.protocol.result.LoginResult;
import com.nhnent.tardis.sample.protocol.Sample;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stress_test.SampleUserClass;

import static org.junit.Assert.assertEquals;

public class CallbackLoginRes implements IDispatchPacket<SampleUserClass> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {

        LoginResult result = user.parseLoginResult(packet);
        assertEquals(true,result.isSuccess());

        try {
            user.setSendCount(0);

            //int nicknameRand = (int) Math.random() * 10000;
            //String nickName = String.format("doctor %d",nicknameRand);
            Sample.RegisterNickNameReq.Builder req = Sample.RegisterNickNameReq.newBuilder().setNickName(user.getUserId());
            user.request(new Packet(req), Sample.RegisterNickNameRes.class);

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

}
