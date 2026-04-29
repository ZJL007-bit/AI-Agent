package com.lenyan.lenaiagent.agent.coffee.service.impl;

import com.lenyan.lenaiagent.agent.coffee.config.AgentProperties;
import com.lenyan.lenaiagent.agent.coffee.service.SensitiveFilter;
import com.lenyan.lenaiagent.mapper.CoffeeSensitiveWordMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 敏感词过滤服务实现类
 * <p>
 * 实现了 {@link SensitiveFilter} 接口，提供敏感词检测和过滤的核心逻辑。
 * 敏感词来源有两个：
 * <ul>
 *     <li>数据库：管理员通过后台动态添加的敏感词，优先级更高</li>
 *     <li>配置文件：classpath 下的文本文件，包含预置的敏感词，以 # 开头的行为注释</li>
 * </ul>
 * 两个来源的敏感词在启动时合并到内存中的 Set 集合，支持通过接口触发热更新。
 * </p>
 * <p>
 * 当前过滤策略为简单字符串替换（敏感词 → "***"），
 * 后续可升级为基于 DFA（确定有限自动机）算法实现更高效的大规模敏感词过滤。
 * </p>
 *
 * @author 曾家乐
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveFilterImpl implements SensitiveFilter {

    /**
     * 敏感词 Mapper，从数据库查询敏感词数据
     */
    private final CoffeeSensitiveWordMapper sensitiveWordMapper;

    /**
     * 智能助手配置属性，包含敏感词文件路径等配置
     */
    private final AgentProperties agentProperties;

    /**
     * 内存中的敏感词集合，使用 HashSet 存储以实现 O(1) 的查询性能
     * 在应用启动时从数据库和文件加载，支持热更新
     */
    private Set<String> sensitiveWords = new HashSet<>();

    /**
     * 应用启动时自动加载敏感词
     * <p>
     * 标注了 @PostConstruct 注解，在 Bean 初始化完成后自动调用，
     * 确保服务可用之前敏感词列表已经加载到内存中。
     * </p>
     */
    @PostConstruct
    public void init() {
        reloadWordList();
    }

    /**
     * 过滤文本中的敏感词
     * <p>
     * 遍历内存中的敏感词集合，将文本中所有匹配的敏感词替换为 "***"。
     * 如果输入为 null 或空字符串，直接返回原值。
     * </p>
     *
     * @param text 需要过滤的原始文本
     * @return 过滤后的文本，敏感词已被替换为 "***"
     */
    @Override
    public String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String result = text;
        for (String word : sensitiveWords) {
            if (result.contains(word)) {
                result = result.replace(word, "***");
            }
        }
        return result;
    }

    /**
     * 检测文本中是否包含敏感词
     * <p>
     * 遍历内存中的敏感词集合，检查文本中是否存在任何敏感词。
     * 只要发现一个匹配项即返回 true，不做替换处理。
     * </p>
     *
     * @param text 需要检测的文本
     * @return true 表示包含敏感词，false 表示不包含
     */
    @Override
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (String word : sensitiveWords) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 重新加载敏感词列表
     * <p>
     * 从两个来源加载敏感词到内存中：
     * <ol>
     *     <li>数据库：查询 coffee_sensitive_word 表中未删除的敏感词</li>
     *     <li>配置文件：读取 classpath 下的敏感词文件，忽略空行和 # 开头的注释行</li>
     * </ol>
     * 两个来源的敏感词合并到同一个 Set 中，数据库和文件的词均可生效。
     * 加载完成后打印日志记录敏感词总数。
     * </p>
     */
    @Override
    public void reloadWordList() {
        Set<String> words = new HashSet<>();

        // 从数据库加载敏感词
        try {
            List<String> dbWords = sensitiveWordMapper.getAllWords();
            if (dbWords != null) {
                words.addAll(dbWords);
            }
        } catch (Exception e) {
            log.warn("从数据库加载敏感词失败: {}", e.getMessage());
        }

        // 从配置文件加载敏感词
        try {
            ClassPathResource resource = new ClassPathResource(agentProperties.getSensitiveWordsFile());
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    // 跳过空行和注释行（以 # 开头）
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                        words.add(trimmed);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("从文件加载敏感词失败: {}", e.getMessage());
        }

        // 原子替换内存中的敏感词集合
        this.sensitiveWords = words;
        log.info("敏感词加载完成，共 {} 个", words.size());
    }
}
