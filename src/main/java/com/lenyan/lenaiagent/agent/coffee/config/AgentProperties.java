package com.lenyan.lenaiagent.agent.coffee.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 咖啡智能助手配置属性类
 * <p>
 * 从 application.yml 中读取以 "coffee-agent" 为前缀的配置项，
 * 集中管理咖啡智能助手运行所需的各种参数，包括 AI 模型配置、
 * 工作流提示词、敏感词文件路径、转人工联系方式等。
 * </p>
 *
 * @author 曾家乐
 */
@Data
@Component
@ConfigurationProperties(prefix = "agent.coffee-shop")
public class AgentProperties {

    /**
     * AI 模型名称，用于指定调用哪个大语言模型
     * 例如："qwen-plus"、"glm-4" 等
     */
    private String modelName;

    /**
     * 意图分类的提示词模板，用于指导 AI 识别用户意图
     * 支持的意图类型包括：QUERY_PRODUCT（查询商品）、ORDER（下单）、
     * CHITCHAT（闲聊）、TRANSFER_TO_HUMAN（转人工）等
     */
    private String intentPrompt;

    /**
     * 商品对话的提示词模板，用于指导 AI 回答商品相关问题
     * 包括商品推荐、详情介绍、对比分析等场景
     */
    private String productDialogPrompt;

    /**
     * 订单对话的提示词模板，用于指导 AI 处理下单、修改订单等操作
     * 包括引导用户选择商品、确认订单、收集定制需求等
     */
    private String orderDialogPrompt;

    /**
     * 闲聊对话的提示词模板，用于指导 AI 进行日常闲聊
     * 保持对话友好自然，适时引导用户回到咖啡相关话题
     */
    private String chitchatPrompt;

    /**
     * 敏感词文件路径，指向 classpath 下的敏感词文件
     * 例如："coffee-prohibited-words.txt"
     */
    private String sensitiveWordsFile;

    /**
     * 转人工时的引导语，告知用户即将转接人工客服
     * 例如："好的，正在为您转接人工客服，请稍等..."
     */
    private String transferHumanPhrase;

    /**
     * 人工客服电话号码，提供给用户作为电话联系方式
     */
    private String transferHumanPhone;

    /**
     * 在线客服链接，提供给用户作为在线联系方式
     * 可以是网页客服链接或第三方客服平台链接
     */
    private String transferHumanLink;

    /**
     * 知识库文档路径，用于 RAG 检索增强生成
     * 指向咖啡产品知识文档所在位置
     */
    private String knowledgeBasePath;
}
