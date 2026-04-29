package com.lenyan.lenaiagent.agent.coffee.workflow.node;

import com.lenyan.lenaiagent.agent.coffee.service.KnowledgeBaseService;
import com.lenyan.lenaiagent.agent.coffee.workflow.AgentState;
import com.lenyan.lenaiagent.agent.coffee.workflow.WorkflowNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 知识检索节点
 * <p>
 * 基于用户查询从知识库中检索相关的咖啡知识片段，
 * 检索结果存储到 AgentState 的 context 中，
 * 供后续节点（如商品对话节点）作为 AI 模型的参考上下文使用，
 * 实现 RAG（检索增强生成）效果。
 * </p>
 * <p>
 * 工作原理：
 * 1. 读取用户的处理后消息
 * 2. 调用知识库服务进行语义相似度检索
 * 3. 将检索结果格式化为文本上下文
 * 4. 存入 AgentState.context 供后续节点使用
 * </p>
 *
 * @author 曾家乐
 */
@Slf4j
@Component("knowledgeRetrievalNode")
@RequiredArgsConstructor
public class KnowledgeRetrievalNode implements WorkflowNode {

    /**
     * 知识库服务，基于向量数据库进行语义检索
     */
    private final KnowledgeBaseService knowledgeBaseService;

    /**
     * 执行知识检索逻辑
     * <p>
     * 处理流程：
     * 1. 记录执行轨迹
     * 2. 使用用户消息在知识库中检索相关内容
     * 3. 将格式化后的知识上下文存入 AgentState
     * 4. 返回 "END"（此节点为辅助节点，检索结果由调用方使用）
     * </p>
     *
     * @param state 工作流状态对象
     * @return "END"（此节点为终端辅助节点）
     */
    @Override
    public String execute(AgentState state) {
        log.debug("进入知识检索节点");
        state.addTrace("KNOWLEDGE_RETRIEVAL");

        // 基于用户消息检索相关知识片段
        String knowledgeContext = knowledgeBaseService.getFormattedContext(state.getProcessedMessage());

        // 将检索结果存入上下文，供后续 AI 模型调用时使用
        state.putContext("knowledgeContext", knowledgeContext);

        log.debug("知识检索完成: sessionId={}, contextLength={}", state.getSessionId(), knowledgeContext.length());
        return "END";
    }
}
