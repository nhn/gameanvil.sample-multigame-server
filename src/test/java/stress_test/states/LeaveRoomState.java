package stress_test.states;

import com.nhn.gameanvil.gamehammer.scenario.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stress_test.SampleActor;

public class LeaveRoomState extends State<SampleActor> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void onEnter(SampleActor actor) {
        actor.getUser().leaveRoom(resultLeaveRoom -> {
            if (resultLeaveRoom.isSuccess()) {
                actor.changeState(LogoutState.class);
            } else {
                logger.info(
                    "[{}] Fail - uuid : {}, AccountId : {}\t{}, {}",
                    getStateName(),
                    actor.getConnection().getUuid(),
                    actor.getConnection().getAccountId(),
                    resultLeaveRoom.getErrorCode(),
                    resultLeaveRoom.getResultCode()
                );
                actor.changeState(LeaveRoomState.class);
            }
        });
    }

    @Override
    protected void onExit(SampleActor actor) {

    }
}
