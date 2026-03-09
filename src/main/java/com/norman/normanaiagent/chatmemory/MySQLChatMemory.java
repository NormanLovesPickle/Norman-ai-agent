package com.norman.normanaiagent.chatmemory;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "spring.datasource.mysql", name = "url")
public class MySQLChatMemory implements ChatMemory {

    private final JdbcTemplate jdbcTemplate;
    private final JSONConfig jsonConfig;

    public MySQLChatMemory(@Qualifier("mysqlJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonConfig = new JSONConfig().setIgnoreNullValue(true);
        log.info("初始化MySQL对话记忆，使用限定的MySQL数据源");
    }

    @Override
    @Transactional
    public void add(String conversationId, Message message) {
        if (message != null && conversationId != null) {
            add(conversationId, Collections.singletonList(message));
        }
    }

    @Override
    @Transactional
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty() || conversationId == null) {
            return;
        }
        Integer maxOrder = getMaxOrder(conversationId).orElse(0);
        int nextOrder = maxOrder + 1;

        String insertSql = "INSERT INTO chatmemory (conversation_id, message_order, message_type, content, message_json, create_time, update_time, is_delete) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        log.info("添加消息到会话 {}, 消息数量: {}", conversationId, messages.size());

        jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Message message = messages.get(i);
                int order = nextOrder + i;
                String messageJson = serializeMessage(message);
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                ps.setString(1, conversationId);
                ps.setInt(2, order);
                ps.setString(3, message.getMessageType().toString());
                ps.setString(4, message.getText());
                ps.setString(5, messageJson);
                ps.setTimestamp(6, now);
                ps.setTimestamp(7, now);
                ps.setBoolean(8, false);
            }

            @Override
            public int getBatchSize() {
                return messages.size();
            }
        });
    }

    @Override
    public List<Message> get(String conversationId) {
        List<Message> messages = getMessages(conversationId, 0);
        log.debug("从会话 {} 中检索到 {} 条消息", conversationId, messages.size());
        return messages;
    }

    private List<Message> getMessages(String conversationId, int lastN) {
        String sql;
        Object[] params;
        if (lastN > 0) {
            sql = "SELECT message_json, message_type, content FROM chatmemory WHERE conversation_id = ? AND is_delete = 0 ORDER BY message_order DESC LIMIT ?";
            params = new Object[]{conversationId, lastN};
        } else {
            sql = "SELECT message_json, message_type, content FROM chatmemory WHERE conversation_id = ? AND is_delete = 0 ORDER BY message_order DESC";
            params = new Object[]{conversationId};
        }
        return executeMessageQuery(sql, params);
    }

    @Override
    @Transactional
    public void clear(String conversationId) {
        String sql = "UPDATE chatmemory SET is_delete = 1, update_time = ? WHERE conversation_id = ? AND is_delete = 0";
        int count = jdbcTemplate.update(sql, Timestamp.valueOf(LocalDateTime.now()), conversationId);
        log.info("从会话 {} 中逻辑删除 {} 条消息", conversationId, count);
    }

    private Optional<Integer> getMaxOrder(String conversationId) {
        String sql = "SELECT MAX(message_order) FROM chatmemory WHERE conversation_id = ? AND is_delete = 0";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, conversationId);
        return Optional.ofNullable(result);
    }

    private String serializeMessage(Message message) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", message.getMessageType().toString());
        map.put("text", message.getText());
        if (message instanceof UserMessage) {
            map.put("messageClass", "UserMessage");
        } else if (message instanceof AssistantMessage) {
            map.put("messageClass", "AssistantMessage");
        } else if (message instanceof SystemMessage) {
            map.put("messageClass", "SystemMessage");
        } else {
            map.put("messageClass", "OtherMessage");
        }
        return JSONUtil.toJsonStr(map, jsonConfig);
    }

    private Message deserializeMessage(String messageType, String content) {
        switch (messageType) {
            case "USER":
                return new UserMessage(content);
            case "ASSISTANT":
                return new AssistantMessage(content);
            case "SYSTEM":
                return new SystemMessage(content);
            default:
                log.warn("未知的消息类型: {}", messageType);
                return new AssistantMessage("未知消息类型: " + content);
        }
    }

    private List<Message> executeMessageQuery(String sql, Object[] params) {
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            String messageType = rs.getString("message_type");
            String content = rs.getString("content");
            return deserializeMessage(messageType, content);
        }).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
