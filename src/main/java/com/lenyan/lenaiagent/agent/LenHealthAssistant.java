package com.lenyan.lenaiagent.agent;

import com.lenyan.lenaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * 云医通健康助手，提供健康咨询和医疗建议
 */
@Component
public class LenHealthAssistant extends ToolCallAgent {

    public LenHealthAssistant(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        
        // 基础配置
        this.setName("lenHealthAssistant");
        this.setMaxSteps(15);
        
        // 提示词设置
        this.setSystemPrompt(
            "You are LenHealthAssistant, a professional health assistant designed to provide health-related information and advice. " +
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
        
        // 初始化对话客户端
        this.setChatClient(
            ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build()
        );
    }
} 