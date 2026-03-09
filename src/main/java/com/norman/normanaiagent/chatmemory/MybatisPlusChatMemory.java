package com.norman.normanaiagent.chatmemory;

import com.norman.normanaiagent.service.ChatMemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@ConditionalOnBean(ChatMemoryService.class)
@org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(org.springframework.ai.chat.memory.ChatMemory.class)
@Component
public class MybatisPlusChatMemory implements ChatMemory {

    private final ChatMemoryService chatMemoryService;

    public MybatisPlusChatMemory(ChatMemoryService chatMemoryService) {
        this.chatMemoryService = chatMemoryService;
        log.info("初始化Mybatis-Plus对话记忆");
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        chatMemoryService.addMessages(conversationId, messages);
    }

    @Override
    public List<Message> get(String conversationId) {
        return chatMemoryService.getMessages(conversationId, 0);
    }

    @Override
    public void clear(String conversationId) {
        chatMemoryService.clearMessages(conversationId);
    }
}
