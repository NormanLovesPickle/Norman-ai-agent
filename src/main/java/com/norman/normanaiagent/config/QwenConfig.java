package com.norman.normanaiagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "qwen")
public class QwenConfig {

    private String apiKey;
    private String textModel = "qwen-plus";
    private String visionModel = "qwen-vl-plus";
    private String videoModel = "qwen-vl-max-latest";
    private String audioModel = "qwen2-audio-instruct";
    private boolean streamOutput = false;
    private boolean enableSearch = false;
}
