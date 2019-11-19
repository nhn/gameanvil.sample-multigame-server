package com.nhnent.tardis.sample.space.chat.user;

import co.paralleluniverse.fibers.SuspendExecution;
import com.nhnent.tardis.common.Exceptions.NodeNotFoundException;
import com.nhnent.tardis.common.Packet;
import com.nhnent.tardis.common.Payload;
import com.nhnent.tardis.common.internal.ITimerHandler;
import com.nhnent.tardis.common.internal.ITimerObject;
import com.nhnent.tardis.common.internal.PauseType;
import com.nhnent.tardis.common.serializer.KryoSerializer;
import com.nhnent.tardis.console.PacketDispatcher;
import com.nhnent.tardis.console.TardisIndexer;
import com.nhnent.tardis.console.space.IUser;
import com.nhnent.tardis.console.space.MatchCancelReason;
import com.nhnent.tardis.console.space.MatchRoomResult;
import com.nhnent.tardis.console.space.RoomMode;
import com.nhnent.tardis.console.space.UserAgent;
import com.nhnent.tardis.sample.Defines.StringValues;
import com.nhnent.tardis.sample.protocol.Sample;
import com.nhnent.tardis.sample.space.chat.match.ChatRoomMatchInfo;
import com.nhnent.tardis.sample.space.chat.match.ChatUserMatchInfo;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.ByteBuffer;

public class ChatUser extends UserAgent implements IUser, ITimerHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String nickName = "";

    static private PacketDispatcher<ChatUser> packetDispatcher = new PacketDispatcher();

    static{
        packetDispatcher.registerMsg(Sample.RegisterNickNameReq.class,CmdRegisterNickNameReq.class);
    }


    @Override
    public boolean onLogin(Payload payload, Payload payload1, Payload payload2) throws SuspendExecution {
        logger.info("ChatUser.onLogin - UserId : {}", getUserId());
        addClientTopics(Arrays.asList(StringValues.ChatServiceName));
        return true;
    }

    @Override
    public void onPostLogin() throws SuspendExecution {
        logger.info("ChatUser.onPostLogin - UserId : {}", getUserId());
    }

    @Override
    public boolean onReLogin(Payload payload, Payload payload1, Payload payload2) throws SuspendExecution {
        logger.info("ChatUser.onReLogin - UserId : {}", getUserId());
        return true;
    }

    @Override
    public void onDisconnect() throws SuspendExecution {
        logger.info("ChatUser.onDisconnect - UserId : {}", getUserId());

//        try {
//            String message = "["+getNickName()+"] is disconnected.";
//            this.sendToRoom(getServiceId(), getRoomId(), new Packet(Sample.ChatMessageToS.newBuilder().setMessage(message)));
//        } catch (NodeNotFoundException e) {
//            e.printStackTrace();
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onDispatch(Packet packet) throws SuspendExecution {
        logger.info("ChatUser.onDispatch : {} , {}",
            TardisIndexer.getMsgName(packet.getDescId(), packet.getMsgIndex()), getUserId());
        packetDispatcher.dispatch(this,packet);
    }

    @Override
    public void onPause(PauseType pauseType) throws SuspendExecution {
        logger.info("ChatUser.onPause - UserId : {}", getUserId());
    }

    @Override
    public void onResume() throws SuspendExecution {
        logger.info("ChatUser.onResume - UserId : {}", getUserId());
    }

    @Override
    public void onLogout(Payload payload, Payload outPayload) throws SuspendExecution {
        logger.info("ChatUser.onLogout - UserId : {}", getUserId());
    }

    @Override
    public boolean canLogout() throws SuspendExecution {
        logger.info("ChatUser.canLogout - UserId : {}", getUserId());
        return true;
    }

    /**
     * client 에서 MatchRoom 을 요청했을 경우 발생하는 callback
     * @param roomType : 매칭되는 room 의 type
     * @param payload : client 의 요청시 추가적으로 전달되는 data
     * @return  matching 된 room 의 정보 , null 을 반환 할시  client 요청 옵션에 따라서 새로운 방이 생성되거나,요청 실패 처리 된다.
     * @throws SuspendExecution
     */
    @Override
    public MatchRoomResult onMatchRoom(final String roomType, final Payload payload) throws SuspendExecution {
        logger.info("ChatUser.onMatchRoom - UserId : {}, RoomIdBeforeMove : {}", getUserId(), getRoomIdBeforeMove());
        try {

            ChatRoomMatchInfo terms = new ChatRoomMatchInfo();
            // moveRoom 옵션이 true일 경우 room에 참여중인 상태에서도 matchRoom 이 가능하다.
            // 이 경우 지금 참여중인 room에서 나간 후 다시 매칭 프로세스를 거치게 되며
            // 참여 가능한 room 목록을 요청할 때 방금 나온 room도 목록에 포함된다.
            // 따라서 이동하기 전 방금 나온 room 을 구분하기 위해 getRoomIdBeforeMove()로 roomId를 얻어와 terms 에 넣어준다.
            // 원래 있던 room에 다시 join 하는것을 허용하거나, moveRoom 옵션이 false 일 경우에는 하지 않아도 된다.
            terms.setRoomId(getRoomIdBeforeMove());
            String matchingGroup = getServiceName();
            return matchRoom(matchingGroup, roomType, terms);

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    /**
     * client 에서 MatchUserStart 를 요청했을 경우 호출되는 callback
     * server에서는 UserMatchInfo를 저장하고 주기적으로 UserMatchMaker의 match() 함수를 호출함
     * UserMatchMaker의 match()함수에서는 getMatchRequests()를 호출하여 저장된 UserMatchInfo 목록을 가져오고
     * 이 목록중에 조건에 맞는 UserMatchInfo를 찾아 매칭를 완료하게 됨.
     * 여기에서 성공은 매칭의 성공 여부가 아닌 매칭 요청의 성공여부를 의미함.
     * @param roomType : 매칭되는 room 의 type
     * @param payload : client 의 요청시 추가적으로 전달되는 data
     * @param outPayload : 서버에서 client 로 전달되는 data
     * @return : true: user matching 요청 성공,false: user matching 요청 실패
     * @throws SuspendExecution
     */
    public boolean onMatchUser(final String roomType, final Payload payload, Payload outPayload) throws SuspendExecution {
        logger.info("ChatUser.onMatchUser - UserId : {}", getUserId());
        try {

            String matchingGroup = getServiceName();
            ChatUserMatchInfo term = new ChatUserMatchInfo(getUserId(), 100);
            return matchUser(matchingGroup, roomType, term, payload);

        } catch (TimeoutException e) {
            return false;

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return false;
    }

    public boolean onMatchUserCancel(final MatchCancelReason reason) throws SuspendExecution {
        logger.info("ChatUser.onMatchUserCancel - UserId : {}", getUserId());
        return false;
    }

    @Override
    public boolean canTransfer() throws SuspendExecution {
        logger.info("ChatUser.canTransfer - UserId : {}", getUserId());
        return false;
    }

    @Override
    public void onTimer(ITimerObject iTimerObject, Object o) throws SuspendExecution {
        logger.info("ChatUser.onTimer : {}", getUserId());
    }

    @Override
    public ByteBuffer onTransferOut() throws SuspendExecution {
        logger.info("ChatUser.onTransferOut - UserId : {}", getUserId());
        return KryoSerializer.write(nickName);
    }

    @Override
    public void onTransferIn(final InputStream inputStream) throws SuspendExecution {
        logger.info("ChatUser.onTransferIn - UserId : {}", getUserId());
        try {
            nickName = (String) KryoSerializer.read(inputStream);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        if(!nickName.isEmpty()){
            return nickName;
        }
        return getUserId();
    }
}
