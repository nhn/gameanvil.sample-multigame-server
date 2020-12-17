package stress_test.states;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.sample.protocol.Sample;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stress_test.SampleActor;

public class ChatState extends State<SampleActor> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void onEnter(SampleActor actor) {
        actor.getUser().addListener(Sample.ChatMessageToC.getDescriptor(), packetResult -> {
            if (packetResult.isSuccess()) {
                try {
                    Sample.ChatMessageToC messageToC = Sample.ChatMessageToC.parseFrom(packetResult.getStream());
                    if (messageToC.getMessage().contains(Integer.toString(actor.getUser().getUserId()))) {
                        if (actor.getSendCount() < 3) {
                            actor.incSendCount();
                            actor.changeState(ChatState.class);
                        } else {
                            actor.setSendCount(0);
                            actor.getUser().removeAllListener(Sample.ChatMessageToC.getDescriptor());
                            actor.changeState(LeaveRoomState.class);
                        }
                    }
                } catch (IOException e) {
                    actor.finish(false);
                }
            } else {
                actor.finish(false);
            }
        });
        actor.getUser().send(Sample.ChatMessageToS.newBuilder().setMessage("Hello!! GameAnvil!!").build());
    }

    @Override
    protected void onExit(SampleActor actor) {
        actor.getUser().removeAllListener(Sample.ChatMessageToC.getDescriptor());
    }
}
