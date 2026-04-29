package com.lenyan.lenaiagent.agent.coffee.workflow;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流状态对象
 * <p>
 * 在整个对话工作流中传递的上下文状态，携带用户消息、意图识别结果、
 * 业务处理数据及最终的回复内容。每个工作流节点通过读取和修改此对象
 * 来完成各自的处理逻辑，实现节点间的数据流转。
 * </p>
 * <p>
 * 生命周期：从用户发送消息创建，经过预处理 → 意图分类 → 业务处理 → 返回回复后结束。
 * </p>
 *
 * @author 曾家乐
 */
@Data
public class AgentState {

    /**
     * 聊天会话唯一标识
     * 首次对话时由后端生成，后续对话中保持不变，用于关联同一用户的多轮对话
     */
    private String sessionId;

    /**
     * 用户标识，用于区分不同的用户
     */
    private String userId;

    /**
     * 用户输入的原始消息内容
     * 在预处理节点中会进行敏感词过滤等处理
     */
    private String userMessage;

    /**
     * 经过预处理后的用户消息（如已过滤敏感词）
     * 后续节点使用此字段而非 userMessage
     */
    private String processedMessage;

    /**
     * 意图分类结果，由 IntentClassifierNode 设置
     * 可选值：
     * - QUERY_PRODUCT: 查询商品信息
     * - ORDER: 下单相关操作
     * - CHITCHAT: 日常闲聊
     * - TRANSFER_TO_HUMAN: 请求转人工客服
     * - ERROR: 处理异常
     */
    private String intent;

    /**
     * 意图分类的置信度分数，0.0 ~ 1.0
     * 置信度较低时可能需要进一步确认用户意图
     */
    private Double confidence;

    /**
     * 工作流执行轨迹，记录经过的节点名称
     * 用于调试和日志追踪，可以查看请求经过了哪些处理步骤
     */
    private List<String> trace = new ArrayList<>();

    /**
     * 智能助手的最终回复内容
     * 由各个业务处理节点设置，作为工作流的输出结果
     */
    private String response;

    /**
     * 扩展数据字典，用于存放各节点产生的临时数据
     * 例如：检索到的知识片段、推荐的商品列表、订单信息等
     * Key 为数据标识，Value 为数据内容
     */
    private Map<String, Object> context = new HashMap<>();

    /**
     * 添加执行轨迹记录
     * <p>
     * 每个工作流节点在开始执行时应调用此方法，记录请求经过的处理步骤。
     * </p>
     *
     * @param node 节点名称，如 "PREPROCESS"、"INTENT_CLASSIFIER" 等
     */
    public void addTrace(String node) {
        trace.add(node);
    }

    /**
     * 向扩展数据字典中添加数据
     * <p>
     * 工作流节点可使用此方法存储中间结果，供后续节点读取使用。
     * </p>
     *
     * @param key   数据标识
     * @param value 数据内容
     */
    public void putContext(String key, Object value) {
        context.put(key, value);
    }

    /**
     * 从扩展数据字典中获取数据
     * <p>
     * 工作流节点可使用此方法读取前序节点存储的中间结果。
     * </p>
     *
     * @param key 数据标识
     * @return 数据内容，不存在时返回 null
     */
    public Object getContext(String key) {
        return context.get(key);
    }
}
