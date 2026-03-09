package com.norman.normanaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class CareerMentorAppTest {

    @Resource
    private CareerMentorApp careerMentorApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是程序员Norman";
        String answer = careerMentorApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        message = "我想提升职场竞争力";
        answer = careerMentorApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        message = "我目前在互联网公司做开发，帮我回忆一下刚才说的";
        answer = careerMentorApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我最近在准备跳槽，想了解如何谈薪资";
        CareerMentorApp.CareerReport report = careerMentorApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(report);
        Assertions.assertNotNull(report.title());
        Assertions.assertNotNull(report.suggestions());
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "如何写好一份吸引HR的简历？";
        String answer = careerMentorApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
        testMessage("帮我分析一下当前就业市场趋势");
        testMessage("看看编程导航网站（codefather.cn）的职场课程有哪些？");
        testMessage("生成一份求职计划PDF，包含简历优化、面试准备和时间安排");
        testMessage("保存我的职场发展档案为文件");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = careerMentorApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        String message = "帮我搜索一些职场办公桌布置的图片";
        String answer = careerMentorApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);
    }
}
