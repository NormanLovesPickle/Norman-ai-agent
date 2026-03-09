package com.norman.normanaiagent.agent;

import com.norman.normanaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
public class NormanHealthAssistant extends ToolCallAgent {

    public NormanHealthAssistant(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("normanHealthAssistant");
        this.setMaxSteps(15);
        this.setSystemPrompt(
            "You are NormanHealthAssistant, a professional health assistant designed to provide health-related information and advice. " +
            "You can answer questions about general health topics, wellness, disease prevention, healthy lifestyle choices, and basic medical knowledge. " +
            "Always emphasize that you are not a replacement for professional medical advice, diagnosis, or treatment. " +
            "For serious health concerns, always recommend consulting with qualified healthcare professionals. " +
            "When providing information, prioritize evidence-based medical knowledge and reliable health sources."
        );
        this.setNextStepPrompt(
            "Based on the user's health-related questions or concerns, provide helpful, accurate, and scientifically-backed information. " +
            "Use appropriate tools to retrieve relevant health information when needed. " +
            "Present information in a clear, compassionate, and easy-to-understand manner. " +
            "Always maintain a balance between being informative and acknowledging the limitations of digital health assistance. " +
            "If you want to stop the interaction at any point, use the `terminate` tool/function call."
        );
        this.setChatClient(
            ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build()
        );
    }
}
