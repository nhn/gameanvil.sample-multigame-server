package stress_test.Cmd;

import com.nhnent.tardis.chat.protocol.Chat;
import com.nhnent.tardis.connector.callback.parent.IDispatchPacket;
import com.nhnent.tardis.connector.protocol.Packet;
import com.nhnent.tardis.connector.protocol.result.NamedRoomResult;
import stress_test.SampleUserClass;

import static org.junit.Assert.assertEquals;

public class CallbackNamedRoomRes implements IDispatchPacket<SampleUserClass> {

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {

        NamedRoomResult result = user.parseNamedRoomResult(packet);
        assertEquals(true,result.isSuccess());

        user.send(Chat.ChatMessageToS.newBuilder().setMessage("Geronimo!!!"));
    }
}
