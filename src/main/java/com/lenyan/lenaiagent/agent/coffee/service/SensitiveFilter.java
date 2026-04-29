package com.lenyan.lenaiagent.agent.coffee.service;

/**
 * 敏感词过滤服务接口
 * <p>
 * 提供内容安全过滤能力，在将用户输入传递给 AI 模型前
 * 以及将 AI 回复返回给用户前进行敏感词检测和过滤，
 * 确保对话内容的合规性和安全性。
 * </p>
 *
 * @author 曾家乐
 */
public interface SensitiveFilter {

    /**
     * 过滤文本中的敏感词
     * <p>
     * 扫描输入文本，将其中的敏感词替换为 "***"。
     * 如果文本中不包含敏感词，则原样返回。
     * </p>
     *
     * @param text 需要过滤的原始文本
     * @return 过滤后的文本，敏感词已被替换为 "***"
     */
    String filter(String text);

    /**
     * 检测文本中是否包含敏感词
     * <p>
     * 仅检测是否包含敏感词，不做替换处理。
     * 通常在预处理阶段使用，用于判断是否需要特殊处理
     * （如记录日志、转人工等）。
     * </p>
     *
     * @param text 需要检测的文本
     * @return true 表示包含敏感词，false 表示不包含
     */
    boolean containsSensitiveWord(String text);

    /**
     * 重新加载敏感词列表
     * <p>
     * 从数据库和配置文件两个来源重新加载敏感词到内存中，
     * 使最新的敏感词配置即时生效，无需重启服务。
     * 加载顺序：先加载文件中的敏感词，再加载数据库中的敏感词，数据库优先级更高。
     * </p>
     */
    void reloadWordList();
}
