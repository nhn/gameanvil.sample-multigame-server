package stress_test.states;

import static org.slf4j.LoggerFactory.getLogger;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.gamehammer.tester.ResultConnect.ResultCodeConnect;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import stress_test.SampleActor;

/**
 * Connect 처리를 정의한 상태 클래스.
 */
public class ConnectState extends State<SampleActor> {
    private static final Logger logger = getLogger(ConnectState.class);
    final AtomicInteger connectCount;

    public ConnectState() {
        connectCount = new AtomicInteger(0);
    }

    protected void onScenarioTestStart(SampleActor sampleActor) {
        sampleActor.getConnection().addListenerDisconnect((resultDisconnect) -> {
            logger.info("[{}] Disconnected - UUID : {}",
                    sampleActor.getCurrentStateName(),
                    sampleActor.getConnection().getUuid()
            );
            sampleActor.finish(false);
        });

        sampleActor.setUser(sampleActor.getConnection().createUser(SampleActor.serviceName, 1));
    }

    protected void onEnter(SampleActor scenarioActor) {
        if (scenarioActor.getConnection().isConnected()) {
            scenarioActor.changeState(AuthState.class);
        } else {
            long connectTime = scenarioActor.getCurrTime();
            scenarioActor.getConnection().connect(scenarioActor.getConnection().getConfig().getNextRemoteInfo(), resultConnect -> {
                if (ResultCodeConnect.CONNECT_SUCCESS == resultConnect.getResultCode()) {
                    scenarioActor.changeState(AuthState.class);
                } else {
                    logger.info("[{}] Fail - UUID : {}, errorCode : {}, resultCode : {}, socketException : {}, elapsedTime : {}",
                            getStateName(),
                            scenarioActor.getConnection().getUuid(),
                            resultConnect.getErrorCode(),
                            resultConnect.getResultCode(),
                            resultConnect.getSocketError(),
                            scenarioActor.getCurrTime() - connectTime
                    );
                    scenarioActor.finish(false);
                }
            });
        }
    }

    protected void onExit(SampleActor scenarioActor) {
    }
}
