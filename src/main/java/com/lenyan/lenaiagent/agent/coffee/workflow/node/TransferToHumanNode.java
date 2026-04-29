package com.lenyan.lenaiagent.agent.coffee.workflow.node;

import com.lenyan.lenaiagent.agent.coffee.config.AgentProperties;
import com.lenyan.lenaiagent.agent.coffee.workflow.AgentState;
import com.lenyan.lenaiagent.agent.coffee.workflow.WorkflowNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 转人工节点
 * <p>
 * 处理用户请求转人工客服的意图。当用户明确要求转人工，
 * 或智能助手无法处理复杂问题时，路由到此节点。
 * 返回人工客服的联系方式（电话和在线链接），引导用户获取人工服务。
 * </p>
 * <p>
 * 触发条件：
 * - 用户主动请求转人工（如"我要找人工客服"）
 * - 意图分类结果为 TRANSFER_TO_HUMAN
 * - 后续可扩展：敏感词严重等级过高、用户情绪异常等
 * </p>
 *
 * @author 曾家乐
 */
@Slf4j
@Component("transferToHumanNode")
@RequiredArgsConstructor
public class TransferToHumanNode implements WorkflowNode {

    /**
     * 智能助手配置属性，包含转人工的引导语和联系方式
     */
    private final AgentProperties agentProperties;

    /**
     * 执行转人工逻辑
     * <p>
     * 处理流程：
     * 1. 记录执行轨迹
     * 2. 从配置中获取人工客服的联系方式
     * 3. 组装转人工提示消息（引导语 + 电话 + 在线链接）
     * 4. 将消息设置到 AgentState 并返回 "END"
     * </p>
     * <p>
     * 返回消息示例：
     * <pre>
     * 好的，正在为您转接人工客服，请稍等...
     *
     * 您可以拨打人工客服电话：400-123-4567
     *
     * 或点击链接联系在线客服：https://xxx.kefu.com
     * </pre>
     * </p>
     *
     * @param state 工作流状态对象
     * @return "END"（转人工节点为终端节点）
     */
    @Override
    public String execute(AgentState state) {
        log.debug("进入转人工节点");
        state.addTrace("TRANSFER_TO_HUMAN");

        // 从配置中获取人工客服联系方式
        String phone = agentProperties.getTransferHumanPhone();
        String link = agentProperties.getTransferHumanLink();

        // 组装转人工提示消息
        StringBuilder sb = new StringBuilder();
        sb.append(agentProperties.getTransferHumanPhrase()).append("\n\n");
        sb.append("您可以拨打人工客服电话：").append(phone).append("\n\n");
        sb.append("或点击链接联系在线客服：").append(link);

        state.setResponse(sb.toString());
        log.info("用户请求转人工: sessionId={}", state.getSessionId());
        return "END";
    }
}
