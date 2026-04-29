package com.lenyan.lenaiagent.agent.coffee.workflow.node;

import com.lenyan.lenaiagent.agent.coffee.config.AgentProperties;
import com.lenyan.lenaiagent.agent.coffee.service.KnowledgeBaseService;
import com.lenyan.lenaiagent.agent.coffee.service.RecommendationService;
import com.lenyan.lenaiagent.agent.coffee.workflow.AgentState;
import com.lenyan.lenaiagent.agent.coffee.workflow.WorkflowNode;
import com.lenyan.lenaiagent.domain.CoffeeProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component("productDialogNode")
@RequiredArgsConstructor
public class ProductDialogNode implements WorkflowNode {

    private final ChatClient.Builder chatClientBuilder;
    private final AgentProperties agentProperties;
    private final KnowledgeBaseService knowledgeBaseService;
    private final RecommendationService recommendationService;

    @Override
    public String execute(AgentState state) {
        log.debug("进入商品对话节点");
        state.addTrace("PRODUCT_DIALOG");

        String userMessage = state.getProcessedMessage();

        // 检索知识库 + 商品数据，作为 AI 上下文
        String knowledgeContext = knowledgeBaseService.getFormattedContext(userMessage);

        // 判断是泛泛询问还是定向搜索
        boolean isGenericQuery = userMessage.length() <= 6
                || userMessage.contains("有什么") || userMessage.contains("菜单")
                || userMessage.contains("推荐") || userMessage.contains("哪些")
                || userMessage.contains("全部") || userMessage.contains("都有");

        List<CoffeeProduct> products;
        if (isGenericQuery) {
            products = recommendationService.getHotProducts();
        } else {
            products = recommendationService.recommend(userMessage);
            if (products.isEmpty()) {
                products = recommendationService.getHotProducts();
            }
        }

        String productList = products.stream()
                .map(p -> String.format(
                        "- [ID:%d] %s | 品类:%s | ¥%s | 库存:%d\n" +
                        "  成分: %s\n" +
                        "  描述: %s",
                        p.getId(),
                        p.getName(),
                        p.getCategory(),
                        p.getPrice().stripTrailingZeros().toPlainString(),
                        p.getStock(),
                        p.getIngredients() != null ? p.getIngredients() : "-",
                        p.getDescription() != null ? p.getDescription() : "-"
                ))
                .collect(Collectors.joining("\n"));

        if (productList.isEmpty()) {
            productList = "暂无在售商品";
        }

        String systemPrompt = agentProperties.getProductDialogPrompt()
                .replace("{knowledge}", knowledgeContext)
                .replace("{products}", productList);

        String response = chatClientBuilder.build()
                .prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();

        state.setResponse(response);
        log.info("商品对话完成: sessionId={}", state.getSessionId());
        return "END";
    }
}
