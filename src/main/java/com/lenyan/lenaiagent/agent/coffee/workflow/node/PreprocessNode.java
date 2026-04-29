package com.lenyan.lenaiagent.agent.coffee.workflow.node;

import com.lenyan.lenaiagent.agent.coffee.service.SensitiveFilter;
import com.lenyan.lenaiagent.agent.coffee.workflow.AgentState;
import com.lenyan.lenaiagent.agent.coffee.workflow.WorkflowNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 预处理节点
 * <p>
 * 工作流的第一个节点，在意图分类之前对用户消息进行预处理，包括：
 * <ul>
 *     <li>敏感词过滤：将用户输入中的敏感词替换为 "***"</li>
 *     <li>消息清洗：去除首尾空白字符</li>
 *     <li>空消息检测：如果消息为空则直接返回提示</li>
 * </ul>
 * 预处理后的消息存储在 state.processedMessage 中，供后续节点使用。
 * </p>
 *
 * @author 曾家乐
 */
@Slf4j
@Component("preprocessNode")
@RequiredArgsConstructor
public class PreprocessNode implements WorkflowNode {

    /**
     * 敏感词过滤服务，用于检测和替换用户输入中的敏感词
     */
    private final SensitiveFilter sensitiveFilter;

    /**
     * 执行预处理逻辑
     * <p>
     * 处理流程：
     * 1. 记录执行轨迹
     * 2. 检查消息是否为空，为空则直接返回提示并结束
     * 3. 对用户消息进行敏感词过滤
     * 4. 将处理后的消息存入 state.processedMessage
     * 5. 路由到意图分类节点
     * </p>
     *
     * @param state 工作流状态对象
     * @return 下一个节点名称 "intentClassifierNode"，或 "END"（消息为空时）
     */
    @Override
    public String execute(AgentState state) {
        log.debug("进入预处理节点");
        state.addTrace("PREPROCESS");

        String message = state.getUserMessage();

        // 空消息检测
        if (message == null || message.trim().isEmpty()) {
            state.setResponse("请输入您的问题，我来为您服务~");
            return "END";
        }

        // 检测敏感词
        if (sensitiveFilter.containsSensitiveWord(message)) {
            log.warn("检测到敏感词: sessionId={}", state.getSessionId());
            state.setResponse("您的消息中包含不当内容，请注意文明用语。如需帮助请联系人工客服。");
            return "END";
        }

        // 敏感词过滤后的消息存入 processedMessage
        state.setProcessedMessage(message.trim());

        // 路由到意图分类节点
        return "intentClassifierNode";
    }
}
