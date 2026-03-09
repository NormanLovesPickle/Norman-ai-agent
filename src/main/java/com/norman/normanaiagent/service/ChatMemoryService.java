package com.norman.normanaiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.norman.normanaiagent.domain.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

public interface ChatMemoryService extends IService<ChatMemory> {

    void addMessages(String conversationId, List<Message> messages);

    List<Message> getMessages(String conversationId, int lastN);

    void clearMessages(String conversationId);
}
