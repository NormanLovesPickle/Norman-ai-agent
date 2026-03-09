package com.norman.normanaiagent.controller;

import com.norman.normanaiagent.domain.request.QwenMultiModalRequest;
import com.norman.normanaiagent.domain.request.QwenTextRequest;
import com.norman.normanaiagent.domain.response.QwenResponse;
import com.norman.normanaiagent.service.QwenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/qwen")
@Tag(name = "通义千问API", description = "阿里云通义千问大模型API")
public class QwenController {

    private final QwenService qwenService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @PostMapping("/chat")
    @Operation(summary = "文本对话", description = "发送文本消息到通义千问进行对话")
    public QwenResponse chat(@RequestBody QwenTextRequest request) {
        return qwenService.chat(request);
    }

    @PostMapping(value = "/stream-chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式文本对话", description = "发送文本消息到通义千问进行流式对话")
    public SseEmitter streamChat(@RequestBody QwenTextRequest request) {
        SseEmitter emitter = new SseEmitter(-1L);
        executorService.execute(() -> {
            try {
                qwenService.streamChat(request)
                        .subscribe(
                                response -> {
                                    try {
                                        emitter.send(response);
                                    } catch (IOException e) {
                                        log.error("发送流式消息出错", e);
                                        emitter.completeWithError(e);
                                    }
                                },
                                error -> {
                                    log.error("流式对话出错", error);
                                    emitter.completeWithError(error);
                                },
                                emitter::complete);
            } catch (Exception e) {
                log.error("处理流式对话请求出错", e);
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    @PostMapping("/image")
    @Operation(summary = "图像对话", description = "发送图像和文本到通义千问进行图像分析对话")
    public QwenResponse imageChat(@RequestBody QwenMultiModalRequest request) {
        request.setType("IMAGE");
        if (request.getModel() == null || request.getModel().isEmpty()) {
            request.setModel("qwen-vl-plus");
        }
        return qwenService.multiModalChat(request);
    }

    @PostMapping("/video")
    @Operation(summary = "视频对话", description = "发送视频URL或视频帧序列和文本到通义千问进行视频分析对话")
    public QwenResponse videoChat(@RequestBody QwenMultiModalRequest request) {
        request.setType("VIDEO");
        if (request.getModel() == null || request.getModel().isEmpty()) {
            request.setModel("qwen-vl-max-latest");
        }
        return qwenService.multiModalChat(request);
    }

    @PostMapping("/audio")
    @Operation(summary = "音频对话", description = "发送音频和文本到通义千问进行音频分析对话")
    public QwenResponse audioChat(@RequestBody QwenMultiModalRequest request) {
        request.setType("AUDIO");
        if (request.getAudioUrl() == null || request.getAudioUrl().isEmpty()) {
            return QwenResponse.builder()
                    .success(false)
                    .statusCode(400)
                    .message("错误：必须提供audioUrl参数")
                    .build();
        }
        if (request.getModel() == null || request.getModel().isEmpty()) {
            request.setModel("qwen2-audio-instruct");
        }
        if (request.getSystemPrompt() == null || request.getSystemPrompt().isEmpty()) {
            request.setSystemPrompt("You are a helpful assistant specialized in audio analysis.");
        }
        return qwenService.multiModalChat(request);
    }
}
