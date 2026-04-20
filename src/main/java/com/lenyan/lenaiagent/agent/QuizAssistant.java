package com.lenyan.lenaiagent.agent;

import com.lenyan.lenaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 智慧答题助手，专注于辅助用户解答各类题目
 */
@Component
public class QuizAssistant extends ToolCallAgent {

    @Autowired
    public QuizAssistant(@Qualifier("allTools") ToolCallback[] allTools,
                         @Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel) {
        super(allTools);


        // 基础配置：设置智能体的名称、最大执行步骤数和重复阈值
        this.setName("智慧问答"); // 设置智能体名称
        this.setMaxSteps(15); // 设置最大执行步骤数
        this.setDuplicateThreshold(3); // 设置重复内容的阈值

        // 提示词设置
        this.setSystemPrompt(
                "你是智慧答题助手，专注于学科辅导与测评深度分析。你的目标是帮助用户高效解决学习难题，并提供专业的测评解读。\n\n" +
                        "## 核心能力\n" +
                        "- 📚 **学科问题解答**：精通数学、英语、物理、化学等多学科知识，提供准确答案。\n" +
                        "- 🔍 **复杂题目分析**：拆解复杂问题，提供清晰的解题思路与步骤，不仅仅是给答案。\n" +
                        "- 📊 **测评结果解读**：专业解读性格测试、能力评估等各类测评结果，挖掘深层特征。\n" +
                        "- 📝 **个性化分析报告**：根据用户数据生成定制化的学习与能力分析报告。\n" +
                        "- 📄 **PDF 可视化报告**：生成包含图表、图片的专业 PDF 报告，辅助学习与展示。\n\n" +
                        "## 工作规则\n" +
                        "1. **解题优先**：遇到具体题目，先分析考点，再给出步骤清晰的解答。\n" +
                        "2. **工具辅助**：遇到需要生成报告或复杂计算的场景，主动调用 PDF 生成或计算工具。\n" +
                        "3. **结构化输出**：回答应条理清晰，使用 Markdown 格式增强可读性。\n" +
                        "4. **友好引导**：在解答后提供延伸建议，帮助用户举一反三。\n" +
                        "5. **适时终止**：任务完成后调用 doTerminate 结束对话。"
        );

        this.setNextStepPrompt(
                "根据用户输入判断意图：\n" +
                        "- 若是具体题目：进行知识点拆解 -> 提供解题步骤 -> 总结考点。\n" +
                        "- 若是测评数据：分析数据维度 -> 给出解读结论 -> 生成 PDF 报告。\n" +
                        "- 完成后务必调用 doTerminate。"
        );

        // 初始化对话客户端
        this.setChatClient(
                ChatClient.builder(dashscopeChatModel)
                        .defaultAdvisors(new MyLoggerAdvisor())
                        .build()
        );
    }
} 