package stress_test.Cmd;

import com.nhnent.tardis.connector.callback.parent.IDispatchPacket;
import com.nhnent.tardis.connector.protocol.Packet;
import com.nhnent.tardis.sample.protocol.Sample;
import stress_test.Stress;
import stress_test.SampleUserClass;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CallbackRegisterNickName implements IDispatchPacket<SampleUserClass> {

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {

        try {
            Sample.RegisterNickNameRes res = Sample.RegisterNickNameRes.parseFrom(packet.getStream());
            assertEquals(true, res.getIsSuccess());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int nicknameRand = (int) (Math.random() * 1000)/5;
        String roomId = String.valueOf(nicknameRand);

        user.namedRoom(Stress.RoomType,roomId);
    }
}
