package stress_test.states;


import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.gamehammer.tool.UuidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stress_test.SampleActor;

public class AuthState extends State<SampleActor> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private UuidGenerator uuidActor = new UuidGenerator("AccountId");
    private UuidGenerator uuidDevice = new UuidGenerator("DeviceId");

    @Override
    protected void onEnter(SampleActor sampleActor) {
        String AccountId = uuidActor.generateUniqueId();
        sampleActor.getConnection().authentication(authenticationResult -> {
            if (authenticationResult.isSuccess()) {
                sampleActor.changeState(LoginState.class);
            } else {
                sampleActor.finish(false);
            }
        }, AccountId, AccountId, uuidDevice.generateUniqueId());
    }

    @Override
    protected void onExit(SampleActor actor) {

    }
}
