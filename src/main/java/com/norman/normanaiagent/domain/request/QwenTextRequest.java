package com.norman.normanaiagent.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "通义千问文本请求")
public class QwenTextRequest {

    @Schema(description = "会话ID")
    private String conversationId;

    @Schema(description = "用户消息内容")
    private String content;

    @Schema(description = "系统提示词")
    private String systemPrompt = "You are a helpful assistant.";

    @Schema(description = "是否使用流式输出")
    private Boolean streamOutput;

    @Schema(description = "历史消息列表")
    private List<Map<String, String>> history = new ArrayList<>();

    @Schema(description = "是否使用联网搜索")
    private Boolean enableSearch;

    @Schema(description = "模型名称")
    private String model;
}
