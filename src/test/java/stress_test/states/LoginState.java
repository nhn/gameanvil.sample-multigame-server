package stress_test.states;

import com.nhn.gameanvil.gamehammer.scenario.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stress_test.SampleActor;

public class LoginState extends State<SampleActor> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void onEnter(SampleActor actor) {
        actor.getUser().login(loginRes -> {
            if (loginRes.isSuccess()) {
                actor.changeState(RegisterNickNameState.class);
            } else {
                logger.info(
                        "[{}] Fail - uuid : {}, AccountId : {}\t{}, {}",
                        getStateName(),
                        actor.getConnection().getUuid(),
                        actor.getConnection().getAccountId(),
                        loginRes.getErrorCode(),
                        loginRes.getResultCode()
                );
                actor.finish(false);
            }
        }, SampleActor.userType, actor.getConnection().getConfig().getNextChannelId(SampleActor.serviceName));
    }

    @Override
    protected void onExit(SampleActor actor) {

    }
}
