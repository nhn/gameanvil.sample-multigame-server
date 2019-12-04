package stress_test.Cmd;

import static org.junit.Assert.fail;

import com.nhnent.tardis.connector.callback.parent.IDispatchPacket;
import com.nhnent.tardis.connector.callback.parent.IDispatchTimer;
import com.nhnent.tardis.connector.protocol.Packet;
import com.nhnent.tardis.connector.tcp.agent.parent.ITimerTask;
import com.nhnent.tardis.sample.protocol.Sample;
import stress_test.SampleUserClass;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CallbackChatMessageToC implements IDispatchPacket<SampleUserClass> {

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {

        Sample.ChatMessageToC messageToC;
        try {
            messageToC = Sample.ChatMessageToC.parseFrom(packet.getStream());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
            return;
        }

        if (messageToC.getMessage().contains("[" + user.getUserId() + "]") &&
            messageToC.getMessage().contains("Geronimo!!!")) {

            if (user.getSendCount() < 3) {
                user.incSendCount();

                // 타이머를 등록하여 무언가 작동하도록 해봅니다.
                user.addTimer(10, TimeUnit.MILLISECONDS, 1, new ChatTimer(), user); // 시간 간격, 호출 횟수를 지정.

            } else if (user.getSendCount() == 3) {
                user.incSendCount();
                user.leaveRoom();
            } else {
                fail(user.getUserId() + " SendCount is over 3 : " + user.getSendCount());
            }
        }
    }

    //-------------------------------------------------------------------------------------

    class ChatTimer implements IDispatchTimer {

        @Override
        public void dispatch(ITimerTask timer, Object fromAddTimer) {

            // 이 타이머에서는 패킷을 보내도록 해봅니다.
            SampleUserClass user = (SampleUserClass) fromAddTimer;
            user.send(Sample.ChatMessageToS.newBuilder().setMessage("Geronimo!!!"));
        }
    }

}
