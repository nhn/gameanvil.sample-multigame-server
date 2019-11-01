package stress_test.Cmd;

import com.nhnent.tardis.chat.protocol.Chat;
import com.nhnent.tardis.connector.callback.parent.IDispatchPacket;
import com.nhnent.tardis.connector.callback.parent.IDispatchTimer;
import com.nhnent.tardis.connector.protocol.Packet;
import com.nhnent.tardis.connector.tcp.agent.parent.ITimerTask;
import stress_test.SampleUserClass;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CallbackChatMessageToC implements IDispatchPacket<SampleUserClass> {

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {

        Chat.ChatMessageToC messageToC;
        try {
            messageToC = Chat.ChatMessageToC.parseFrom(packet.getStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if(messageToC.getMessage().contains(user.getUserId())){

            if(user.getSendCount() < 3){
                user.incSendCount();

                // 타이머를 등록하여 무언가 작동하도록 해봅니다.
                user.addTimer(10, TimeUnit.MILLISECONDS, 1, new ChatTimer(), user); // 시간 간격, 호출 횟수를 지정.

            }else{
                user.leaveRoom();
            }
        }
    }

    //-------------------------------------------------------------------------------------

    class ChatTimer implements IDispatchTimer {

        @Override
        public void dispatch(ITimerTask timer, Object fromAddTimer) {

            // 이 타이머에서는 패킷을 보내도록 해봅니다.
            SampleUserClass user = (SampleUserClass) fromAddTimer;
            user.send(Chat.ChatMessageToS.newBuilder().setMessage("Geronimo!!!"));
        }
    }

}
