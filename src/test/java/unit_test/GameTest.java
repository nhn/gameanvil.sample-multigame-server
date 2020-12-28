package unit_test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.nhn.gameanvil.gamehammer.tester.Connection;
import com.nhn.gameanvil.gamehammer.tester.PacketResult;
import com.nhn.gameanvil.gamehammer.tester.RemoteInfo;
import com.nhn.gameanvil.gamehammer.tester.ResultAuthentication;
import com.nhn.gameanvil.gamehammer.tester.ResultConnect;
import com.nhn.gameanvil.gamehammer.tester.ResultLeaveRoom;
import com.nhn.gameanvil.gamehammer.tester.ResultLogin;
import com.nhn.gameanvil.gamehammer.tester.ResultMatchPartyCancel;
import com.nhn.gameanvil.gamehammer.tester.ResultMatchPartyCancel.ResultCodeMatchPartyCancel;
import com.nhn.gameanvil.gamehammer.tester.ResultMatchPartyStart;
import com.nhn.gameanvil.gamehammer.tester.ResultMatchPartyStart.ResultCodeMatchPartyStart;
import com.nhn.gameanvil.gamehammer.tester.ResultMatchRoom;
import com.nhn.gameanvil.gamehammer.tester.ResultMatchRoom.ResultCodeMatchRoom;
import com.nhn.gameanvil.gamehammer.tester.ResultMatchUserCancel;
import com.nhn.gameanvil.gamehammer.tester.ResultMatchUserDone;
import com.nhn.gameanvil.gamehammer.tester.ResultMatchUserDone.ResultCodeMatchUserDone;
import com.nhn.gameanvil.gamehammer.tester.ResultMatchUserStart;
import com.nhn.gameanvil.gamehammer.tester.ResultMatchUserTimeout;
import com.nhn.gameanvil.gamehammer.tester.ResultNamedRoom;
import com.nhn.gameanvil.gamehammer.tester.ResultNamedRoom.ResultCodeNamedRoom;
import com.nhn.gameanvil.gamehammer.tester.Tester;
import com.nhn.gameanvil.gamehammer.tester.User;
import com.nhn.gameanvil.gamehammer.tool.UuidGenerator;
import com.nhn.gameanvil.sample.Defines.StringValues;
import com.nhn.gameanvil.sample.protocol.Sample;
import com.nhn.gameanvilcore.util.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GameTest {
    static final String[] ChannelId = {"1", "2", "3", "4"};

    static final int Timeout = 3000;

    private static Tester tester;
    private List<User> users = new ArrayList<>();

    @BeforeClass
    public static void configuration() {

        tester = Tester.newBuilder()
            .addRemoteInfo("127.0.0.1", 11200) // 테스트 하려는 서버의 IP 와 Port 를 지정합니다.
            .setDefaultPacketTimeoutSeconds(3)
            .addProtoBufClass(0, Sample.getDescriptor())// 컨텐츠 프로토콜 등록.
            .addServiceInfo(1, StringValues.ChatServiceName, ChannelId)// 컨텐츠 서비스 등록.
            .addServiceInfo(2, StringValues.GameServiceName, ChannelId)// 컨텐츠 서비스 등록.
            .Build();
    }

    @Before
    public void setUp() throws TimeoutException, ExecutionException, InterruptedException {
        UuidGenerator uuidActor = new UuidGenerator("AccountId");
        UuidGenerator uuidDevice = new UuidGenerator("DeviceId");
        for (int i = 0; i < 4; ++i) {

            // 커넥션을 생성하고 세션 정보가 담긴 객체를 리턴.
            Connection connection = tester.createConnection(i);
            ResultConnect resultConnect = connection.connect(new RemoteInfo("127.0.0.1", 11200)).get(Timeout, TimeUnit.MILLISECONDS);
            assertTrue(resultConnect.isSuccess());

            // 인증을 진행.
            String accountId = uuidActor.generateUniqueId();
            String deviceId = uuidDevice.generateUniqueId();
            ResultAuthentication result = connection.authentication(accountId, accountId, deviceId).get(Timeout, TimeUnit.MILLISECONDS);
            assertTrue(result.isSuccess());

            // 세션에 유저를 등록하고, 각종 ID 정보가 담긴 유저 객체를 리턴.
            User user = connection.createUser(StringValues.GameServiceName, 1);

            // 로그인을 진행.
            ResultLogin resultLogin = user.login(StringValues.GameUserType, ChannelId[i]).get(Timeout, TimeUnit.MILLISECONDS);
            assertTrue(resultLogin.isSuccess());

            // Test 단계에서 활용하도록 준비합니다.
            users.add(user);
        }
    }

    @After
    public void tearDown() throws TimeoutException, ExecutionException, InterruptedException {
        for (User user : users) {
            user.logout().get(Timeout, TimeUnit.MILLISECONDS);
            user.getConnection().close().get(Timeout, TimeUnit.MILLISECONDS);
        }
    }

    private int matchRoom(Collection<User> users) throws TimeoutException, ExecutionException, InterruptedException, IOException {
        int roomId = 0;
        List<User> members = new ArrayList<>();
        for (User user : users) {
            List<Future<PacketResult>> futures = new ArrayList<>(members.size());
            for (User member : members) {
                futures.add(member.waitFor(Sample.GameMessageToC.getDescriptor()));
            }
            ResultMatchRoom resultMatchRoom = user.matchRoom(StringValues.GameRoomType_MatchRoom, true, false).get(Timeout, TimeUnit.MILLISECONDS);
            assertEquals(ResultCodeMatchRoom.MATCH_ROOM_SUCCESS, resultMatchRoom.getResultCode());
            for (Future<PacketResult> future : futures) {
                PacketResult packetResult = future.get(Timeout, TimeUnit.MILLISECONDS);
                Sample.GameMessageToC message = Sample.GameMessageToC.parseFrom(packetResult.getStream());
                assertEquals(user.getUserId() + " is join", message.getMessage());
            }

            if (roomId == 0) {
                roomId = resultMatchRoom.getRoomId();
                assertTrue(resultMatchRoom.getCreated());
            } else {
                assertFalse(resultMatchRoom.getCreated());
                assertEquals(roomId, resultMatchRoom.getRoomId());
            }

            members.add(user);
        }

        return roomId;
    }

    private void checkChatMsg(Collection<User> users, String chatMsg) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        int senderId = 0;
        List<Future<PacketResult>> futures = new ArrayList<>(users.size());
        for (User user : users) {
            if (senderId == 0) {
                senderId = user.getUserId();
                user.send(Sample.GameMessageToS.newBuilder().setMessage(chatMsg).build());
            }
            futures.add(user.waitFor(Sample.GameMessageToC.getDescriptor()));
        }

        for (Future<PacketResult> future : futures) {
            PacketResult packetResult = future.get(Timeout, TimeUnit.MILLISECONDS);
            Sample.GameMessageToC gameMessageToC = Sample.GameMessageToC.parseFrom(packetResult.getStream());
            assertEquals("[" + senderId + "] " + chatMsg, gameMessageToC.getMessage());
        }
    }

    @Test
    public void MatchRoomAndChat() throws TimeoutException, IOException, InterruptedException, ExecutionException {

        List<User> members = users.subList(0, 2);

        // 채팅방 입장
        matchRoom(members);

        checkChatMsg(members, "Hello GameAnvil!");
    }

    @Test
    public void MatchRoomMoveAndChat() throws TimeoutException, IOException, InterruptedException, ExecutionException {

        User account1 = users.get(0);
        User account2 = users.get(1);
        List<User> members = users.subList(0, 2);

        int roomId = matchRoom(members);

        ResultMatchRoom resultMatchRoomMove = account1.matchRoom(StringValues.GameRoomType_MatchRoom, true, true).get(Timeout, TimeUnit.MILLISECONDS);
        assertEquals(ResultCodeMatchRoom.MATCH_ROOM_SUCCESS, resultMatchRoomMove.getResultCode());
        assertNotEquals(roomId, resultMatchRoomMove.getRoomId());

        ResultLeaveRoom resultLeaveRoom = account2.leaveRoom().get(Timeout, TimeUnit.MILLISECONDS);
        assertTrue(resultLeaveRoom.isSuccess());

        Future<PacketResult> future = account1.waitFor(Sample.GameMessageToC.getDescriptor());
        ResultMatchRoom resultMatchRoomJoin = account2.matchRoom(StringValues.GameRoomType_MatchRoom, true, true).get(Timeout, TimeUnit.MILLISECONDS);
        assertTrue(resultMatchRoomJoin.isSuccess());
        assertFalse(resultMatchRoomJoin.getCreated());
        assertEquals(resultMatchRoomMove.getRoomId(), resultMatchRoomJoin.getRoomId());

        PacketResult packetResult = future.get(Timeout, TimeUnit.MILLISECONDS);
        Sample.GameMessageToC account1GetAccount3JoinMsg = Sample.GameMessageToC.parseFrom(packetResult.getStream());
        assertEquals(account2.getUserId() + " is join", account1GetAccount3JoinMsg.getMessage());

        checkChatMsg(members, "Hello GameAnvil!");
    }

    private void matchUser(Collection<User> users) throws TimeoutException, ExecutionException, InterruptedException, IOException {
        List<Future<ResultMatchUserStart>> futures = new ArrayList<>(users.size());
        for (User user : users) {
            futures.add(user.matchUserStart(StringValues.GameRoomType_MatchUser));
        }

        for (Future<ResultMatchUserStart> future : futures) {
            ResultMatchUserStart resultMatchUserStart = future.get(Timeout, TimeUnit.MILLISECONDS);
            assertTrue(resultMatchUserStart.isSuccess());
        }

        Map<String, List<Future<PacketResult>>> joinMsgMap = new HashMap<>();
        for (User sender : users) {
            List<Future<PacketResult>> list = new ArrayList<>(users.size());
            joinMsgMap.put(sender.getUserId() + " is join", list);
            for (User listener : users) {
                if (sender.getUserId() == listener.getUserId()) {
                    continue;
                }
                list.add(listener.waitFor(Sample.GameMessageToC.getDescriptor()));
            }
        }

        List<Future<ResultMatchUserDone>> futuresMatchUserDone = new ArrayList<>(users.size());
        for (User user : users) {
            futuresMatchUserDone.add(user.waitForMatchUserDoneNoti());
        }

        for (Future<ResultMatchUserDone> future : futuresMatchUserDone) {
            future.get(Timeout, TimeUnit.MILLISECONDS);
        }

        for (User sender : users) {
            String expected = sender.getUserId() + " is join";
            List<Future<PacketResult>> list = joinMsgMap.get(expected);
            for (Future<PacketResult> future : list) {
                PacketResult packetResult = future.get(Timeout, TimeUnit.MILLISECONDS);
                Sample.GameMessageToC joinMsg = Sample.GameMessageToC.parseFrom(packetResult.getStream());
                assertEquals(expected, joinMsg.getMessage());
            }
        }
    }

    @Test
    public void MatchUserAndChat() throws TimeoutException, IOException, InterruptedException, ExecutionException {

        List<User> members = users.subList(0, 2);

        matchUser(members);

        checkChatMsg(members, "Hello GameAnvil!");
    }

    @Test
    public void MatchUserAndRefillAndChat() throws TimeoutException, IOException, InterruptedException, ExecutionException {

        User account1 = users.get(0);
        User account2 = users.get(1);
        List<User> members = users.subList(0, 2);

        matchUser(members);

        Future<PacketResult> futureLeaveMsg = account1.waitFor(Sample.GameMessageToC.getDescriptor());
        ResultLeaveRoom resultLeaveRoom = account2.leaveRoom().get(Timeout, TimeUnit.MILLISECONDS);
        assertTrue(resultLeaveRoom.isSuccess());

        PacketResult packetResult1 = futureLeaveMsg.get(Timeout, TimeUnit.MILLISECONDS);
        Sample.GameMessageToC leaveMsg = Sample.GameMessageToC.parseFrom(packetResult1.getStream());
        assertEquals(account2.getUserId() + " is leave", leaveMsg.getMessage());

        Thread.sleep(500);

        ResultMatchUserStart resultMatchUserStart = account2.matchUserStart(StringValues.GameRoomType_MatchUser).get(Timeout, TimeUnit.MILLISECONDS);
        assertTrue(resultMatchUserStart.isSuccess());

        Future<PacketResult> futureJoinMsg = account1.waitFor(Sample.GameMessageToC.getDescriptor());
        ResultMatchUserDone resultMatchUserDone = account2.waitForMatchUserDoneNoti().get(Timeout, TimeUnit.MILLISECONDS);
        assertEquals(ResultCodeMatchUserDone.MATCH_USER_DONE_SUCCESS, resultMatchUserDone.getResultCode());

        PacketResult packetResult2 = futureJoinMsg.get(Timeout, TimeUnit.MILLISECONDS);
        Sample.GameMessageToC joinMsg = Sample.GameMessageToC.parseFrom(packetResult2.getStream());
        assertEquals(account2.getUserId() + " is join", joinMsg.getMessage());

        checkChatMsg(members, "Hello GameAnvil!");
    }

    @Test
    public void MatchUserAndCancel() throws TimeoutException, IOException, InterruptedException, ExecutionException {

        User account1 = users.get(0);
        User account2 = users.get(1);

        // 채팅방 입장
        ResultMatchUserStart resultMatchUserStart1 = account1.matchUserStart(StringValues.GameRoomType_MatchUser).get(Timeout, TimeUnit.MILLISECONDS);
        assertTrue(resultMatchUserStart1.isSuccess());

        ResultMatchUserStart resultMatchUserStart2 = account2.matchUserStart(StringValues.GameRoomType_MatchUser).get(Timeout, TimeUnit.MILLISECONDS);
        assertTrue(resultMatchUserStart2.isSuccess());

        ResultMatchUserCancel resultMatchUserCancel = account2.matchUserCancel(StringValues.GameRoomType_MatchUser).get(Timeout, TimeUnit.MILLISECONDS);
        assertTrue(resultMatchUserCancel.isSuccess());

        ResultMatchUserTimeout matchUserTimeout = account1.waitForMatchUserTimeoutNoti().get(Timeout, TimeUnit.SECONDS);
        assertTrue(matchUserTimeout.isSuccess());
    }

    private void makePartyRoom(Collection<User> users, String roomName) throws TimeoutException, ExecutionException, InterruptedException, IOException {
        List<User> members = new ArrayList<>();
        for (User user : users) {
            List<Future<PacketResult>> futures = new ArrayList<>(members.size());
            if (members.size() > 0) {
                for (User member : members) {
                    futures.add(member.waitFor(Sample.GameMessageToC.getDescriptor()));
                }
            }

            ResultNamedRoom resultNamedRoom = user.namedRoom(StringValues.PartyRoomType, roomName, true).get(Timeout, TimeUnit.MILLISECONDS);
            assertEquals(ResultCodeNamedRoom.NAMED_ROOM_SUCCESS, resultNamedRoom.getResultCode());
            System.out.println("NAMED_ROOM_SUCCESS : " + user.getUserId());

            for (Future<PacketResult> future : futures) {
                PacketResult packetResult = future.get(Timeout, TimeUnit.MILLISECONDS);
                Sample.GameMessageToC message = Sample.GameMessageToC.parseFrom(packetResult.getStream());
                assertEquals(user.getUserId() + " is join", message.getMessage());
            }

            members.add(user);
        }
    }

    private void startMatchParty(Collection<User> users) throws TimeoutException, ExecutionException, InterruptedException {
        boolean first = true;
        Future<ResultMatchPartyStart> futureStart = null;
        List<Future<ResultMatchPartyStart>> futures = new ArrayList<>(users.size());
        for (User user : users) {
            if (first) {
                futureStart = user.matchPartyStart(StringValues.GameRoomType_MatchParty);
                first = false;
            } else {
                futures.add(user.waitForMatchPartyStartNoti());
            }
        }

        assertNotNull(futureStart);
        ResultMatchPartyStart resultMatchPartyStart = futureStart.get(Timeout, TimeUnit.MILLISECONDS);
        assertEquals(ResultCodeMatchPartyStart.MATCH_PARTY_START_SUCCESS, resultMatchPartyStart.getResultCode());
        for (Future<ResultMatchPartyStart> future : futures) {
            ResultMatchPartyStart resultMatchPartyStartNoti = future.get(Timeout, TimeUnit.MILLISECONDS);
            assertEquals(ResultCodeMatchPartyStart.MATCH_PARTY_START_SUCCESS, resultMatchPartyStartNoti.getResultCode());
        }
    }

    private void cancelMatchParty(Collection<User> users) throws TimeoutException, ExecutionException, InterruptedException {
        boolean first = true;
        List<Future<ResultMatchPartyCancel>> futures = new ArrayList<>(users.size());
        for (User user : users) {
            if (first) {
                futures.add(user.matchPartyCancel(StringValues.GameRoomType_MatchParty));
                first = false;
            } else {
                futures.add(user.waitForMatchPartyCancelNoti());
            }
        }

        for (Future<ResultMatchPartyCancel> future : futures) {
            ResultMatchPartyCancel resultMatchPartyCancel = future.get(Timeout, TimeUnit.MILLISECONDS);
            assertEquals(ResultCodeMatchPartyCancel.MATCH_PARTY_CANCEL_SUCCESS, resultMatchPartyCancel.getResultCode());
        }
    }

    private void checkMatchUserDone(Collection<User> users) throws TimeoutException, ExecutionException, InterruptedException, IOException {
        Map<User, Future<ResultMatchUserDone>> futuresMatchUserDone = new HashMap<>(users.size());
        Map<User, List<Pair<String, Future<PacketResult>>>> futuresJoinMsg = new HashMap<>(users.size());
        for (User user : users) {
            futuresMatchUserDone.put(user, user.waitForMatchUserDoneNoti());
        }

        List<User> sorted = new ArrayList<>(users);
        sorted.sort((a, b) -> Integer.compare(a.getUserId(), b.getUserId()));

        for (User user : sorted) {
            List<Pair<String, Future<PacketResult>>> list = new ArrayList<>();
            futuresJoinMsg.put(user, list);
            for (User join : sorted) {
                if (join.getUserId() == user.getUserId()) {
                    continue;
                }
                String expected = join.getUserId() + " is join";
                list.add(new Pair<>(expected, user.waitFor(Sample.GameMessageToC.getDescriptor())));
            }
        }

        for (User user : users) {
            ResultMatchUserDone matchUserDone = futuresMatchUserDone.get(user).get(Timeout, TimeUnit.MILLISECONDS);
            assertEquals(ResultCodeMatchUserDone.MATCH_USER_DONE_SUCCESS, matchUserDone.getResultCode());
            for (Pair<String, Future<PacketResult>> pair : futuresJoinMsg.get(user)) {
                PacketResult packetResult = pair.snd.get(Timeout, TimeUnit.MILLISECONDS);
                Sample.GameMessageToC joinMsg = Sample.GameMessageToC.parseFrom(packetResult.getStream());
                assertEquals(pair.fst, joinMsg.getMessage());
            }
        }
    }

    private void checkMatchUserTimeout(Collection<User> users) throws InterruptedException, ExecutionException, TimeoutException {
        List<Future<ResultMatchUserTimeout>> futures = new ArrayList<>(users.size());
        for (User user : users) {
            futures.add(user.waitForMatchUserTimeoutNoti());
        }

        for (Future<ResultMatchUserTimeout> future : futures) {
            future.get(Timeout, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    public void MatchParty() throws TimeoutException, IOException, InterruptedException, ExecutionException {

        List<User> party1 = users.subList(0, 2);
        List<User> party2 = users.subList(2, 4);

        // PartyRoom1 입장
        makePartyRoom(party1, "PartyRoom1");
        // PartyRoom2 입장
        makePartyRoom(party2, "PartyRoom2");

        // StartMatchParty1
        startMatchParty(party1);
        // StartMatchParty2
        startMatchParty(party2);

        checkMatchUserDone(users);
    }

    @Test
    public void MatchPartyAndIndividuals() throws TimeoutException, IOException, InterruptedException, ExecutionException {

        List<User> party = users.subList(0, 2);
        List<User> individuals = users.subList(2, 4);

        // PartyRoom 입장
        makePartyRoom(party, "PartyRoom");

        // StartMatchParty
        startMatchParty(party);

        ResultMatchUserStart resultMatchUserStart1 = users.get(2).matchUserStart(StringValues.GameRoomType_MatchParty).get(Timeout, TimeUnit.MILLISECONDS);
        assertTrue(resultMatchUserStart1.isSuccess());

        ResultMatchUserStart resultMatchUserStart2 = users.get(3).matchUserStart(StringValues.GameRoomType_MatchParty).get(Timeout, TimeUnit.MILLISECONDS);
        ;
        assertTrue(resultMatchUserStart2.isSuccess());

        checkMatchUserDone(users);
    }

    @Test
    public void MatchPartyAndRefill() throws TimeoutException, IOException, InterruptedException, ExecutionException {

        List<User> party1 = users.subList(0, 2);
        List<User> party2 = users.subList(2, 4);

        // PartyRoom1 입장
        makePartyRoom(party1, "PartyRoom1");
        // PartyRoom2 입장
        makePartyRoom(party2, "PartyRoom2");

        startMatchParty(party1);
        startMatchParty(party2);

        checkMatchUserDone(users);

        List<Future<PacketResult>> futures = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            futures.add(users.get(i).waitFor(Sample.GameMessageToC.getDescriptor()));
        }
        User account3 = users.get(3);
        ResultLeaveRoom resultLeaveRoom = account3.leaveRoom().get(Timeout, TimeUnit.MILLISECONDS);
        assertTrue(resultLeaveRoom.isSuccess());

        for (Future<PacketResult> future : futures) {
            PacketResult packetResult = future.get(Timeout, TimeUnit.MILLISECONDS);
            Sample.GameMessageToC joinMsg = Sample.GameMessageToC.parseFrom(packetResult.getStream());
            assertEquals(account3.getUserId() + " is leave", joinMsg.getMessage());
        }

        futures.clear();
        for (int i = 0; i < 3; i++) {
            futures.add(users.get(i).waitFor(Sample.GameMessageToC.getDescriptor()));
        }

        ResultMatchUserStart resultMatchUserStart = account3.matchUserStart(StringValues.GameRoomType_MatchParty).get(Timeout, TimeUnit.MILLISECONDS);
        assertTrue(resultMatchUserStart.isSuccess());

        ResultMatchUserDone matchUserDone = account3.waitForMatchUserDoneNoti().get(Timeout, TimeUnit.MILLISECONDS);
        assertEquals(ResultCodeMatchUserDone.MATCH_USER_DONE_SUCCESS, matchUserDone.getResultCode());

        for (Future<PacketResult> future : futures) {
            PacketResult packetResult = future.get(Timeout, TimeUnit.MILLISECONDS);
            Sample.GameMessageToC joinMsg = Sample.GameMessageToC.parseFrom(packetResult.getStream());
            assertEquals(account3.getUserId() + " is join", joinMsg.getMessage());
        }

        checkChatMsg(users, "Hello GameAnvil!");
    }

    @Test
    public void MatchPartyCancel() throws TimeoutException, IOException, InterruptedException, ExecutionException {

        List<User> party1 = users.subList(0, 2);
        List<User> party2 = users.subList(2, 4);

        // PartyRoom1 입장
        makePartyRoom(party1, "PartyRoom1");
        // PartyRoom2 입장
        makePartyRoom(party2, "PartyRoom2");

        // StartMatchParty1
        startMatchParty(party1);
        // StartMatchParty2
        startMatchParty(party2);

        List<Future<ResultMatchUserTimeout>> futures = new ArrayList<>(party2.size());
        for (User user : party2) {
            futures.add(user.waitForMatchUserTimeoutNoti());
        }

        cancelMatchParty(party1);

        for (Future<ResultMatchUserTimeout> future : futures) {
            future.get(20000, TimeUnit.MILLISECONDS);
        }
    }


    @Test
    public void MessageFromSpot() throws TimeoutException, IOException, InterruptedException, ExecutionException {

        User account1 = users.get(0);
        User account3 = users.get(2);
        List<User> members1 = users.subList(0, 2);
        List<User> members2 = users.subList(2, 4);

        matchRoom(members1);
        matchUser(members2);

        List<Future<PacketResult>> futuresHello = new ArrayList<>(users.size());
        List<Future<PacketResult>> futuresNext = new ArrayList<>(users.size());
        for (User user : users) {
            futuresHello.add(user.waitFor(Sample.SampleToC.getDescriptor()));
            futuresNext.add(user.waitFor(Sample.SampleToC.getDescriptor()));
        }

        int restCount = 5;
        account1.send(Sample.ResetSpot.newBuilder().setCount(restCount).build());
        for (int i = 0; i < 3; i++) {
            checkChatMsg(members1, "Hello GameAnvil!");
        }
        for (int i = 0; i < 2; i++) {
            checkChatMsg(members2, "Hello GameAnvil!");
        }

        for (int i = 0; i < users.size(); i++) {
            PacketResult packetResult = futuresHello.get(i).get(Timeout, TimeUnit.MILLISECONDS);
            Sample.SampleToC sampleToC = Sample.SampleToC.parseFrom(packetResult.getStream());
            assertEquals(String.format("[Event : %s] Hello GameAnvil! ", account3.getUserId()), sampleToC.getMessage());
            packetResult = futuresNext.get(i).get(Timeout, TimeUnit.MILLISECONDS);
            sampleToC = Sample.SampleToC.parseFrom(packetResult.getStream());
            assertEquals(String.format("[Event] Next Event will be on count %s ", restCount), sampleToC.getMessage());
        }
    }
}

