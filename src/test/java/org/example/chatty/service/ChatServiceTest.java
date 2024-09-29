package org.example.chatty.service;

import org.example.chatty.exceptions.MessageDeliveryException;
import org.example.chatty.model.Message;
import org.example.chatty.validators.MessageValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private MessageValidator messageValidator;

    @Mock
    private WebSocketSession mockSession;

    private ConcurrentHashMap<String, WebSocketSession> activeSessions;

    @BeforeEach
    public void setUp() {
        activeSessions = new ConcurrentHashMap<>();
        activeSessions.put("user1", mockSession);
    }

    @Test
    public void shouldBroadcastMessageToAllUsers() throws IOException {
        Message message = new Message("user1", null, "Hello, everyone!", LocalDateTime.now());

        when(mockSession.isOpen()).thenReturn(true);
        chatService.addUser("user1", mockSession);
        chatService.broadcastMessage(message);

        verify(mockSession, times(1)).sendMessage(new TextMessage(message.toString()));
    }

    @Test
    public void shouldSendPrivateMessageSuccessfully() throws IOException {
        Message message = new Message("user1", "user2", "Hello, user2!", LocalDateTime.now());

        when(mockSession.isOpen()).thenReturn(true);
        chatService.addUser("user2", mockSession);
        doNothing().when(messageValidator).validateReceiverSession(mockSession, "user2");
        chatService.sendPrivateMessage(message);

        verify(mockSession, times(1)).sendMessage(new TextMessage(message.toString()));
        verify(messageValidator, times(1)).validateReceiverSession(mockSession, "user2");
    }

    @Test
    public void shouldThrowExceptionWhenSessionIsClosedForPrivateMessage() {
        Message message = new Message("user1", "user2", "Hello, user2!", LocalDateTime.now());

        when(mockSession.isOpen()).thenReturn(false);
        chatService.addUser("user2", mockSession);

        MessageDeliveryException exception = assertThrows(
                MessageDeliveryException.class,
                () -> chatService.sendPrivateMessage(message)
        );

        assertEquals("Receiver with ID user2 has a closed session.", exception.getMessage());
    }


    @Test
    public void shouldAddUserToActiveSessions() {
        chatService.addUser("user2", mockSession);

        assertTrue(chatService.getActiveUsers().contains("user2"));
    }

    @Test
    public void shouldRemoveUserFromActiveSessions() {
        chatService.addUser("user2", mockSession);
        chatService.removeUser("user2");

        assertFalse(chatService.getActiveUsers().contains("user2"));
    }

    @Test
    public void shouldThrowExceptionWhenReceiverNotFound() {
        Message message = new Message("user1", "user2", "Hello, user2!", LocalDateTime.now());

        chatService.removeUser("user2");

        MessageDeliveryException exception = assertThrows(
                MessageDeliveryException.class,
                () -> chatService.sendPrivateMessage(message)
        );

        assertEquals("Receiver with ID user2 not found.", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMessageSendingFails() throws IOException {
        Message message = new Message("user1", "user2", "Hello, user2!", LocalDateTime.now());

        when(mockSession.isOpen()).thenReturn(true);
        chatService.addUser("user2", mockSession);
        doNothing().when(messageValidator).validateReceiverSession(mockSession, "user2");
        doThrow(new IOException("Failed to send message")).when(mockSession).sendMessage(any(TextMessage.class));

        MessageDeliveryException exception = assertThrows(
                MessageDeliveryException.class,
                () -> chatService.sendPrivateMessage(message)
        );

        assertEquals("Failed to send private message to user with ID: user2", exception.getMessage());
    }

}