package org.example.chatty.validators;

import org.example.chatty.exceptions.MessageDeliveryException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class MessageValidator {

    public void validateReceiverSession(WebSocketSession receiverSession, String receiverId) {
        if (receiverSession == null) {
            throw new MessageDeliveryException("Receiver with ID " + receiverId + " not found.");
        }

        if (!receiverSession.isOpen()) {
            throw new MessageDeliveryException("Receiver with ID " + receiverId + " has a closed session.");
        }
    }

}