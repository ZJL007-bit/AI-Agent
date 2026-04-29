package com.lenyan.lenaiagent.agent.coffee.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 工作流图配置类
 * <p>
 * 定义了咖啡智能助手的完整对话工作流，包括节点注册、边的连接关系，
 * 以及工作流的启动和节点路由逻辑。工作流采用图结构组织，
 * 每个节点处理完成后根据返回值决定下一个执行节点。
 * </p>
 * <p>
 * 工作流结构：
 * <pre>
 *                  ┌─────────────────┐
 *                  │  preprocessNode │ (预处理：敏感词过滤)
 *                  └────────┬────────┘
 *                           │
 *                  ┌────────▼────────┐
 *                  │intentClassifier │ (意图分类)
 *                  └────────┬────────┘
 *                           │
 *           ┌───────────────┼───────────────┬───────────────┐
 *           │               │               │               │
 * ┌─────────▼──────┐ ┌──────▼───────┐ ┌─────▼──────┐ ┌─────▼────────┐
 * │productDialogNode│ │orderDialogNode│ │chitchatNode│ │transferToHuman│
 * └────────────────┘ └──────────────┘ └────────────┘ └──────────────┘
 * </pre>
 * </p>
 *
 * @author 曾家乐
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentGraphConfig {

    /**
     * 工作流节点映射表
     * Key: 节点名称（如 "preprocessNode"、"intentClassifierNode"）
     * Value: 节点实现实例
     * 所有节点通过 Spring 的依赖注入自动注册
     */
    private final Map<String, WorkflowNode> nodeMap;

    /**
     * 执行完整工作流
     * <p>
     * 从预处理节点开始，按照节点返回的路由信息逐步执行，
     * 直到某个节点返回 "END" 表示工作流结束。
     * </p>
     * <p>
     * 执行流程：
     * 1. 从预处理节点开始执行
     * 2. 每个节点执行后返回下一个节点名称
     * 3. 根据返回值查找并执行下一个节点
     * 4. 遇到 "END" 或找不到节点时终止
     * </p>
     *
     * @param state 工作流状态对象，携带用户消息和上下文信息
     */
    public void runWorkflow(AgentState state) {
        runWorkflow(state, null);
    }

    /**
     * 执行完整工作流，支持进度回调
     *
     * @param state           工作流状态对象
     * @param onNodeExecuted  节点执行回调，传入节点名称；传 null 则不回调
     */
    public void runWorkflow(AgentState state, Consumer<String> onNodeExecuted) {
        String currentNode = "preprocessNode";
        int maxSteps = 10;

        while (!"END".equals(currentNode) && maxSteps-- > 0) {
            WorkflowNode node = nodeMap.get(currentNode);
            if (node == null) {
                log.error("未找到工作流节点: {}", currentNode);
                break;
            }
            log.debug("执行工作流节点: {}", currentNode);
            if (onNodeExecuted != null) {
                onNodeExecuted.accept(currentNode);
            }
            currentNode = node.execute(state);
        }

        log.info("工作流执行完成: sessionId={}, trace={}", state.getSessionId(), state.getTrace());
    }
}
