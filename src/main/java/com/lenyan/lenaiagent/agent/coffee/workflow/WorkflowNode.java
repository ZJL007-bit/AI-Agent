package com.lenyan.lenaiagent.agent.coffee.workflow;

/**
 * 工作流节点接口
 * <p>
 * 定义了工作流中每个处理节点的统一规范。
 * 每个节点实现此接口，在 execute 方法中完成特定的处理逻辑，
 * 并通过返回值指定下一个要执行的节点名称。
 * </p>
 * <p>
 * 节点执行流程：
 * 1. 从 AgentState 中读取输入数据
 * 2. 执行本节点的业务逻辑
 * 3. 将处理结果写回 AgentState
 * 4. 返回下一个节点的名称，或 "END" 表示流程结束
 * </p>
 * <p>
 * 现有节点：
 * - preprocessNode: 预处理（敏感词过滤、消息清洗）
 * - intentClassifierNode: 意图分类
 * - productDialogNode: 商品对话处理
 * - orderDialogNode: 订单对话处理
 * - chitchatNode: 闲聊处理
 * - knowledgeRetrievalNode: 知识检索
 * - transferToHumanNode: 转人工
 * - errorHandlerNode: 错误处理
 * </p>
 *
 * @author 曾家乐
 */
public interface WorkflowNode {

    /**
     * 执行工作流节点的处理逻辑
     * <p>
     * 每个节点在实现此方法时，应遵循以下步骤：
     * 1. 读取 AgentState 中所需的数据
     * 2. 执行核心业务逻辑
     * 3. 将处理结果写入 AgentState
     * 4. 返回下一个节点名称或 "END"
     * </p>
     *
     * @param state 工作流状态对象，包含上下文数据和中间结果
     * @return 下一个要执行的节点名称，返回 "END" 表示工作流结束
     */
    String execute(AgentState state);
}
