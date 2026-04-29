package com.lenyan.lenaiagent.agent.coffee.service.impl;

import com.lenyan.lenaiagent.agent.coffee.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库服务实现类
 * <p>
 * 实现了 {@link KnowledgeBaseService} 接口，基于向量数据库（PgVector）进行语义检索。
 * 将用户查询转换为向量嵌入，在向量空间中查找最相似的知识片段，
 * 利用 RAG（检索增强生成）技术提升 AI 回复的准确性和专业性。
 * </p>
 * <p>
 * 工作原理：
 * 1. 知识文档在应用启动时被分块、嵌入向量并存储到 PgVector
 * 2. 用户查询时，将查询文本转换为向量
 * 3. 在向量空间中执行相似度搜索，返回最相关的知识片段
 * 4. 将检索结果格式化为上下文，嵌入到 AI 提示词中
 * </p>
 *
 * @author 曾家乐
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    /**
     * Spring AI 提供的向量存储接口，底层使用 PgVector
     * 用于存储文档的向量嵌入并执行相似度搜索
     */
    private final VectorStore vectorStore;

    /**
     * 根据用户查询检索相关知识片段
     * <p>
     * 实现逻辑：
     * 1. 构建 SearchRequest，指定查询文本和返回结果数量
     * 2. 调用向量存储的相似度搜索接口
     * 3. 提取每个搜索结果的内容文本并返回
     * </p>
     *
     * @param query 用户的查询内容
     * @return 与查询相关的知识片段列表，按相关性从高到低排序
     */
    @Override
    public List<String> search(String query) {
        log.debug("知识库检索: query={}", query);
        // 构建搜索请求，默认返回最相关的3条结果
        SearchRequest request = SearchRequest.builder().query(query).topK(3).build();
        List<Document> results = vectorStore.similaritySearch(request);
        // 提取文档内容文本
        return results.stream()
                .map(Document::getText)
                .collect(Collectors.toList());
    }

    /**
     * 格式化检索结果为提示词上下文
     * <p>
     * 实现逻辑：
     * 1. 调用 search 方法获取相关知识片段
     * 2. 如果没有检索到相关内容，返回空字符串
     * 3. 将知识片段按序号拼接为结构化文本
     * </p>
     * 输出格式示例：
     * <pre>
     * 【参考知识】
     * 1. 拿铁是由浓缩咖啡和蒸汽牛奶按照1:2的比例...
     * 2. 美式咖啡是浓缩咖啡加水稀释...
     * </pre>
     *
     * @param query 用户的查询内容
     * @return 格式化后的知识上下文文本，无结果时返回空字符串
     */
    @Override
    public String getFormattedContext(String query) {
        try {
            List<String> results = search(query);
            if (results.isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("【参考知识】\n");
            for (int i = 0; i < results.size(); i++) {
                sb.append(i + 1).append(". ").append(results.get(i)).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("知识库检索失败，降级为空上下文: {}", e.getMessage());
            return "";
        }
    }
}
