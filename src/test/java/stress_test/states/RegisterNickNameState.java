package stress_test.states;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.sample.protocol.Sample;
import java.io.IOException;
import stress_test.SampleActor;

public class RegisterNickNameState extends State<SampleActor> {

    @Override
    protected void onEnter(SampleActor actor) {
        actor.setSendCount(0);

        Sample.RegisterNickNameReq.Builder req = Sample.RegisterNickNameReq.newBuilder().setNickName(Integer.toString(actor.getUser().getUserId()));
        actor.getUser().request(req.build(), (res) -> {
            if (res.isSuccess()) {
                try {
                    Sample.RegisterNickNameRes resRegisterNickName = Sample.RegisterNickNameRes.parseFrom(res.getStream());
                    actor.changeState(NamedRoomState.class);
                } catch (IOException e) {
                    actor.finish(false);
                }
            } else {
                actor.finish(false);
            }
        });
    }

    @Override
    protected void onExit(SampleActor actor) {

    }
}
