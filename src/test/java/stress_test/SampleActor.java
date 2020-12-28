package stress_test;

import com.nhn.gameanvil.gamehammer.scenario.ScenarioActor;
import com.nhn.gameanvil.gamehammer.tester.User;

public class SampleActor extends ScenarioActor<SampleActor> {
    public static final String serviceName = "ChatService";
    public static final String userType = "ChatUser";
    public static final String RoomType = "ChatRoom";
    User user;
    private int sendCount = 0;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getSendCount() {
        return sendCount;
    }
    public void incSendCount(){
        ++sendCount;
    }
    public void setSendCount(int sendCount) {
        this.sendCount = sendCount;
    }

}
