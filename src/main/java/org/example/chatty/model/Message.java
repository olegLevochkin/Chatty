package org.example.chatty.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp;
}