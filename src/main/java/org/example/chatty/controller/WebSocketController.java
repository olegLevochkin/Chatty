package org.example.chatty.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.example.chatty.model.Message;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
public class WebSocketController extends TextWebSocketHandler {

    @Getter
    private static final ConcurrentHashMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = session.getId();
        activeSessions.put(userId, session);
        log.info("User connected: {}", userId);

        broadcastMessage("Server", "User " + userId + " has joined the chat.");
        broadcastActiveUsers();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        String senderId = session.getId();
        log.info("Received message from user {}: {}", senderId, textMessage.getPayload());

        Message message = objectMapper.readValue(textMessage.getPayload(), Message.class);

        if (message.getReceiver() != null) {
            sendMessageToUser(message.getReceiver(), message.getSender(), message.getContent());
        } else {
            broadcastMessage(message.getSender(), message.getContent());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) {
        String userId = session.getId();
        activeSessions.remove(userId);
        log.info("User disconnected: {}", userId);

        broadcastMessage("Server", "User " + userId + " has left the chat.");
        broadcastActiveUsers();
    }

    private void broadcastMessage(String sender, String messageContent) {
        TextMessage message = new TextMessage(sender + ": " + messageContent);
        activeSessions.values().forEach(session -> {
            try {
                session.sendMessage(message);
            } catch (Exception e) {
                log.error("Failed to send message to user: {}", session.getId(), e);
            }
        });
    }

    private void sendMessageToUser(String receiverId, String sender, String messageContent) {
        WebSocketSession receiverSession = activeSessions.get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            try {
                receiverSession.sendMessage(new TextMessage("[Private] " + sender + ": " + messageContent));
                log.info("Sent private message from {} to {}: {}", sender, receiverId, messageContent);
            } catch (Exception e) {
                log.error("Failed to send private message to user: {}", receiverId, e);
            }
        } else {
            log.warn("User {} is not available for private messaging.", receiverId);
        }
    }

    private void broadcastActiveUsers() {
        String activeUsersList = String.join(", ", activeSessions.keySet());
        broadcastMessage("Server", "Active users: " + activeUsersList);
    }
}