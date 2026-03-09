package com.norman.normanaiagent.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通义千问响应")
public class QwenResponse {

    private String conversationId;
    private String requestId;
    private String content;
    private String finishReason;
    private Usage usage;
    private SearchInfo searchInfo;
    private String code;
    private String message;
    private Integer statusCode;
    private Boolean success;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用量信息")
    public static class Usage {
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer totalTokens;
        private Integer imageTokens;
        private Integer videoTokens;
        private Integer audioTokens;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "搜索信息")
    public static class SearchInfo {
        private List<SearchResult> searchResults;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "搜索结果")
    public static class SearchResult {
        private String siteName;
        private String icon;
        private Integer index;
        private String title;
        private String url;
    }
}
