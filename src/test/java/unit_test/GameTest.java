package unit_test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.nhnent.tardis.common.protocol.Base;
import com.nhnent.tardis.common.protocol.Base.ResultCodeMatchPartyCancel;
import com.nhnent.tardis.common.protocol.Base.ResultCodeMatchPartyStart;
import com.nhnent.tardis.common.protocol.Base.ResultCodeMatchRoom;
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

    private String matchRoom(Collection<ConnectorUser> users) throws TimeoutException {
        String roomId = null;
        List<ConnectorUser> members = new ArrayList<>();
        for(ConnectorUser user : users){
            MatchRoomResult matchRoomResult = user.matchRoom(RoomType_MatchRoom);
            assertEquals(ResultCodeMatchRoom.MATCH_ROOM_SUCCESS, ResultCodeMatchRoom.forNumber(matchRoomResult.getResultCode()));
            assertEquals(members.isEmpty(), matchRoomResult.isMatchRoomCreated());
            assertNotEquals(members.isEmpty(), matchRoomResult.isMatchRoomJoined());
            for (ConnectorUser member : members) {
                Sample.GameMessageToC message = member.waitProtoPacketByFirstReceived(5, TimeUnit.SECONDS, Sample.GameMessageToC.class);
                assertEquals(user.getUserId() + " is join", message.getMessage());
            }
            members.add(user);
            if(roomId == null)
                roomId = matchRoomResult.getRoomId();
            else
                assertEquals(roomId, matchRoomResult.getRoomId());
        }
        return roomId;
    }

    private void checkChatMsg(Collection<ConnectorUser> users, String chatMsg) {
        String senderId = null;
        for (ConnectorUser user : users) {
            if (senderId == null) {
                senderId = user.getUserId();
                user.send(new Packet(Sample.GameMessageToS.newBuilder().setMessage(chatMsg)));
            } else {
                Sample.GameMessageToC gameMessageToC = user.waitProtoPacket(1, TimeUnit.SECONDS, Sample.GameMessageToC.class);
                assertEquals("[" + senderId + "] "+ chatMsg, gameMessageToC.getMessage());
            }
        }
    }

    @Test
    public void MatchRoomAndChat() throws TimeoutException, IOException, InterruptedException {

        List<ConnectorUser> members = users.subList(0, 2);

        // 채팅방 입장
        matchRoom(members);

        checkChatMsg(members, "Hello Tardis!");
    }

    @Test
    public void MatchRoomMoveAndChat() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser account1 = users.get(0);
        ConnectorUser account2 = users.get(1);
        List<ConnectorUser> members = users.subList(0, 2);

        String roomId = matchRoom(members);

        MatchRoomResult matchRoomMoveResult = account1.matchRoom(true, RoomType_MatchRoom, true);
        assertEquals(ResultCodeMatchRoom.MATCH_ROOM_SUCCESS, ResultCodeMatchRoom.forNumber(matchRoomMoveResult.getResultCode()));
        assertNotEquals(roomId, matchRoomMoveResult.getRoomId());

        LeaveRoomResult leaveRoomResult = account2.leaveRoom();
        assertTrue(leaveRoomResult.isSuccess());
        account2.clearRemainWaitPackets();;

        MatchRoomResult matchRoomJoinResult = account2.matchRoom(RoomType_MatchRoom);
        assertTrue(matchRoomJoinResult.isSuccess());
        assertTrue(matchRoomJoinResult.isMatchRoomJoined());
        assertEquals(matchRoomMoveResult.getRoomId(), matchRoomJoinResult.getRoomId());

        Sample.GameMessageToC account1GetAccount3JoinMsg = account1.waitProtoPacketByFirstReceived(5, TimeUnit.SECONDS, Sample.GameMessageToC.class);
        assertEquals(account2.getUserId() + " is join", account1GetAccount3JoinMsg.getMessage());

        checkChatMsg(members, "Hello Tardis!");
    }

    private void matchUser(Collection<ConnectorUser> users) throws TimeoutException{
        for(ConnectorUser user : users){
            MatchUserStartResult matchUserStartResult1 = user.matchUserStart(RoomType_MatchUser);
            assertTrue(matchUserStartResult1.isSuccess());
        }

        for(ConnectorUser user : users){
            user.waitProtoPacket(5, TimeUnit.SECONDS, Base.MatchUserDone.class);
        }

        for(ConnectorUser sender : users){
            for(ConnectorUser listener : users){
                if(sender.getUserId() == listener.getUserId())
                    continue;
                Sample.GameMessageToC joinMsg = listener.waitProtoPacketByFirstReceived(5, TimeUnit.SECONDS, Sample.GameMessageToC.class);
                assertEquals(sender.getUserId() + " is join", joinMsg.getMessage());
            }
        }
    }

    @Test
    public void MatchUserAndChat() throws TimeoutException, IOException, InterruptedException {

        List<ConnectorUser> members = users.subList(0, 2);

        matchUser(members);

        checkChatMsg(members, "Hello Tardis!");
    }

    @Test
    public void MatchUserAndRefillAndChat() throws TimeoutException, IOException, InterruptedException {

        ConnectorUser account1 = users.get(0);
        ConnectorUser account2 = users.get(1);
        List<ConnectorUser> members = users.subList(0, 2);

        matchUser(members);

        LeaveRoomResult leaveRoomResult = account2.leaveRoom();
        assertTrue(leaveRoomResult.isSuccess());

        Thread.sleep(500);

        MatchUserStartResult matchUserStartResult = account2.matchUserStart(RoomType_MatchUser);
        assertTrue(matchUserStartResult.isSuccess());

        Base.MatchUserDone matchUserDone = account2.waitProtoPacket(5, TimeUnit.SECONDS, Base.MatchUserDone.class);
        assertTrue(matchUserDone.getResultCode() == ResultCodeMatchUserDone.MATCH_USER_DONE_SUCCESS);

        Sample.GameMessageToC joinMsg = account1.waitProtoPacket(5, TimeUnit.SECONDS, Sample.GameMessageToC.class);
        assertEquals(account2.getUserId() + " is join", joinMsg.getMessage());

        checkChatMsg(members, "Hello Tardis!");
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

    private void makePartyRoom(Collection<ConnectorUser> users, String roomId) throws TimeoutException {
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

    private  void startMatchParty(Collection<ConnectorUser> users) throws TimeoutException {
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

    private void cancelMatchParty(Collection<ConnectorUser> users) throws TimeoutException {
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

    private void checkMatchUserDone(Collection<ConnectorUser> users){
        for(ConnectorUser user:users){
            Base.MatchUserDone matchUserDone = user.waitProtoPacketByFirstReceived(5, TimeUnit.SECONDS, Base.MatchUserDone.class);
            assertEquals(ResultCodeMatchUserDone.MATCH_USER_DONE_SUCCESS, matchUserDone.getResultCode());
        }
    }

    private void checkMatchUserTimeout(Collection<ConnectorUser> users){
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
    public void MatchPartyAndIndividuals() throws TimeoutException, IOException, InterruptedException {

        List<ConnectorUser> party = users.subList(0, 2);
        List<ConnectorUser> individuals = users.subList(2, 4);

        // PartyRoom 입장
        makePartyRoom(party, "PartyRoom");

        // StartMatchParty
        startMatchParty(party);

        MatchUserStartResult matchUserStartResult1 = users.get(2).matchUserStart(RoomType_MatchUserParty);
        assertTrue(matchUserStartResult1.isSuccess());

        MatchUserStartResult matchUserStartResult2 = users.get(3).matchUserStart(RoomType_MatchUserParty);
        assertTrue(matchUserStartResult2.isSuccess());

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
