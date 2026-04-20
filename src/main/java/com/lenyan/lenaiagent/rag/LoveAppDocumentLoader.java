package com.lenyan.lenaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 1. LoveAppDocumentLoader.java - 文档加载器
 * 加载 resources/document/ 目录下的所有 Markdown 文档，按水平分隔符切分，并提取文件名作为元数据标签。
 * 2. MyTokenTextSplitter.java - 文档切分器
 * 使用 TokenTextSplitter 将长文档按 Token 切分成小块，支持自定义切分参数（块大小、重叠量等）。
 * 3. MyKeywordEnricher.java - 关键词增强器
 * 调用 AI 模型自动为文档提取关键词，添加到文档元数据中，提升向量检索的准确率。
 * 4. LoveAppVectorStoreConfig.java - 向量存储配置
 * 初始化内存型向量数据库（SimpleVectorStore），完成文档加载→增强→存入向量的完整流程。
 * 5. QueryRewriter.java - 查询重写器
 * 在检索前用 AI 将用户口语化的问题重写为更适合检索的规范化查询语句。
 * 6. LoveAppContextualQueryAugmenterFactory.java - 查询增强工厂
 * 创建空上下文提示模板，当检索不到相关内容时，让 AI 回复"只能回答恋爱相关问题"。
 * 7. LoveAppRagCustomAdvisorFactory.java - 自定义 RAG 顾问工厂
 * 构建本地向量检索顾问，支持按状态（status）过滤文档、设置相似度阈值和返回数量。
 * 8. LoveAppRagCloudAdvisorConfig.java - 云端 RAG 配置
 * 配置阿里云 DashScope 云端知识库检索顾问，从云端索引"AI超级智能体"获取文档。
 * 9. documentreader/GitHubDocumentLoader.java - GitHub 文档加载器
 * 从 GitHub 仓库或 Gist 拉取 Markdown/代码文件，转换为 RAG 可用的文档格式。
 */
@Component
@Slf4j
public class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载多篇 Markdown 文档
     * @return
     */
    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                //提取文档倒数第 3 和第 2 个字作为标签
                String status = filename.substring(filename.length() - 6, filename.length() - 4);
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", filename)
                        .withAdditionalMetadata("status", status)
                        .build();
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(markdownDocumentReader.get());
            }
        } catch (IOException e) {
            log.error("Markdown 文档加载失败", e);
        }
        return allDocuments;
    }
}
