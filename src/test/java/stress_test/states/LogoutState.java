package stress_test.states;

import com.nhn.gameanvil.gamehammer.scenario.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stress_test.SampleActor;

public class LogoutState extends State<SampleActor> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void onEnter(SampleActor actor) {
        actor.getUser().logout(resultLogout -> {
            if (resultLogout.isSuccess()) {
                actor.finish(true);
            } else {
                logger.info(
                    "[{}] Fail - uuid : {}, AccountId : {}\t{}, {}",
                    getStateName(),
                    actor.getConnection().getUuid(),
                    actor.getConnection().getAccountId(),
                    resultLogout.getErrorCode(),
                    resultLogout.getResultCode()
                );
                actor.changeState(LogoutState.class);
            }
        });
    }

    @Override
    protected void onExit(SampleActor actor) {

    }
}
