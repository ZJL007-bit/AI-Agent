package com.lenyan.lenaiagent.agent.coffee.workflow.node;

import com.lenyan.lenaiagent.agent.coffee.config.AgentProperties;
import com.lenyan.lenaiagent.agent.coffee.workflow.AgentState;
import com.lenyan.lenaiagent.agent.coffee.workflow.WorkflowNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 意图分类节点
 * <p>
 * 工作流的第二个节点，利用 AI 大模型对用户消息进行意图识别，
 * 判断用户想要进行的操作类型，作为后续路由的依据。
 * </p>
 * <p>
 * 支持的意图类型：
 * <ul>
 *     <li>QUERY_PRODUCT: 查询商品信息（如"有什么咖啡推荐"、"拿铁多少钱"）</li>
 *     <li>ORDER: 下单操作（如"我要一杯拿铁"、"来两杯美式"）</li>
 *     <li>QUERY_ORDERS: 查看订单记录（如"我的订单"、"查看历史订单"）</li>
 *     <li>CHITCHAT: 日常闲聊（如"今天天气怎么样"、"你好"）</li>
 *     <li>TRANSFER_TO_HUMAN: 转人工客服（如"我要找人工"、"转人工"）</li>
 * </ul>
 * 分类结果存储在 state.intent 中，后续节点根据此值进行路由。
 * </p>
 *
 * @author 曾家乐
 */
@Slf4j
@Component("intentClassifierNode")
@RequiredArgsConstructor
public class IntentClassifierNode implements WorkflowNode {

    /**
     * AI 聊天客户端，用于调用大语言模型进行意图分类
     */
    private final ChatClient.Builder chatClientBuilder;

    /**
     * 智能助手配置属性，包含意图分类的提示词模板
     */
    private final AgentProperties agentProperties;

    /**
     * 执行意图分类逻辑
     * <p>
     * 处理流程：
     * 1. 记录执行轨迹
     * 2. 构建意图分类提示词（系统提示词 + 用户消息）
     * 3. 调用 AI 模型获取分类结果
     * 4. 解析分类结果，设置到 state.intent
     * 5. 根据意图类型路由到对应的业务处理节点
     * </p>
     *
     * @param state 工作流状态对象
     * @return 下一个节点名称，根据意图类型路由
     */
    @Override
    public String execute(AgentState state) {
        log.debug("进入意图分类节点");
        state.addTrace("INTENT_CLASSIFIER");

        // 构建意图分类提示词并调用 AI 模型
        String intentResult = chatClientBuilder.build()
                .prompt()
                .system(agentProperties.getIntentPrompt())
                .user(state.getProcessedMessage())
                .call()
                .content();

        // 清理分类结果（去除首尾空白和换行）
        String intent = intentResult != null ? intentResult.trim().toUpperCase() : "CHITCHAT";
        state.setIntent(intent);

        log.info("意图分类结果: sessionId={}, intent={}", state.getSessionId(), intent);

        // 根据意图类型路由到对应的业务处理节点
        return routeByIntent(intent);
    }

    /**
     * 根据意图类型确定下一个执行节点
     * <p>
     * 路由规则：
     * - QUERY_PRODUCT → 商品对话节点（productDialogNode）
     * - ORDER → 订单对话节点（orderDialogNode）
     * - CHITCHAT → 闲聊节点（chitchatNode）
     * - TRANSFER_TO_HUMAN → 转人工节点（transferToHumanNode）
     * - 未知意图 → 闲聊节点（兜底处理）
     * </p>
     *
     * @param intent 意图分类结果
     * @return 下一个节点名称
     */
    private String routeByIntent(String intent) {
        return switch (intent) {
            case "QUERY_PRODUCT" -> "productDialogNode";
            case "ORDER" -> "orderDialogNode";
            case "QUERY_ORDERS" -> "orderHistoryNode";
            case "TRANSFER_TO_HUMAN" -> "transferToHumanNode";
            default -> "chitchatNode";
        };
    }
}
