package com.lenyan.lenaiagent.agent.coffee.workflow.node;

import com.lenyan.lenaiagent.agent.coffee.workflow.AgentState;
import com.lenyan.lenaiagent.agent.coffee.workflow.WorkflowNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 错误处理节点
 * <p>
 * 工作流的异常兜底节点，当其他节点处理过程中发生异常时，
 * 路由到此节点进行统一的错误处理和用户提示。
 * 确保用户在遇到异常时不会收到无响应或技术性错误信息，
 * 而是收到友好的提示和后续操作引导。
 * </p>
 * <p>
 * 适用场景：
 * - AI 模型调用超时或返回异常
 * - 数据库查询失败
 * - 知识库检索异常
 * - 其他未知异常
 * </p>
 *
 * @author 曾家乐
 */
@Slf4j
@Component("errorHandlerNode")
@RequiredArgsConstructor
public class ErrorHandlerNode implements WorkflowNode {

    /**
     * 执行错误处理逻辑
     * <p>
     * 处理流程：
     * 1. 记录执行轨迹
     * 2. 生成友好的错误提示消息
     * 3. 提供后续操作建议（重试或转人工）
     * 4. 将消息设置到 AgentState 并返回 "END"
     * </p>
     *
     * @param state 工作流状态对象
     * @return "END"（错误处理节点为终端节点）
     */
    @Override
    public String execute(AgentState state) {
        log.debug("进入错误处理节点");
        state.addTrace("ERROR_HANDLER");

        // 生成友好的错误提示，避免暴露技术细节
        String errorMessage = "抱歉，我暂时无法处理您的请求。请稍后再试，或输入:转人工,联系客服获取帮助。";
        state.setResponse(errorMessage);

        log.error("工作流异常: sessionId={}, trace={}", state.getSessionId(), state.getTrace());
        return "END";
    }
}
