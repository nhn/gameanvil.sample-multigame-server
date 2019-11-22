package unit_test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.nhnent.tardis.common.protocol.Base;
import com.nhnent.tardis.common.protocol.Base.ResultCodeMatchPartyCancel;
import com.nhnent.tardis.common.protocol.Base.ResultCodeMatchPartyStart;
import com.nhnent.tardis.common.protocol.Base.ResultCodeMatchUserDone;
import com.nhnent.tardis.common.protocol.Base.ResultCodeNamedRoom;
import com.nhnent.tardis.connector.common.Config;
import com.nhnent.tardis.connector.protocol.Packet;
import com.nhnent.tardis.connector.protocol.result.AuthenticationResult;
import com.nhnent.tardis.connector.protocol.result.LeaveRoomResult;
import com.nhnent.tardis.connector.protocol.result.LoginResult;
import com.nhnent.tardis.connector.protocol.result.MatchPartyCancelResult;
import com.nhnent.tardis.connector.protocol.result.MatchPartyStartResult;
import com.nhnent.tardis.connector.protocol.result.MatchRoomResult;
import com.nhnent.tardis.connector.protocol.result.MatchUserCancelResult;
import com.nhnent.tardis.connector.protocol.result.MatchUserStartResult;
import com.nhnent.tardis.connector.protocol.result.NamedRoomResult;
import com.nhnent.tardis.connector.tcp.ConnectorSession;
import com.nhnent.tardis.connector.tcp.ConnectorUser;
import com.nhnent.tardis.connector.tcp.TardisConnector;
import com.nhnent.tardis.sample.protocol.Sample;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GameTest {
    public static String ServiceName = "GameService";
    public static String UserType = "GameUser";
    public static String RoomType_MatchRoom = "GameRoom_MatchRoom";
    public static String RoomType_MatchUser = "GameRoom_MatchUser";
    public static String RoomType_MatchUserParty = "GameRoom_MatchUserParty";
    public static String RoomType_Party = "PartyRoom";

    private static TardisConnector connector;
    private List<ConnectorUser> users = new ArrayList<>();

    //-------------------------------------------------------------------------------------

    @BeforeClass
    public static void configuration() {

        // 테스트 하려는 서버의 IP 와 Port 를 지정합니다.
        Config.addRemoteInfo("127.0.0.1", 11200);
        Config.WAIT_RECV_TIMEOUT_MSEC = 5000;

        // 커넥터와, Base 프로토콜 사용 편의를 위해 Helper 를 생성합니다.
        connector = TardisConnector.getInstance();

        // 컨텐츠 프로토콜 등록.
        connector.addProtoBufClass(0, Sample.class);

        // 컨텐츠 서비스 등록.
        connector.addService(0, "ChatService");
        connector.addService(1, ServiceName);
    }

    //-------------------------------------------------------------------------------------

    @Before
    public void setUp() throws TimeoutException {

        for (int i=0; i<4; ++i) {

            // 커넥션을 생성하고 세션 정보가 담긴 객체를 리턴.
            ConnectorSession session = connector.addSession(connector.getIncrementedValue("account_"), connector.makeUniqueId());

            // 인증을 진행.
            AuthenticationResult authResult = session.authentication(session.getAccountId());
            assertTrue("Authentication fail", authResult.isSuccess());

            // 세션에 유저를 등록하고, 각종 ID 정보가 담긴 유저 객체를 리턴.
            ConnectorUser user = session.addUser(ServiceName);

            // 로그인을 진행.
            LoginResult loginResult = user.login(UserType, String.valueOf(i+1));
            assertTrue("Login fail", loginResult.isSuccess());

            // Test 단계에서 활용하도록 준비합니다.
            users.add(user);
        }
    }

    @After
    public void tearDown() throws TimeoutException {

        for(ConnectorUser user : users){
            user.logout();
            user.getSession().disconnect();
        }
    }

    @Test
    public void MatchRoomAndChat() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser account1 = users.get(0);
        ConnectorUser account2 = users.get(1);

        // 채팅방 입장
        MatchRoomResult matchRoomResult1 = account1.matchRoom(RoomType_MatchRoom);
        assertTrue(matchRoomResult1.isSuccess());
        assertTrue(matchRoomResult1.isMatchRoomCreated());

        MatchRoomResult matchRoomResult2 = account2.matchRoom(RoomType_MatchRoom);
        assertTrue(matchRoomResult2.isSuccess());
        assertTrue(matchRoomResult2.isMatchRoomJoined());

        Sample.GameMessageToC account1GetAccount2JoinMsg = account1.waitProtoPacket(5, TimeUnit.SECONDS, Sample.GameMessageToC.class);
        assertEquals(account2.getUserId() + " is join", account1GetAccount2JoinMsg.getMessage());

        // 채팅 메시지 전송.
        account1.send(new Packet(Sample.GameMessageToS.newBuilder().setMessage("Hello Tardis!")));

        // 다른 유저에게도 응답이 왔는지 확인.
        Sample.GameMessageToC gameMessageToC = account2.waitProtoPacket(1, TimeUnit.SECONDS, Sample.GameMessageToC.class);
        assertEquals("[" + account1.getUserId() + "] Hello Tardis!",gameMessageToC.getMessage());
    }

    @Test
    public void MatchRoomMoveAndChat() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser account1 = users.get(0);
        ConnectorUser account2 = users.get(1);
        ConnectorUser account3 = users.get(2);

        MatchRoomResult matchRoomResult1 = account1.matchRoom(RoomType_MatchRoom);
        assertTrue(matchRoomResult1.isSuccess());
        assertTrue(matchRoomResult1.isMatchRoomCreated());
        String roomId = matchRoomResult1.getRoomId();

        MatchRoomResult matchRoomResult2 = account2.matchRoom(RoomType_MatchRoom);
        assertTrue(matchRoomResult2.isSuccess());
        assertTrue(matchRoomResult2.isMatchRoomJoined());
        assertEquals(roomId, matchRoomResult2.getRoomId());

        Sample.GameMessageToC account1GetAccount2JoinMsg = account1.waitProtoPacketByFirstReceived(5, TimeUnit.SECONDS, Sample.GameMessageToC.class);
        assertEquals(account2.getUserId() + " is join", account1GetAccount2JoinMsg.getMessage());

        // 채팅방 입장
        MatchRoomResult matchRoomResult3 = account1.matchRoom(true, RoomType_MatchRoom, true);
        assertTrue(matchRoomResult3.isSuccess());
        assertTrue(matchRoomResult3.isMatchRoomCreated());
        assertNotEquals(roomId, matchRoomResult3.getRoomId());

        LeaveRoomResult leaveRoomResult = account2.leaveRoom();
        assertTrue(leaveRoomResult.isSuccess());

        MatchRoomResult matchRoomResult4 = account3.matchRoom(RoomType_MatchRoom);
        assertTrue(matchRoomResult4.isSuccess());
        assertTrue(matchRoomResult4.isMatchRoomJoined());
        assertEquals(matchRoomResult3.getRoomId(), matchRoomResult4.getRoomId());

        Sample.GameMessageToC account1GetAccount3JoinMsg = account1.waitProtoPacketByFirstReceived(5, TimeUnit.SECONDS, Sample.GameMessageToC.class);
        assertEquals(account3.getUserId() + " is join", account1GetAccount3JoinMsg.getMessage());

        // 채팅 메시지 전송.
        account1.send(new Packet(Sample.GameMessageToS.newBuilder().setMessage("Hello Tardis!")));

        // 다른 유저에게도 응답이 왔는지 확인.
        Sample.GameMessageToC gameMessageToC = account3.waitProtoPacket(1, TimeUnit.SECONDS, Sample.GameMessageToC.class);
        assertEquals("[" + account1.getUserId() + "] Hello Tardis!",gameMessageToC.getMessage());
    }

    @Test
    public void MatchRoomAndDisconnect() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser account1 = users.get(0);
        ConnectorUser account2 = users.get(1);

        // 채팅방 입장
        MatchRoomResult matchRoomResult1 = account1.matchRoom(RoomType_MatchRoom);
        assertTrue(matchRoomResult1.isSuccess());
        assertTrue(matchRoomResult1.isMatchRoomCreated());

        MatchRoomResult matchRoomResult2 = account2.matchRoom(RoomType_MatchRoom);
        assertTrue(matchRoomResult2.isSuccess());
        assertTrue(matchRoomResult2.isMatchRoomJoined());

        Sample.GameMessageToC account1GetAccount2JoinMsg = account1.waitProtoPacket(1, TimeUnit.SECONDS, Sample.GameMessageToC.class);
        assertEquals(account2.getUserId() + " is join", account1GetAccount2JoinMsg.getMessage());

        String accountId = account1.getSession().getAccountId();
        String deviceId = account1.getSession().getDeviceId();
        account1.getSession().disconnect();

        // TearDown에서 제외하기 위해 users에서 제외
        users.remove(account1);
    }

    @Test
    public void MatchUserAndChat() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser account1 = users.get(0);
        ConnectorUser account2 = users.get(1);

        // 채팅방 입장
        MatchUserStartResult matchUserStartResult1 = account1.matchUserStart(RoomType_MatchUser);
        assertTrue(matchUserStartResult1.isSuccess());

        MatchUserStartResult matchUserStartResult2 = account2.matchUserStart(RoomType_MatchUser);
        assertTrue(matchUserStartResult2.isSuccess());

        Base.MatchUserDone matchUserDone1 = account1.waitProtoPacket(5, TimeUnit.SECONDS, Base.MatchUserDone.class);
        Base.MatchUserDone matchUserDone2 = account2.waitProtoPacket(5, TimeUnit.SECONDS, Base.MatchUserDone.class);

        Sample.GameMessageToC account1GetAccount2JoinMsg = account1.waitProtoPacket(5, TimeUnit.SECONDS, Sample.GameMessageToC.class);
        assertEquals(account2.getUserId() + " is join", account1GetAccount2JoinMsg.getMessage());

        Sample.GameMessageToC account2GetAccount1JoinMsg = account2.waitProtoPacket(5, TimeUnit.SECONDS, Sample.GameMessageToC.class);
        assertEquals(account1.getUserId() + " is join", account2GetAccount1JoinMsg.getMessage());

        // 채팅 메시지 전송.
        account1.send(new Packet(Sample.GameMessageToS.newBuilder().setMessage("Hello Tardis!")));

        // 다른 유저에게도 응답이 왔는지 확인.
        Sample.GameMessageToC gameMessageToC = account2.waitProtoPacket(1, TimeUnit.SECONDS, Sample.GameMessageToC.class);
        assertEquals("["+account1.getUserId()+"] Hello Tardis!",gameMessageToC.getMessage());
    }

    @Test
    public void MatchUserAndRefillAndChat() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser account1 = users.get(0);
        ConnectorUser account2 = users.get(1);
        ConnectorUser account3 = users.get(2);

        // 채팅방 입장
        MatchUserStartResult matchUserStartResult1 = account1.matchUserStart(RoomType_MatchUser);
        assertTrue(matchUserStartResult1.isSuccess());

        MatchUserStartResult matchUserStartResult2 = account2.matchUserStart(RoomType_MatchUser);
        assertTrue(matchUserStartResult2.isSuccess());

        Base.MatchUserDone matchUserDone1 = account1.waitProtoPacket(10, TimeUnit.SECONDS, Base.MatchUserDone.class);
        Base.MatchUserDone matchUserDone2 = account2.waitProtoPacket(1, TimeUnit.SECONDS, Base.MatchUserDone.class);

        Sample.GameMessageToC account1GetAccount2JoinMsg = account1.waitProtoPacket(5, TimeUnit.SECONDS, Sample.GameMessageToC.class);
        assertEquals(account2.getUserId() + " is join", account1GetAccount2JoinMsg.getMessage());

        Sample.GameMessageToC account2GetAccount1JoinMsg = account2.waitProtoPacket(1, TimeUnit.SECONDS, Sample.GameMessageToC.class);
        assertEquals(account1.getUserId() + " is join", account2GetAccount1JoinMsg.getMessage());

        LeaveRoomResult leaveRoomResult = account2.leaveRoom();
        assertTrue(leaveRoomResult.isSuccess());

        Thread.sleep(500);

        MatchUserStartResult matchUserStartResult3 = account3.matchUserStart(RoomType_MatchUser);
        assertTrue(matchUserStartResult3.isSuccess());

        Base.MatchUserDone matchUserDone3 = account3.waitProtoPacket(5, TimeUnit.SECONDS, Base.MatchUserDone.class);
        assertTrue(matchUserDone3.getResultCode() == ResultCodeMatchUserDone.MATCH_USER_DONE_SUCCESS);

        Sample.GameMessageToC account1GetAccount3JoinMsg = account1.waitProtoPacket(5, TimeUnit.SECONDS, Sample.GameMessageToC.class);
        assertEquals(account3.getUserId() + " is join", account1GetAccount3JoinMsg.getMessage());

        // 채팅 메시지 전송.
        account1.send(new Packet(Sample.GameMessageToS.newBuilder().setMessage("Hello Tardis!")));

        // 다른 유저에게도 응답이 왔는지 확인.
        Sample.GameMessageToC gameMessageToC = account3.waitProtoPacket(1, TimeUnit.SECONDS, Sample.GameMessageToC.class);
        assertEquals("[" + account1.getUserId() + "] Hello Tardis!",gameMessageToC.getMessage());
    }

    @Test
    public void MatchUserAndCancel() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser account1 = users.get(0);
        ConnectorUser account2 = users.get(1);

        // 채팅방 입장
        MatchUserStartResult matchUserStartResult1 = account1.matchUserStart(RoomType_MatchUser);
        assertTrue(matchUserStartResult1.isSuccess());

        MatchUserStartResult matchUserStartResult2 = account2.matchUserStart(RoomType_MatchUser);
        assertTrue(matchUserStartResult2.isSuccess());

        MatchUserCancelResult matchUserCancelResult = account2.matchUserCancel(RoomType_MatchUser);
        assertTrue(matchUserCancelResult.isSuccess());

        Base.MatchUserTimeout matchUserTimeout = account1.waitProtoPacket(6, TimeUnit.SECONDS, Base.MatchUserTimeout.class);
    }

    public void makePartyRoom(Collection<ConnectorUser> users, String roomId) throws TimeoutException {
        List<ConnectorUser> members = new ArrayList<>();
        for (ConnectorUser user : users) {
            NamedRoomResult namedRoomResult1 = user.namedRoom(RoomType_Party, roomId, true);
            assertEquals(ResultCodeNamedRoom.NAMED_ROOM_SUCCESS, ResultCodeNamedRoom.forNumber(namedRoomResult1.getResultCode()));
            System.out.println("NAMED_ROOM_SUCCESS : " + user.getUserId());
            for (ConnectorUser member : members) {
                Sample.GameMessageToC message = member.waitProtoPacketByFirstReceived(5, TimeUnit.SECONDS, Sample.GameMessageToC.class);
                assertEquals(user.getUserId() + " is join", message.getMessage());
            }

            members.add(user);
        }
    }

    public  void startMatchParty(Collection<ConnectorUser> users) throws TimeoutException {
        boolean first = true;
        for(ConnectorUser user:users){
            if(first){
                MatchPartyStartResult matchPartyStartResult = user.matchPartyStart(RoomType_MatchUserParty);
                assertEquals(ResultCodeMatchPartyStart.MATCH_PARTY_START_SUCCESS, ResultCodeMatchPartyStart.forNumber(matchPartyStartResult.getResultCode()));
                first = false;
            }else{
                Base.MatchPartyStartResOrNoti matchPartyStartResOrNoti = user.waitProtoPacketByFirstReceived(5, TimeUnit.SECONDS, Base.MatchPartyStartResOrNoti.class);
                assertEquals(ResultCodeMatchPartyStart.MATCH_PARTY_START_SUCCESS, matchPartyStartResOrNoti.getResultCode());
            }
        }
    }

    public void cancelMatchParty(Collection<ConnectorUser> users) throws TimeoutException {
        boolean first = true;
        for(ConnectorUser user:users){
            if(first){
                MatchPartyCancelResult matchPartyCancelResult = user.matchPartyCancel(RoomType_MatchUserParty);
                assertEquals(ResultCodeMatchPartyCancel.MATCH_PARTY_CANCEL_SUCCESS, ResultCodeMatchPartyCancel.forNumber(matchPartyCancelResult.getResultCode()));
                first = false;
            }else{
                Base.MatchPartyCancelResOrNoti matchPartyCancelResOrNoti = user.waitProtoPacketByFirstReceived(5, TimeUnit.SECONDS, Base.MatchPartyCancelResOrNoti.class);
                assertEquals(ResultCodeMatchPartyCancel.MATCH_PARTY_CANCEL_SUCCESS, matchPartyCancelResOrNoti.getResultCode());
            }
        }
    }

    public void checkMatchUserDone(Collection<ConnectorUser> users){
        for(ConnectorUser user:users){
            Base.MatchUserDone matchUserDone = user.waitProtoPacketByFirstReceived(5, TimeUnit.SECONDS, Base.MatchUserDone.class);
            assertEquals(ResultCodeMatchUserDone.MATCH_USER_DONE_SUCCESS, matchUserDone.getResultCode());
        }
    }

    public void checkMatchUserTimeout(Collection<ConnectorUser> users){
        for(ConnectorUser user:users){
            user.waitProtoPacketByFirstReceived(6, TimeUnit.SECONDS, Base.MatchUserTimeout.class);
        }
    }

    public void checkJoinMsg(Collection<ConnectorUser> users){
        Sample.GameMessageToC joinMsg;
        for(ConnectorUser receiver:users){
            for(ConnectorUser sender:users){
                if(receiver == sender)
                    continue;;
                joinMsg = receiver.waitProtoPacketByFirstReceived(5, TimeUnit.SECONDS, Sample.GameMessageToC.class);
                assertEquals(sender.getUserId() + " is join", joinMsg.getMessage());
            }
        }
    }

    @Test
    public void MatchParty() throws TimeoutException, IOException, InterruptedException {

        List<ConnectorUser> party1 = users.subList(0, 2);
        List<ConnectorUser> party2 = users.subList(2, 4);

        // PartyRoom1 입장
        makePartyRoom(party1, "PartyRoom1");
        // PartyRoom2 입장
        makePartyRoom(party2, "PartyRoom2");

        // StartMatchParty1
        startMatchParty(party1);
        // StartMatchParty2
        startMatchParty(party2);

        checkMatchUserDone(users);

        checkJoinMsg(users);
    }

    @Test
    public void MatchPartyAndRefill() throws TimeoutException, IOException, InterruptedException {

        List<ConnectorUser> party1 = users.subList(0, 2);
        List<ConnectorUser> party2 = users.subList(2, 4);

        // PartyRoom1 입장
        makePartyRoom(party1, "PartyRoom1");
        // PartyRoom2 입장
        makePartyRoom(party2, "PartyRoom2");

        startMatchParty(party1);
        startMatchParty(party2);

        cancelMatchParty(party2);
        startMatchParty(party2);

        checkMatchUserDone(users);

        checkJoinMsg(users);
    }

    @Test
    public void MatchPartyCancel() throws TimeoutException, IOException, InterruptedException {

        List<ConnectorUser> party1 = users.subList(0, 2);
        List<ConnectorUser> party2 = users.subList(2, 4);

        // PartyRoom1 입장
        makePartyRoom(party1, "PartyRoom1");
        // PartyRoom2 입장
        makePartyRoom(party2, "PartyRoom2");

        // StartMatchParty1
        startMatchParty(party1);
        // StartMatchParty2
        startMatchParty(party2);

        cancelMatchParty(party1);
        checkMatchUserTimeout(party2);
    }


    @Test
    public void MessageFromServiceNodeAgent() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser doctor = users.get(0);
        ConnectorUser dalek = users.get(1);
        ConnectorUser bobby = users.get(2);
        ConnectorUser jhone = users.get(3);

        Sample.SampleToC sampleToC4 = jhone.waitProtoPacket(5, TimeUnit.SECONDS, Sample.SampleToC.class);
        Sample.SampleToC sampleToC3 = bobby.waitProtoPacket(5, TimeUnit.SECONDS, Sample.SampleToC.class);
        Sample.SampleToC sampleToC2 = dalek.waitProtoPacket(5, TimeUnit.SECONDS, Sample.SampleToC.class);
        Sample.SampleToC sampleToC1 = doctor.waitProtoPacket(5, TimeUnit.SECONDS, Sample.SampleToC.class);

        assertNotEquals(doctor.getChannelId(), dalek.getChannelId());
        assertNotEquals(doctor.getChannelId(), bobby.getChannelId());
        assertNotEquals(doctor.getChannelId(), jhone.getChannelId());


        assertEquals(sampleToC1.getMessage(), sampleToC2.getMessage());
        assertEquals(sampleToC1.getMessage(), sampleToC3.getMessage());
        assertEquals(sampleToC1.getMessage(), sampleToC4.getMessage());
    }
}
