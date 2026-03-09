package com.norman.normanaiagent.controller;

import com.norman.normanaiagent.agent.NormanHealthAssistant;
import com.norman.normanaiagent.agent.NormanManus;
import com.norman.normanaiagent.agent.QuizAssistant;
import com.norman.normanaiagent.app.CareerMentorApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private CareerMentorApp careerMentorApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @GetMapping("/career_mentor/chat/sync")
    public String doChatWithCareerMentorSync(String message, String chatId) {
        return careerMentorApp.doChat(message, chatId);
    }

    @GetMapping(value = "/career_mentor/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithCareerMentorSSE(String message, String chatId) {
        return careerMentorApp.doChatByStream(message, chatId);
    }

    @GetMapping(value = "/career_mentor/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithCareerMentorServerSentEvent(String message, String chatId) {
        return careerMentorApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @GetMapping(value = "/career_mentor/chat/sse_emitter")
    public SseEmitter doChatWithCareerMentorServerSseEmitter(String message, String chatId) {
        SseEmitter sseEmitter = new SseEmitter(180000L);
        careerMentorApp.doChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);
        // 返回
        return sseEmitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        NormanManus normanManus = new NormanManus(allTools, dashscopeChatModel);
        return normanManus.runStream(message);
    }

    @GetMapping("/quiz/chat")
    public SseEmitter doChatWithQuiz(String message) {
        QuizAssistant quizAssistant = new QuizAssistant(allTools, dashscopeChatModel);
        return quizAssistant.runStream(message);
    }

    @GetMapping("/health/chat")
    public SseEmitter doChatWithHealthAssistant(String message) {
        NormanHealthAssistant healthAssistant = new NormanHealthAssistant(allTools, dashscopeChatModel);
        return healthAssistant.runStream(message);
    }
}
