package org.example.chatty.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MessageDeliveryException extends RuntimeException {
    private final String message;
}