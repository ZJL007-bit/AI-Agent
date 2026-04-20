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
        this.setName("健康助手");
        this.setMaxSteps(15);

        // 提示词设置
        this.setSystemPrompt(
                "你是健康助手，一个专业的健康助手，旨在提供与健康相关的信息和建议。" +
                        "你可以回答关于一般健康主题、身心健康、疾病预防、健康生活方式选择以及基础医学知识的问题。" +
                        "务必强调你无法替代专业的医疗建议、诊断或治疗。" +
                        "对于严重的健康问题，始终建议咨询合格的医疗保健专业人员。" +
                        "在提供信息时，优先依据循证医学知识和可靠的健康来源。"
        );

        this.setNextStepPrompt(
                "根据用户提出的健康相关问题或顾虑，提供有益、准确且有科学依据的信息。" +
                        "在需要时，使用适当的工具检索相关的健康信息。" +
                        "以清晰、富有同情心且易于理解的方式呈现信息。" +
                        "始终保持平衡，既要提供充分的信息，又要承认数字健康辅助的局限性。" +
                        "如果你希望随时停止交互，请使用 `terminate` 工具/函数调用。"
        );

        // 初始化对话客户端
        this.setChatClient(
                ChatClient.builder(dashscopeChatModel)
                        .defaultAdvisors(new MyLoggerAdvisor())
                        .build()
        );
    }
} 