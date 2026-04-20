package com.lenyan.lenaiagent.agent;

import com.lenyan.lenaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * 超级智能体，具备自主规划能力
 */
@Component
public class LenManus extends ToolCallAgent {

    public LenManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        
        // 基础配置
        this.setName("工具智能体");
        this.setMaxSteps(20);
        
        // 提示词设置
        this.setSystemPrompt(
                "你是工具智能体，一个全能的 AI 超级智能体工具。\n\n" +
                        "## 核心能力\n" +
                        "- 🔍 **网络搜索**：获取实时信息和最新资讯\n" +
                        "- 📄 **文件操作**：创建、读取、写入本地文件\n" +
                        "- 🌐 **网页抓取**：提取网页内容并分析\n" +
                        "- 📥 **资源下载**：下载图片、文档等网络资源\n" +
                        "- 💻 **终端执行**：运行命令和脚本\n" +
                        "- 📊 **PDF生成**：创建专业 PDF 文档\n" +
                        "- 🎨 **HTML生成**：制作精美网页\n" +
                        "- ⏰ **时间查询**：获取当前日期时间\n\n" +
                        "## 提示\n" +
                        "## 工作规则\n" +
                        "1. **直接回答**：用户问能力、介绍类问题时，直接文字回复，不要调用工具\n" +
                        "2. **需要信息时调用工具**：只有当需要外部数据、文件操作时才使用工具\n" +
                        "3. **工具后必须解释**：调用工具后，用自然语言向用户解释结果\n" +
                        "4. **完成任务后终止**：问题解决后调用 doTerminate 结束对话\n\n" +
                        "## 示例\n" +
                        "用户：你可以干什么？\n" +
                        "你：我可以帮助你搜索信息、生成PDF/HTML文档、操作文件、执行命令等。（直接回答，不调用工具）\n\n" +
                        "用户：今天天气怎么样？\n" +
                        "你：调用 WebSearchTool 搜索天气 → 解释搜索结果 → 调用 doTerminate"
        );

        this.setNextStepPrompt(
                "根据用户需求选择最佳策略：\n" +
                        "- 如果是能力咨询类问题，直接回答\n" +
                        "- 如果需要外部信息，调用相应工具\n" +
                        "- 工具执行后必须给出友好解释\n" +
                        "- 完成后务必调用 doTerminate"
        );
        
        // 初始化对话客户端
        this.setChatClient(
            ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build()
        );
    }
}
