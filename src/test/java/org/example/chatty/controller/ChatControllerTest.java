package org.example.chatty.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.chatty.model.Message;
import org.example.chatty.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldReturnActiveUsers() throws Exception {
        Set<String> activeUsers = Set.of("user1", "user2");
        when(chatService.getActiveUsers()).thenReturn(activeUsers);

        mockMvc.perform(get("/api/chat/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0]").value("user1"))
                .andExpect(jsonPath("$[1]").value("user2"));

        verify(chatService, times(1)).getActiveUsers();
    }

    @Test
    public void shouldSendBroadcastMessage() throws Exception {
        Message message = new Message("user1", null, "Hello, everyone!", LocalDateTime.now());


        doNothing().when(chatService).broadcastMessage(message);

        mockMvc.perform(post("/api/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(content().string("Message sent successfully!"));

        verify(chatService, times(1)).broadcastMessage(message);
    }

    @Test
    public void shouldSendPrivateMessage() throws Exception {
        Message message = new Message("user1", "user2", "Hello, user2!", LocalDateTime.now());

        doNothing().when(chatService).sendPrivateMessage(message);

        mockMvc.perform(post("/api/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(content().string("Message sent successfully!"));

        verify(chatService, times(1)).sendPrivateMessage(message);
    }
}