package com.norman.normanaiagent.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.norman.normanaiagent.config.QwenConfig;
import com.norman.normanaiagent.domain.request.QwenMultiModalRequest;
import com.norman.normanaiagent.domain.request.QwenTextRequest;
import com.norman.normanaiagent.domain.response.QwenResponse;
import com.norman.normanaiagent.service.QwenService;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QwenServiceImpl implements QwenService {

    private final QwenConfig qwenConfig;

    private List<Message> buildMessages(QwenTextRequest request) {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.builder().role(Role.SYSTEM.getValue()).content(request.getSystemPrompt()).build());
        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            for (Map<String, String> h : request.getHistory()) {
                messages.add(Message.builder().role(h.get("role")).content(h.get("content")).build());
            }
        }
        messages.add(Message.builder().role(Role.USER.getValue()).content(request.getContent()).build());
        return messages;
    }

    @Override
    public QwenResponse chat(QwenTextRequest request) {
        try {
            GenerationParam param = GenerationParam.builder()
                    .apiKey(qwenConfig.getApiKey())
                    .model(request.getModel() != null ? request.getModel() : qwenConfig.getTextModel())
                    .messages(buildMessages(request))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();

            GenerationResult result = new Generation().call(param);
            return buildResponse(result, request.getConversationId());
        } catch (Exception e) {
            log.error("调用通义千问文本API出错", e);
            return QwenResponse.builder()
                    .success(false)
                    .statusCode(500)
                    .message("调用通义千问API失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Flowable<QwenResponse> streamChat(QwenTextRequest request) {
        try {
            GenerationParam param = GenerationParam.builder()
                    .apiKey(qwenConfig.getApiKey())
                    .model(request.getModel() != null ? request.getModel() : qwenConfig.getTextModel())
                    .messages(buildMessages(request))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .incrementalOutput(true)
                    .build();

            Flowable<GenerationResult> resultFlowable = new Generation().streamCall(param);
            return resultFlowable.map(result -> buildResponse(result, request.getConversationId()));
        } catch (Exception e) {
            log.error("调用通义千问流式文本API出错", e);
            return Flowable.error(e);
        }
    }

    @Override
    public QwenResponse multiModalChat(QwenMultiModalRequest request) {
        try {
            MultiModalConversation conv = new MultiModalConversation();
            List<Map<String, Object>> contentList = new ArrayList<>();

            if ("IMAGE".equalsIgnoreCase(request.getType())) {
                if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
                    for (String imageUrl : request.getImageUrls()) {
                        contentList.add(Collections.singletonMap("image", imageUrl));
                    }
                } else {
                    return createErrorResponse("IMAGE类型请求必须提供至少一个图像URL");
                }
            } else if ("VIDEO".equalsIgnoreCase(request.getType())) {
                if (request.getVideoUrls() != null && !request.getVideoUrls().isEmpty()) {
                    if (request.getVideoUrls().size() == 1) {
                        contentList.add(Collections.singletonMap("video", request.getVideoUrls().get(0)));
                    } else {
                        contentList.add(Collections.singletonMap("video", request.getVideoUrls()));
                    }
                } else {
                    return createErrorResponse("VIDEO类型请求必须提供至少一个视频URL或帧序列");
                }
            } else if ("AUDIO".equalsIgnoreCase(request.getType())) {
                if (request.getAudioUrl() != null && !request.getAudioUrl().isEmpty()) {
                    contentList.add(Collections.singletonMap("audio", request.getAudioUrl()));
                } else {
                    return createErrorResponse("AUDIO类型请求必须提供音频URL");
                }
            }

            if (request.getTextContent() != null && !request.getTextContent().isEmpty()) {
                contentList.add(Collections.singletonMap("text", request.getTextContent()));
            }

            if (contentList.isEmpty()) {
                return createErrorResponse("没有有效的多模态内容。请提供类型对应的必要内容。");
            }

            MultiModalMessage systemMessage = MultiModalMessage.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(Arrays.asList(Collections.singletonMap("text", request.getSystemPrompt())))
                    .build();
            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(contentList)
                    .build();

            String model;
            if (request.getModel() != null && !request.getModel().isEmpty()) {
                model = request.getModel();
            } else {
                if ("IMAGE".equalsIgnoreCase(request.getType())) {
                    model = qwenConfig.getVisionModel();
                } else if ("VIDEO".equalsIgnoreCase(request.getType())) {
                    model = qwenConfig.getVideoModel();
                } else if ("AUDIO".equalsIgnoreCase(request.getType())) {
                    model = qwenConfig.getAudioModel();
                } else {
                    model = qwenConfig.getTextModel();
                }
            }

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(qwenConfig.getApiKey())
                    .model(model)
                    .message(systemMessage)
                    .message(userMessage)
                    .build();

            MultiModalConversationResult result = conv.call(param);
            return buildMultiModalResponse(result, request.getConversationId());
        } catch (Exception e) {
            log.error("调用通义千问多模态API出错", e);
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("Failed to download multimodal content")) {
                    return analyzeDownloadFailure(errorMessage, request.getType());
                } else if (errorMessage.contains("Invalid backend response")) {
                    return handleInvalidBackendResponse(errorMessage);
                } else if (errorMessage.contains("InvalidParameter")) {
                    return handleInvalidParameterError(errorMessage);
                }
            }
            return QwenResponse.builder()
                    .success(false)
                    .statusCode(500)
                    .message("调用通义千问多模态API失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Flowable<QwenResponse> streamMultiModalChat(QwenMultiModalRequest request) {
        log.warn("通义千问多模态流式API目前不支持");
        return Flowable.error(new UnsupportedOperationException("通义千问多模态流式API目前不支持"));
    }

    private QwenResponse buildResponse(GenerationResult result, String conversationId) {
        QwenResponse response = new QwenResponse();
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }
        response.setConversationId(conversationId);
        response.setRequestId(result.getRequestId());
        response.setSuccess(true);
        response.setStatusCode(200);
        if (result.getOutput() != null) {
            if (result.getOutput().getChoices() != null && !result.getOutput().getChoices().isEmpty()) {
                response.setContent(result.getOutput().getChoices().get(0).getMessage().getContent());
                response.setFinishReason(result.getOutput().getChoices().get(0).getFinishReason());
            } else if (result.getOutput().getText() != null) {
                response.setContent(result.getOutput().getText());
                response.setFinishReason(result.getOutput().getFinishReason());
            }
        }
        if (result.getUsage() != null) {
            QwenResponse.Usage usage = new QwenResponse.Usage();
            usage.setInputTokens(result.getUsage().getInputTokens());
            usage.setOutputTokens(result.getUsage().getOutputTokens());
            usage.setTotalTokens(result.getUsage().getTotalTokens());
            response.setUsage(usage);
        }
        return response;
    }

    private QwenResponse buildMultiModalResponse(MultiModalConversationResult result, String conversationId) {
        QwenResponse response = new QwenResponse();
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }
        response.setConversationId(conversationId);
        response.setRequestId(result.getRequestId());
        response.setSuccess(true);
        response.setStatusCode(200);
        if (result.getOutput() != null && result.getOutput().getChoices() != null
                && !result.getOutput().getChoices().isEmpty()) {
            Object contentObj = result.getOutput().getChoices().get(0).getMessage().getContent();
            String content = null;
            if (contentObj instanceof String) {
                content = (String) contentObj;
            } else if (contentObj instanceof List<?>) {
                List<?> contentList = (List<?>) contentObj;
                if (!contentList.isEmpty() && contentList.get(0) instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> contentMap = (Map<String, Object>) contentList.get(0);
                    if (contentMap.containsKey("text")) {
                        content = contentMap.get("text").toString();
                    }
                }
            }
            response.setContent(content);
            response.setFinishReason(result.getOutput().getChoices().get(0).getFinishReason());
        }
        if (result.getUsage() != null) {
            QwenResponse.Usage usage = new QwenResponse.Usage();
            usage.setInputTokens(result.getUsage().getInputTokens());
            usage.setOutputTokens(result.getUsage().getOutputTokens());
            if (result.getUsage().getImageTokens() != null) {
                usage.setImageTokens(result.getUsage().getImageTokens());
            }
            if (result.getUsage().getVideoTokens() != null) {
                usage.setVideoTokens(result.getUsage().getVideoTokens());
            }
            if (result.getUsage().getAudioTokens() != null) {
                usage.setAudioTokens(result.getUsage().getAudioTokens());
            }
            response.setUsage(usage);
        }
        return response;
    }

    private QwenResponse createErrorResponse(String errorMessage) {
        return QwenResponse.builder()
                .success(false)
                .statusCode(400)
                .message(errorMessage)
                .build();
    }

    private QwenResponse analyzeDownloadFailure(String errorMessage, String type) {
        return QwenResponse.builder()
                .success(false)
                .statusCode(400)
                .message("无法下载多模态内容。请确保URL使用HTTPS协议、资源公开可访问且格式受支持。建议使用阿里云OSS格式。")
                .build();
    }

    private QwenResponse handleInvalidBackendResponse(String errorMessage) {
        return QwenResponse.builder()
                .success(false)
                .statusCode(500)
                .message("通义千问API后端响应无效。这通常是临时性服务器问题，请稍后重试。")
                .build();
    }

    private QwenResponse handleInvalidParameterError(String errorMessage) {
        return QwenResponse.builder()
                .success(false)
                .statusCode(400)
                .message("通义千问API参数无效: " + errorMessage)
                .build();
    }
}
