package com.lenyan.lenaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.lenyan.lenaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 代理基类，管理状态和执行流程
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 核心属性
    private String name;
    private String systemPrompt;
    private String nextStepPrompt;

    // 状态与执行控制
    private AgentState state = AgentState.IDLE;
    private int currentStep = 0;
    private int maxSteps = 10;

    // 循环检测
    private int duplicateThreshold = 2;

    // LLM与记忆
    private ChatClient chatClient;
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理
     */
    public String run(String userPrompt) {
        // 校验
        if (state != AgentState.IDLE || StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("无法执行: " + (state != AgentState.IDLE ? "状态错误" : "空提示词"));
        }

        // 执行
        state = AgentState.RUNNING;
        messageList.add(new UserMessage(userPrompt));
        List<String> results = new ArrayList<>();

        try {
            // 步骤循环
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                currentStep = i + 1;
                log.info("执行步骤 {}/{}", currentStep, maxSteps);
                String stepResult = step();
                results.add("步骤 " + currentStep + ": " + stepResult);

                // 检查是否陷入循环
                if (isStuck()) {
                    handleStuckState();
                    results.add("检测到可能的循环，已添加额外提示以避免重复");
                }
            }

            // 检查终止条件
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("终止: 达到最大步骤 (" + maxSteps + ")");
            }

            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("执行错误", e);
            return "执行错误: " + e.getMessage();
        } finally {
            cleanup();
        }
    }

      /**
     * 运行代理(流式输出)*/

    public SseEmitter runStream(String userPrompt) {
        //1.创建一个 SseEmitter 对象，设置超时时间为 5 分钟
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        //2.异步执行任务，避免阻塞主线程,
        CompletableFuture.runAsync(() -> {
            try {
                // 校验当前状态是否为空闲状态，且用户输入是否为空
                if (state != AgentState.IDLE || StrUtil.isBlank(userPrompt)) {
                    String error = "错误: " + (state != AgentState.IDLE ? "无法从状态运行: " + state : "空提示词");
                    emitter.send(error); // 发送错误信息
                    emitter.complete(); // 完成 SSE 连接
                    return;
                }

                //3.设置状态为运行中，并添加用户输入到消息列表
                state = AgentState.RUNNING;
                messageList.add(new UserMessage(userPrompt));

                //4.循环执行步骤，直到达到最大步骤数或状态变为已完成
                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    currentStep = i + 1; // 更新当前步骤
                    log.info("执行步骤 {}/{}", currentStep, maxSteps); // 记录日志
                    String stepResult = step(); // 执行单个步骤
                    String result = "步骤 " + currentStep + ": " + stepResult;
                    emitter.send(result); // 发送步骤结果

                    //5.检查是否陷入循环状态
                    if (isStuck()) {
                        handleStuckState(); // 处理循环状态
                        emitter.send("检测到可能的循环，已添加额外提示以避免重复");
                    }
                }

                //7.新增：提取并发送智能体的最终答案
                String finalAnswer = extractFinalAnswer();
                if (finalAnswer != null && !finalAnswer.isEmpty()) {
                    try {
                        emitter.send(finalAnswer);
                        log.info("已发送最终答案: {}", finalAnswer.substring(0, Math.min(50, finalAnswer.length())));
                    } catch (IOException e) {
                        log.warn("发送最终答案失败: {}", e.getMessage());
                    }
                }

                //6.如果达到最大步骤数，设置状态为已完成并发送结束信息(防止智能体无限执行)
                if (currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    emitter.send("执行结束: 达到最大步骤 (" + maxSteps + ")");
                }
                emitter.complete(); // 完成 SSE 连接
            } catch (Exception e) {
                // 捕获异常，设置状态为错误并记录日志
                state = AgentState.ERROR;
                log.error("执行错误", e);
                try {
                    emitter.send("执行错误: " + e.getMessage()); // 发送错误信息
                    emitter.complete(); // 完成 SSE 连接
                } catch (IOException ex) {
                    emitter.completeWithError(ex); // 处理发送错误时的异常
                }
            } finally {
                cleanup(); // 清理资源
            }
        });

        //8.设置 SSE 连接的超时处理逻辑
        emitter.onTimeout(() -> {
            state = AgentState.ERROR; // 设置状态为错误
            cleanup(); // 清理资源
            log.warn("SSE连接超时"); // 记录超时日志
        });

        //9.设置 SSE 连接的完成处理逻辑
        emitter.onCompletion(() -> {
            if (state == AgentState.RUNNING) {
                state = AgentState.FINISHED; // 如果仍在运行，设置状态为已完成
            }
            cleanup(); // 清理资源
            log.info("SSE连接完成"); // 记录完成日志
        });

        return emitter; // 返回 SseEmitter 对象
    }

    /**
     * 提取智能体的最终答案
     * 从消息列表中获取最后一条助手消息作为最终回答
     */
    protected String extractFinalAnswer() {
        // 从后往前查找最后一条 AssistantMessage
        for (int i = messageList.size() - 1; i >= 0; i--) {
            if (messageList.get(i) instanceof AssistantMessage) {
                AssistantMessage assistantMsg = (AssistantMessage) messageList.get(i);
                String text = assistantMsg.getText();
                // 过滤掉工具结果等中间信息，只返回有意义的回答
                if (text != null && !text.isEmpty() && !text.contains("工具[") && !text.contains("思考完成")) {
                    return text;
                }
            }
        }
        return null;
    }


    /**
     * 定义单个执行步骤
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 重置WebSearchTool的搜索次数计数器
        com.lenyan.lenaiagent.tools.WebSearchTool.resetSearchCallCount();
        log.debug("清理资源：已重置WebSearchTool的搜索次数计数器");
        // 子类可重写
    }

    /**
     * 处理陷入循环的状态
     */
    protected void handleStuckState() {
        String stuckPrompt = "观察到重复响应。请考虑新的策略，避免重复已尝试过的无效路径。";
        this.nextStepPrompt = stuckPrompt + "\n" + (this.nextStepPrompt != null ? this.nextStepPrompt : "");
        log.warn("检测到智能体陷入循环状态。添加额外提示: {}", stuckPrompt);
    }

    /**
     * 检查代理是否陷入循环
     *
     * @return 是否陷入循环
     */
    protected boolean isStuck() {
        if (messageList.size() < 2) {
            return false;
        }

        // 获取最后一条助手消息
        AssistantMessage lastAssistantMessage = null;
        for (int i = messageList.size() - 1; i >= 0; i--) {
            if (messageList.get(i) instanceof AssistantMessage) {
                lastAssistantMessage = (AssistantMessage) messageList.get(i);
                break;
            }
        }

        if (lastAssistantMessage == null || lastAssistantMessage.getText() == null
                || lastAssistantMessage.getText().isEmpty()) {
            return false;
        }

        // 计算重复内容出现次数
        int duplicateCount = 0;
        String lastContent = lastAssistantMessage.getText();

        for (int i = messageList.size() - 2; i >= 0; i--) {
            Message msg = messageList.get(i);
            if (msg instanceof AssistantMessage) {
                AssistantMessage assistantMsg = (AssistantMessage) msg;
                if (lastContent.equals(assistantMsg.getText())) {
                    duplicateCount++;

                    if (duplicateCount >= this.duplicateThreshold) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
