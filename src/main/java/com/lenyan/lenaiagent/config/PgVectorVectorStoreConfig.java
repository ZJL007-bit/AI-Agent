package com.lenyan.lenaiagent.config;

import com.lenyan.lenaiagent.rag.CoffeeQADocumentLoader;
import com.lenyan.lenaiagent.rag.LoveAppDocumentLoader;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Slf4j
@Configuration
public class PgVectorVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private CoffeeQADocumentLoader coffeeQADocumentLoader;

    @Bean
    @Lazy
    public VectorStore pgVectorVectorStore(@Qualifier("postgresJdbcTemplate") JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        try {
            VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                    .dimensions(1536)
                    .distanceType(COSINE_DISTANCE)
                    .indexType(HNSW)
                    .initializeSchema(true)
                    .schemaName("public")
                    .vectorTableName("vector_store")
                    .maxDocumentBatchSize(10000)
                    .build();

            List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
            if (documents != null && !documents.isEmpty()) {
                vectorStore.add(documents);
                log.info("已成功将 {} 个恋爱大师文档加载到向量存储中", documents.size());
            }

            List<Document> qaDocuments = coffeeQADocumentLoader.loadQAPairs();
            if (qaDocuments != null && !qaDocuments.isEmpty()) {
                vectorStore.add(qaDocuments);
                log.info("已成功将 {} 个咖啡 QA 文档加载到向量存储中", qaDocuments.size());
            }

            return vectorStore;
        } catch (Exception e) {
            log.error("初始化 PgVectorVectorStore 失败：{}", e.getMessage(), e);
            throw new RuntimeException("无法初始化 PgVectorVectorStore", e);
        }
    }
}
