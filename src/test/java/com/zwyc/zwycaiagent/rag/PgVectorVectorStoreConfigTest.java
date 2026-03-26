package com.zwyc.zwycaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PgVectorVectorStoreConfigTest {

    @Resource(name = "pgVectorVectorStore")
    private VectorStore pgVectorVectorStore;

    @Test
    void pgVectorStore() {
        List<Document> documents = List.of(
                new Document("Lexora平台有什么用？学习英语啊，背单词啊", Map.of("meta1", "meta1")),
                new Document("Zwickyc的原创学习英语平台...."),
                new Document("Zwickyc偷吃Doro大王的橘子", Map.of("meta2", "meta2")));
        // Add the documents to PGVector
        pgVectorVectorStore.add(documents);
        // Retrieve documents similar to a query
        List<Document> results = this.pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("怎么学英语啊").topK(5).build());
        Assertions.assertNotNull(results);
    }
}