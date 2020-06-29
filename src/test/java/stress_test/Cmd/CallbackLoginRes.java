package stress_test.Cmd;

import com.nhn.gameflexcore.connector.callback.parent.IDispatchPacket;
import com.nhn.gameflexcore.connector.protocol.Packet;
import com.nhn.gameflexcore.connector.protocol.result.LoginResult;
import com.nhnent.tardis.sample.protocol.Sample;
import stress_test.SampleUserClass;

import static org.junit.Assert.assertEquals;

public class CallbackLoginRes implements IDispatchPacket<SampleUserClass> {

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {

        LoginResult result = user.parseLoginResult(packet);
        assertEquals(true, result.isSuccess());

        user.setSendCount(0);

        //int nicknameRand = (int) Math.random() * 10000;
        //String nickName = String.format("doctor %d",nicknameRand);
        Sample.RegisterNickNameReq.Builder req = Sample.RegisterNickNameReq.newBuilder().setNickName("" + user.getUserId());
        user.request(new Packet(req), Sample.RegisterNickNameRes.class);
    }

}
