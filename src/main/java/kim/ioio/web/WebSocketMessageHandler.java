package kim.ioio.web;

import kim.ioio.web.constants.ParamKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketMessageHandler extends TextWebSocketHandler {

    private static final Logger logger = LogManager.getLogger(WebSocketMessageHandler.class);

    public static final Map<String, WebSocketSession> sessionUsers = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get(ParamKey.USERID);
        logger.info("afterConnectionEstablished from user ======>" + userId);
        sessionUsers.put(userId, session);

        JSONObject responseUpLineJo = new JSONObject();
        responseUpLineJo.put("messageType", 0);
        responseUpLineJo.put("data", new JSONArray(sessionUsers.keySet()));
        sendMessageToUser(userId, responseUpLineJo.toString());


        //通知全部用户有人上线
        JSONObject responseAllUserOnLineJo = new JSONObject();
        responseAllUserOnLineJo.put("messageType", 1 );
        JSONObject onLineDataJo = new JSONObject();
        onLineDataJo.put("type","1");
        onLineDataJo.put("userId",userId);
        responseAllUserOnLineJo.put("data", onLineDataJo);
        sendMessageToAllUsers(responseAllUserOnLineJo.toString());


        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get(ParamKey.USERID);
        logger.info("afterConnectionClosed from user ======>" + userId);
        sessionUsers.remove(userId);

        //通知全部用户有人上线
        JSONObject responseAllUserOnLineJo = new JSONObject();
        responseAllUserOnLineJo.put("messageType", 1 );
        JSONObject onLineDataJo = new JSONObject();
        onLineDataJo.put("type","0");
        onLineDataJo.put("userId",userId);
        responseAllUserOnLineJo.put("data", onLineDataJo);
        sendMessageToAllUsers(responseAllUserOnLineJo.toString());

        super.afterConnectionClosed(session, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        logger.info("Websocket TextMessage from user ======>" + payload);

        JSONObject messageJo = new JSONObject(payload);
        String sendType = messageJo.getString("sendType");

        if(!StringUtils.isEmpty(sendType) && "toUser".equals(sendType)){
            String toUserId = messageJo.getString("to");
            String fromUserId = messageJo.getString("from");
            String messageContent = messageJo.getString("messageContent");

            JSONObject messagePayLoadJo = new JSONObject();
            messagePayLoadJo.put("messageType",2);
            messagePayLoadJo.put("to",toUserId);
            messagePayLoadJo.put("from",fromUserId);
            messagePayLoadJo.put("messageContent",messageContent);

            sendMessageToUser(toUserId,messagePayLoadJo.toString());
        }
    }

    private void sendMessageToUser(String targetUserId, String textMessage) {
        Iterator iter = sessionUsers.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, WebSocketSession> entry = (Map.Entry) iter.next();
            WebSocketSession session = entry.getValue();
            String userId = entry.getKey();
            if (userId.equals(targetUserId)) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(convertWebSocketTextMessage(textMessage));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public TextMessage convertWebSocketTextMessage(String textMessage) {
        return new TextMessage(textMessage);
    }

    private void sendMessageToAllUsers(String textMessage) {
        Iterator iter = sessionUsers.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, WebSocketSession> entry = (Map.Entry) iter.next();
            WebSocketSession session = entry.getValue();
            if (session.isOpen()) {
                try {
                    session.sendMessage(convertWebSocketTextMessage(textMessage));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
