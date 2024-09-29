package org.example.chatty.validators;

import org.example.chatty.exceptions.MessageDeliveryException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MessageValidatorTest {

    @InjectMocks
    private MessageValidator messageValidator;

    @Mock
    private WebSocketSession mockSession;

    @Test
    public void shouldValidateReceiverSessionSuccessfully() {
        when(mockSession.isOpen()).thenReturn(true);

        assertDoesNotThrow(() -> messageValidator.validateReceiverSession(mockSession, "user1"));
    }

    @Test
    public void shouldThrowExceptionWhenSessionIsNull() {
        MessageDeliveryException exception = assertThrows(
                MessageDeliveryException.class,
                () -> messageValidator.validateReceiverSession(null, "user1")
        );

        assertEquals("Receiver with ID user1 not found.", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenSessionIsClosed() {
        when(mockSession.isOpen()).thenReturn(false);

        MessageDeliveryException exception = assertThrows(
                MessageDeliveryException.class,
                () -> messageValidator.validateReceiverSession(mockSession, "user1")
        );

        assertEquals("Receiver with ID user1 has a closed session.", exception.getMessage());
    }
}