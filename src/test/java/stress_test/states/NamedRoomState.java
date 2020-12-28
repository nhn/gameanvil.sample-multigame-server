package stress_test.states;

import com.nhn.gameanvil.gamehammer.scenario.State;
import stress_test.SampleActor;

public class NamedRoomState extends State<SampleActor> {

    @Override
    protected void onEnter(SampleActor actor) {
        int nicknameRand = (int) (Math.random() * 1000) / 5;
        String roomName = String.valueOf(nicknameRand);

        actor.getUser().namedRoom(res -> {
            if(res.isSuccess()){
                actor.changeState(ChatState.class);
            }else{
                actor.finish(false);
            }
        }, SampleActor.RoomType, roomName, false);
    }

    @Override
    protected void onExit(SampleActor actor) {

    }
}
