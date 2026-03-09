package com.norman.normanaiagent.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通义千问多模态请求")
public class QwenMultiModalRequest {

    @Schema(description = "会话ID")
    private String conversationId;

    @Schema(description = "文本内容")
    private String textContent;

    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @Builder.Default
    private List<String> videoUrls = new ArrayList<>();

    @Schema(description = "音频URL")
    private String audioUrl;

    @Builder.Default
    private String systemPrompt = "You are a helpful assistant.";

    @Builder.Default
    private List<Map<String, Object>> history = new ArrayList<>();

    private Boolean streamOutput;

    @Schema(description = "多模态类型", allowableValues = {"IMAGE", "VIDEO", "AUDIO"})
    private String type;

    private String model;
}
