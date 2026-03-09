package com.norman.normanaiagent.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "spring.datasource.mysql", name = "url")
@MapperScan("com.norman.normanaiagent.mapper")
public class MyBatisConfig {
}
