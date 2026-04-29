package com.lenyan.lenaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 咖啡店 QA 知识库文档加载器
 * <p>
 * 从 classpath:QA.csv 加载预置的咖啡相关问题-回答对，
 * 将每条 Q&A 转换为一个 Spring AI Document 并存入向量数据库，
 * 供闲聊节点进行 RAG 检索增强生成。
 * </p>
 *
 * @author 曾家乐
 */
@Slf4j
@Component
public class CoffeeQADocumentLoader {

    private final ResourceLoader resourceLoader;

    public CoffeeQADocumentLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 加载 QA.csv 中的所有问答对，转换为 Document 列表
     * <p>
     * CSV 格式：序号\t问题,回答
     * 每行包含一个咖啡相关的问题及其标准回答，
     * 文档文本格式为 "问题: {q}\n回答: {a}"，
     * 元数据中包含原始问题字段。
     * </p>
     *
     * @return 问答对文档列表
     */
    public List<Document> loadQAPairs() {
        List<Document> documents = new ArrayList<>();
        try {
            Resource resource = resourceLoader.getResource("classpath:QA.csv");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                boolean isHeader = true;
                while ((line = reader.readLine()) != null) {
                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }
                    String[] parts = line.split("\t", 2);
                    if (parts.length < 2) continue;

                    String qaPart = parts[1];
                    int firstComma = qaPart.indexOf(',');
                    if (firstComma < 0) continue;

                    String question = qaPart.substring(0, firstComma);
                    String answer = qaPart.substring(firstComma + 1);

                    // 去除回答两端的双引号
                    if (answer.startsWith("\"") && answer.endsWith("\"")) {
                        answer = answer.substring(1, answer.length() - 1);
                    }

                    String content = "问题: " + question + "\n回答: " + answer;
                    Document doc = new Document(content,
                            Map.of("question", question, "source", "QA.csv"));
                    documents.add(doc);
                }
            }
            log.info("成功加载 {} 条咖啡 QA 知识库文档", documents.size());
        } catch (Exception e) {
            log.error("QA.csv 文档加载失败", e);
        }
        return documents;
    }
}
