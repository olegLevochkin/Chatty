package org.example.chatty.service;

import lombok.RequiredArgsConstructor;
import org.example.chatty.exceptions.MessageDeliveryException;
import org.example.chatty.model.Message;
import org.example.chatty.validators.MessageValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConcurrentHashMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    private final MessageValidator messageValidator;

    public Set<String> getActiveUsers() {
        return activeSessions.keySet();
    }

    public void broadcastMessage(Message message) {
        TextMessage textMessage = new TextMessage(message.toString());
        activeSessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    throw new MessageDeliveryException("Failed to broadcast message to user with session ID: " + session.getId());
                }
            }
        });
    }

    public void sendPrivateMessage(Message message) {
        WebSocketSession receiverSession = activeSessions.get(message.getReceiver());

        if (receiverSession == null) {
            throw new MessageDeliveryException("Receiver with ID " + message.getReceiver() + " not found.");
        }

        if (!receiverSession.isOpen()) {
            throw new MessageDeliveryException("Receiver with ID " + message.getReceiver() + " has a closed session.");
        }

        messageValidator.validateReceiverSession(receiverSession, message.getReceiver());

        try {
            TextMessage textMessage = new TextMessage(message.toString());
            receiverSession.sendMessage(textMessage);
        } catch (IOException e) {
            throw new MessageDeliveryException("Failed to send private message to user with ID: " + message.getReceiver());
        }
    }


    public void addUser(String userId, WebSocketSession session) {
        activeSessions.put(userId, session);
    }

    public void removeUser(String userId) {
        activeSessions.remove(userId);
    }
}