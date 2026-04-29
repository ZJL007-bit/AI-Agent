package com.lenyan.lenaiagent.agent.coffee.service;

import java.util.List;

/**
 * 知识库服务接口
 * <p>
 * 提供基于 RAG（检索增强生成）的知识检索能力，
 * 从预置的咖啡知识文档中检索与用户问题相关的信息，
 * 作为 AI 模型的上下文参考，提升回复的准确性和专业性。
 * </p>
 *
 * @author 曾家乐
 */
public interface KnowledgeBaseService {

    /**
     * 根据用户查询检索相关知识片段
     * <p>
     * 调用向量数据库进行语义相似度检索，返回与用户问题最相关的知识片段。
     * 检索结果将作为 AI 模型的参考上下文，帮助生成更准确的回复。
     * </p>
     *
     * @param query 用户的查询内容
     * @return 与查询相关的知识片段列表，按相关性从高到低排序
     */
    List<String> search(String query);

    /**
     * 格式化检索结果为提示词上下文
     * <p>
     * 将检索到的知识片段拼接为结构化的文本，作为 AI 模型的参考信息
     * 嵌入到提示词模板中，格式如：
     * <pre>
     * 【参考知识】
     * 1. 拿铁是由浓缩咖啡和蒸汽牛奶按照1:2的比例...
     * 2. 美式咖啡是浓缩咖啡加水稀释...
     * </pre>
     * </p>
     *
     * @param query 用户的查询内容
     * @return 格式化后的知识上下文文本
     */
    String getFormattedContext(String query);
}
