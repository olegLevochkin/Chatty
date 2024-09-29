package org.example.chatty.controller;

import lombok.AllArgsConstructor;
import org.example.chatty.model.Message;
import org.example.chatty.service.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/chat")
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/users")
    public Set<String> getActiveUsers() {
        return chatService.getActiveUsers();
    }

    @PostMapping("/send")
    public String sendMessage(@RequestBody Message message) {
        Optional<String> receiver = Optional.ofNullable(message.getReceiver());

        receiver.ifPresentOrElse(
                r -> chatService.sendPrivateMessage(message),
                () -> chatService.broadcastMessage(message)
        );

        return "Message sent successfully!";
    }
}