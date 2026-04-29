package com.lenyan.lenaiagent.agent.coffee.workflow.node;

import com.lenyan.lenaiagent.agent.coffee.dto.OrderItemRequest;
import com.lenyan.lenaiagent.agent.coffee.service.OrderService;
import com.lenyan.lenaiagent.agent.coffee.service.RecommendationService;
import com.lenyan.lenaiagent.agent.coffee.workflow.AgentState;
import com.lenyan.lenaiagent.agent.coffee.workflow.WorkflowNode;
import com.lenyan.lenaiagent.domain.CoffeeOrder;
import com.lenyan.lenaiagent.domain.CoffeeProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component("orderDialogNode")
@RequiredArgsConstructor
public class OrderDialogNode implements WorkflowNode {

    private final OrderService orderService;
    private final RecommendationService recommendationService;

    private static final Pattern QUANTITY_PATTERN = Pattern.compile(
            "(一|1|二|两|2|三|3|四|4|五|5|六|6|七|7|八|8|九|9|十|10)\\s*(杯|个|份|件|瓶|罐)"
    );

    private static final java.util.Map<String, Integer> NUMBER_MAP = java.util.Map.ofEntries(
            java.util.Map.entry("一", 1), java.util.Map.entry("1", 1),
            java.util.Map.entry("二", 2), java.util.Map.entry("两", 2), java.util.Map.entry("2", 2),
            java.util.Map.entry("三", 3), java.util.Map.entry("3", 3),
            java.util.Map.entry("四", 4), java.util.Map.entry("4", 4),
            java.util.Map.entry("五", 5), java.util.Map.entry("5", 5),
            java.util.Map.entry("六", 6), java.util.Map.entry("6", 6),
            java.util.Map.entry("七", 7), java.util.Map.entry("7", 7),
            java.util.Map.entry("八", 8), java.util.Map.entry("8", 8),
            java.util.Map.entry("九", 9), java.util.Map.entry("9", 9),
            java.util.Map.entry("十", 10), java.util.Map.entry("10", 10)
    );

    @Override
    public String execute(AgentState state) {
        log.debug("进入订单对话节点");
        state.addTrace("ORDER_DIALOG");

        String userMessage = state.getProcessedMessage();

        // 1. 提取数量
        int quantity = extractQuantity(userMessage);

        // 2. 匹配商品：遍历所有在售商品，检查名称或别名是否出现在用户消息中
        List<CoffeeProduct> allProducts = recommendationService.getHotProducts();
        // 同时也要考虑可能不在热门列表中的商品，用搜索补充
        List<CoffeeProduct> matched = matchProducts(userMessage, allProducts);
        if (matched.isEmpty()) {
            // 尝试用 searchProducts 补充匹配
            for (String keyword : extractKeywords(userMessage)) {
                List<CoffeeProduct> found = recommendationService.searchProducts(keyword);
                for (CoffeeProduct p : found) {
                    if (matched.stream().noneMatch(m -> m.getId().equals(p.getId()))) {
                        matched.add(p);
                    }
                }
            }
        }

        if (matched.isEmpty()) {
            // 没有匹配到商品，列出热门商品提示用户
            StringBuilder sb = new StringBuilder("抱歉，我没找到您要的商品。目前有以下热门商品可选：\n");
            for (CoffeeProduct p : allProducts) {
                sb.append(String.format("- %s  ¥%s\n", p.getName(),
                        p.getPrice().stripTrailingZeros().toPlainString()));
            }
            sb.append("\n请告诉我您想要哪个？");
            state.setResponse(sb.toString());
            return "END";
        }

        // 3. 取第一个匹配的商品创建订单
        CoffeeProduct chosen = matched.get(0);
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(chosen.getId());
        item.setProductName(chosen.getName());
        item.setQuantity(quantity);
        items.add(item);

        try {
            CoffeeOrder order = orderService.createOrder(
                    state.getSessionId(),
                    state.getUserId(),
                    items,
                    ""
            );

            // 4. 组装订单确认信息
            BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : chosen.getPrice().multiply(BigDecimal.valueOf(quantity));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d HH:mm:ss");
            String timeStr = order.getCreateTime() != null ? sdf.format(order.getCreateTime()) : sdf.format(new Date());
            String productSummary = chosen.getName() + " × " + quantity;

            String response = String.format(
                    "☕ 订单已生成，请确认：\n\n" +
                            "📋 订单ID：%d\n" +
                            "📋 订单编号：%s\n" +
                            "🍵 商品概要：%s\n" +
                            "📦 数量：%d\n" +
                            "💰 金额：¥%s\n" +
                            "📌 状态：待确认\n" +
                            "🕐 下单时间：%s\n\n" +
                            "如需确认请回复「确认」，如需取消请回复「取消」。",
                    order.getId(),
                    order.getOrderNo(),
                    productSummary,
                    quantity,
                    total.stripTrailingZeros().toPlainString(),
                    timeStr
            );

            state.setResponse(response);
            // 标记可支付，前端展示支付按钮
            state.putContext("orderId", order.getId());
            state.putContext("orderNo", order.getOrderNo());
            state.putContext("orderTotal", total.stripTrailingZeros().toPlainString());
            state.putContext("paymentEnabled", true);
            state.putContext("orderStatus", order.getStatus());
            state.putContext("orderCreateTime", order.getCreateTime());
            state.putContext("orderProductSummary", productSummary);
            state.putContext("orderItemCount", quantity);
        } catch (Exception e) {
            log.error("订单创建失败: {}", e.getMessage(), e);
            state.setResponse("下单失败：" + e.getMessage() + "，请稍后重试或联系人工客服。");
        }

        log.info("订单对话完成: sessionId={}", state.getSessionId());
        return "END";
    }

    /**
     * 从用户消息中提取数量，默认返回 1
     */
    private int extractQuantity(String message) {
        Matcher m = QUANTITY_PATTERN.matcher(message);
        if (m.find()) {
            String numStr = m.group(1);
            return NUMBER_MAP.getOrDefault(numStr, 1);
        }
        return 1;
    }

    /**
     * 匹配商品：两阶段策略。
     * 第一阶段：名称精确包含匹配，按名称长度降序（长名称更精确，如"香草拿铁"胜过"拿铁"）。
     * 第二阶段：别名匹配（仅在无名称匹配时启用），按匹配到的别名字符串长度降序。
     */
    private List<CoffeeProduct> matchProducts(String message, List<CoffeeProduct> products) {
        String lowerMsg = message.toLowerCase();

        // ---- 第一阶段：名称匹配 ----
        List<CoffeeProduct> nameMatches = products.stream()
                .filter(p -> lowerMsg.contains(p.getName().toLowerCase()))
                .sorted(java.util.Comparator.comparingInt((CoffeeProduct p) -> p.getName().length()).reversed())
                .collect(Collectors.toList());

        if (!nameMatches.isEmpty()) {
            return nameMatches;
        }

        // ---- 第二阶段：别名匹配 ----
        // 记录每个商品的最佳别名匹配长度
        List<Object[]> aliasMatches = new ArrayList<>(); // [CoffeeProduct, matchLength]
        for (CoffeeProduct p : products) {
            if (p.getAlias() == null) continue;
            int bestLen = 0;
            for (String alias : p.getAlias().split(";")) {
                String t = alias.trim().toLowerCase();
                if (!t.isEmpty() && lowerMsg.contains(t) && t.length() > bestLen) {
                    bestLen = t.length();
                }
            }
            if (bestLen > 0) {
                aliasMatches.add(new Object[]{p, bestLen});
            }
        }
        aliasMatches.sort((a, b) -> Integer.compare((Integer) b[1], (Integer) a[1]));
        return aliasMatches.stream().map(a -> (CoffeeProduct) a[0]).collect(Collectors.toList());
    }

    /**
     * 从消息中提取可能的关键词用于搜索
     */
    private String[] extractKeywords(String message) {
        // 简单分词：取2-4字词组作为搜索关键词
        List<String> keywords = new ArrayList<>();
        String cleaned = message.replaceAll("[，。！？、\\s]+", "");
        for (int len = 4; len >= 2; len--) {
            for (int i = 0; i + len <= cleaned.length(); i++) {
                String sub = cleaned.substring(i, i + len);
                // 过滤纯数字/符号
                if (!sub.matches("[0-9]+") && !sub.matches("[的了吗呢啊吧]*")) {
                    keywords.add(sub);
                }
            }
        }
        return keywords.toArray(new String[0]);
    }
}
