package com.lenyan.lenaiagent.agent.coffee.workflow.node;

import com.lenyan.lenaiagent.agent.coffee.service.OrderService;
import com.lenyan.lenaiagent.agent.coffee.workflow.AgentState;
import com.lenyan.lenaiagent.agent.coffee.workflow.WorkflowNode;
import com.lenyan.lenaiagent.domain.CoffeeOrder;
import com.lenyan.lenaiagent.domain.CoffeeOrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component("orderHistoryNode")
@RequiredArgsConstructor
public class OrderHistoryNode implements WorkflowNode {

    private final OrderService orderService;

    @Override
    public String execute(AgentState state) {
        log.debug("进入订单历史查询节点");
        state.addTrace("ORDER_HISTORY");

        List<CoffeeOrder> orders = orderService.getOrdersBySessionId(state.getSessionId());

        if (orders.isEmpty()) {
            state.setResponse("您当前还没有订单记录哦～输入有什么咖啡可以浏览菜单下单。");
            return "END";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d HH:mm:ss");

        StringBuilder sb = new StringBuilder();
        sb.append("📋 您的订单记录：\n\n");

        for (int i = 0; i < orders.size(); i++) {
            CoffeeOrder o = orders.get(i);
            String statusText = switch (o.getStatus() != null ? o.getStatus() : "") {
                case "PENDING" -> "⏳ 待确认";
                case "CONFIRMED" -> "✅ 已确认";
                case "PREPARING" -> "🛠️ 制作中";
                case "COMPLETED" -> "✔️ 已完成";
                case "CANCELLED" -> "❌ 已取消";
                default -> o.getStatus();
            };

            String total = o.getTotalAmount() != null
                    ? o.getTotalAmount().stripTrailingZeros().toPlainString()
                    : "0";

            String productSummary = "";
            int itemCount = 0;
            if (o.getItems() != null && !o.getItems().isEmpty()) {
                itemCount = o.getItems().stream().mapToInt(CoffeeOrderItem::getQuantity).sum();
                productSummary = o.getItems().stream()
                        .map(it -> it.getProductName() + " x" + it.getQuantity())
                        .collect(Collectors.joining(", "));
            }

            String timeStr = o.getCreateTime() != null ? sdf.format(o.getCreateTime()) : "-";

            sb.append(String.format("%d. ID：%d\n", i + 1, o.getId()));
            sb.append(String.format("   订单编号：%s\n", o.getOrderNo()));
            sb.append(String.format("   商品概要：%s\n", productSummary));
            sb.append(String.format("   数量：%d\n", itemCount));
            sb.append(String.format("   金额：¥%s\n", total));
            sb.append(String.format("   状态：%s\n", statusText));
            sb.append(String.format("   下单时间：%s\n", timeStr));
            if (o.getRemark() != null && !o.getRemark().isEmpty()) {
                sb.append(String.format("   备注：%s\n", o.getRemark()));
            }
            sb.append("\n");
        }

        sb.append("如需查看订单详情或有其他问题，随时告诉我～");
        state.setResponse(sb.toString());
        log.info("订单历史查询完成: sessionId={}, count={}", state.getSessionId(), orders.size());
        return "END";
    }
}
