package com.lenyan.lenaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.lenyan.lenaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 工具调用代理，实现think和act方法处理工具调用
 */
@EqualsAndHashCode(callSuper = true) // 继承父类的equals和hashCode方法
@Data // 自动生成getter、setter、toString等方法
@Slf4j // 自动生成日志记录器
public class ToolCallAgent extends ReActAgent {

    private final ToolCallback[] availableTools; // 可用的工具回调数组
    private final ToolCallingManager toolCallingManager; // 工具调用管理器
    private final ChatOptions chatOptions; // 聊天选项配置
    private ChatResponse toolCallChatResponse; // 工具调用的聊天响应

    public ToolCallAgent(ToolCallback[] availableTools) {
        super(); // 调用父类构造方法
        this.availableTools = availableTools; // 初始化可用工具
        this.toolCallingManager = ToolCallingManager.builder().build(); // 构建工具调用管理器
        // 禁用Spring AI内置工具调用，自行管理
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true) // 启用代理工具调用
                .build();
    }

    /**
     * 思考阶段：分析当前状态并确定要调用的工具
     */
    @Override
    public boolean think() {
        // 添加下一步提示词
        if (StrUtil.isNotBlank(getNextStepPrompt())) { // 如果下一步提示词不为空
            getMessageList().add(new UserMessage(getNextStepPrompt())); // 添加用户消息到消息列表
        }

        // 调用AI获取工具选择
        try {
            Prompt prompt = new Prompt(getMessageList(), this.chatOptions); // 创建提示对象
            ChatResponse chatResponse = getChatClient().prompt(prompt) // 调用聊天客户端生成响应
                    .system(getSystemPrompt()) // 设置系统提示词
                    .tools(availableTools) // 设置可用工具
                    .call() // 执行调用
                    .chatResponse(); // 获取聊天响应

            this.toolCallChatResponse = chatResponse; // 保存工具调用的聊天响应

            // 解析响应
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput(); // 获取助手消息
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls(); // 获取工具调用列表

            // 记录选择
            String result = assistantMessage.getText(); // 获取助手消息文本
            log.info("{}的思考: {}", getName(), result); // 记录思考结果
            log.info("{}选择了 {} 个工具", getName(), toolCallList.size()); // 记录选择的工具数量

            if (toolCallList.size() > 0) { // 如果有工具调用
                String toolCallInfo = toolCallList.stream() // 遍历工具调用列表
                        .map(tc -> String.format("工具: %s, 参数: %s", tc.name(), tc.arguments())) // 格式化工具调用信息
                        .collect(Collectors.joining("\n")); // 拼接为字符串
                log.info(toolCallInfo); // 记录工具调用信息
            }

            // 没有工具调用时记录助手消息
            if (toolCallList.isEmpty()) { // 如果工具调用列表为空
                getMessageList().add(assistantMessage); // 添加助手消息到消息列表
                return false; // 返回false表示没有工具调用
            }

            return true; // 返回true表示有工具调用
        } catch (Exception e) { // 捕获异常
            log.error("{}思考过程错误: {}", getName(), e.getMessage()); // 记录错误信息
            getMessageList().add(new AssistantMessage("处理错误: " + e.getMessage())); // 添加错误消息到消息列表
            return false; // 返回false表示思考失败
        }
    }

    /**
     * 行动阶段：执行选定的工具
     */
/*    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) { // 如果没有工具调用
            return "没有工具需要调用"; // 返回提示信息
        }

        // 执行工具调用
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions); // 创建提示对象
        ToolExecutionResult result = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse); // 执行工具调用

        // 更新消息上下文
        setMessageList(result.conversationHistory()); // 更新消息列表为对话历史
        ToolResponseMessage response = (ToolResponseMessage) CollUtil.getLast(result.conversationHistory()); // 获取最后一条工具响应消息

        // 检查是否调用了终止工具
        boolean terminated = response.getResponses().stream() // 遍历工具响应
                .anyMatch(r -> r.name().equals("doTerminate")); // 检查是否有终止工具调用
        if (terminated) { // 如果调用了终止工具
            setState(AgentState.FINISHED); // 设置代理状态为完成
        }

        // 格式化结果
        String results = response.getResponses().stream() // 遍历工具响应
                .map(r -> "工具[" + r.name() + "]结果: " + r.responseData()) // 格式化工具调用结果
                .collect(Collectors.joining("\n")); // 拼接为字符串
        log.info(results); // 记录工具调用结果

        return results; // 返回工具调用结果
    }*/

    /**
     * 行动阶段：执行选定的工具
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具需要调用";
        }

        // 执行工具调用
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        ToolExecutionResult result = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);

        // 更新消息上下文
        setMessageList(result.conversationHistory());
        ToolResponseMessage response = (ToolResponseMessage) CollUtil.getLast(result.conversationHistory());

        // 检查是否调用了终止工具
        boolean terminated = response.getResponses().stream()
                .anyMatch(r -> r.name().equals("doTerminate"));
        if (terminated) {
            setState(AgentState.FINISHED);
        }

        // 格式化结果
        String results = response.getResponses().stream()
                .map(r -> "工具[" + r.name() + "]结果: " + r.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);

        return results;
    }
}