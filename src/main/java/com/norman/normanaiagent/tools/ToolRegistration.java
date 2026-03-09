package com.norman.normanaiagent.tools;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${spring.mail.host:smtp.qq.com}")
    private String mailHost;

    @Value("${spring.mail.port:465}")
    private int mailPort;

    @Value("${pexels.api-key:}")
    private String pexelsApiKey;

    @Autowired(required = false)
    private JdbcTemplate mysqlJdbcTemplate;

    @Autowired(required = false)
    private NamedParameterJdbcTemplate mysqlNamedParameterJdbcTemplate;

    @Bean
    public ToolCallback[] allTools() {
        List<Object> tools = new ArrayList<>();
        tools.add(new FileOperationTool());
        tools.add(new WebSearchTool(searchApiKey));
        tools.add(new WebScrapingTool());
        tools.add(new ResourceDownloadTool());
        tools.add(new TerminalOperationTool());
        tools.add(new PDFGenerationTool());
        tools.add(new HtmlGenerationTool());
        tools.add(new DateTimeTool());
        tools.add(new EmailSendingTool(mailUsername, mailPassword, mailHost, mailPort));
        if (mysqlJdbcTemplate != null && mysqlNamedParameterJdbcTemplate != null) {
            tools.add(new DatabaseOperationTool(mysqlJdbcTemplate, mysqlNamedParameterJdbcTemplate));
        }
        if (org.springframework.util.StringUtils.hasText(pexelsApiKey)) {
            tools.add(new ImageSearchTool(pexelsApiKey));
        }
        tools.add(new TerminateTool());
        return ToolCallbacks.from(tools.toArray());
    }
}
