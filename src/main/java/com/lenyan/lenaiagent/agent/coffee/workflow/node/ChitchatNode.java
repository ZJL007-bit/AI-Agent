package com.lenyan.lenaiagent.agent.coffee.workflow.node;

import com.lenyan.lenaiagent.agent.coffee.config.AgentProperties;
import com.lenyan.lenaiagent.agent.coffee.service.KnowledgeBaseService;
import com.lenyan.lenaiagent.agent.coffee.workflow.AgentState;
import com.lenyan.lenaiagent.agent.coffee.workflow.WorkflowNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 闲聊节点
 * <p>
 * 处理用户的日常闲聊意图，包括问候、闲谈、无关咖啡的日常对话等。
 * 在友好自然的对话中，适时引导用户回到咖啡相关话题，
 * 避免对话偏离太远。
 * </p>
 * <p>
 * 此节点也作为意图分类的兜底处理，当 AI 无法明确判断用户意图时，
 * 默认路由到此节点进行轻松的对话。
 * </p>
 * <p>
 * 增强功能：在调用大模型前，先从 QA 知识库（QA.csv）中检索与用户消息
 * 相关的问答对作为参考上下文。如果检索到相关内容，则基于知识库内容生成回复；
 * 如果未检索到，则完全依赖模型自身知识进行回答。
 * </p>
 *
 * @author 曾家乐
 */
@Slf4j
@Component("chitchatNode")
@RequiredArgsConstructor
public class ChitchatNode implements WorkflowNode {

    private final ChatClient.Builder chatClientBuilder;
    private final AgentProperties agentProperties;
    private final KnowledgeBaseService knowledgeBaseService;

    @Override
    public String execute(AgentState state) {
        log.debug("进入闲聊节点");
        state.addTrace("CHITCHAT");

        String userMessage = state.getProcessedMessage();

        // 从 QA 知识库中检索与用户消息相关的问答对
        String knowledgeContext = knowledgeBaseService.getFormattedContext(userMessage);

        String systemPrompt = agentProperties.getChitchatPrompt();
        if (knowledgeContext != null && !knowledgeContext.isEmpty()) {
            systemPrompt = systemPrompt.replace("{knowledge}", knowledgeContext);
            log.debug("闲聊节点 RAG 增强: 检索到相关知识");
        } else {
            systemPrompt = systemPrompt.replace("{knowledge}", "");
            log.debug("闲聊节点: 未检索到相关知识，使用基础闲聊模式");
        }

        String response = chatClientBuilder.build()
                .prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();

        state.setResponse(response);
        log.info("闲聊完成: sessionId={}", state.getSessionId());
        return "END";
    }
}
