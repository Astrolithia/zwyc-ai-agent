package com.zwyc.zwycaiagent.rag.service;

import com.alibaba.cloud.ai.document.DocumentParser;
import com.alibaba.cloud.ai.reader.github.GitHubDocumentReader;
import com.alibaba.cloud.ai.reader.github.GitHubResource;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GithubRagService {

    @Value("${spring.ai.alibaba.document.reader.github.token}")
    private String token;

    private final VectorStore vectorStore;

    public GithubRagService(@Qualifier("pgVectorVectorStore") VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void loadGithubDocsToVectorStore(String owner, String repo, String branch, String path) {
        // 1. 批量读取整个目录下的所有文件（递归扫描子目录）
        List<GitHubResource> resources = GitHubResource.builder().gitHubToken(token).owner(owner).repo(repo).branch(branch).path(path)       // 目录路径，会递归扫描
                .buildBatch();       // ← 批量构建，返回 List<GitHubResource>

        // buildBatch 之后立即过滤，只保留文本类文件
        List<String> allowedExtensions = List.of(".md", ".txt", ".py", ".ipynb", ".java", ".js", ".ts", ".json", ".yaml", ".yml", ".csv");

        List<GitHubResource> filteredResources = resources.stream()
                .filter(r -> {
                    String name = r.getText().getName().toLowerCase();
                    return allowedExtensions.stream().anyMatch(name::endsWith);
                })
                .toList();
        System.out.println("原始文件数：" + resources.size() + "，过滤后：" + filteredResources.size());

        // 2. 创建 DocumentParser（解析文本内容）
        // MarkdownDocumentParser 专门解析 md 文件（需额外依赖）
        // 如果没有，用通用的 TextDocumentParser 替代：
//        var parser = new org.springframework.ai.reader.tika.TikaDocumentReader(null);
        // 推荐直接用下面这个简单实现：
        DocumentParser textParser = (inputStream) -> {
            try {
                String content = new String(inputStream.readAllBytes());
                return List.of(new Document(content));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        // 3. 创建 Reader（批量模式）
        GitHubDocumentReader reader = new GitHubDocumentReader(filteredResources, textParser);

        // 4. 读取所有文档
        List<Document> documents = reader.get();

        // 5. 分割文本
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> splitDocs = splitter.apply(documents);

        // 过滤内容为空或含 null 字节的文档
        List<Document> cleanDocs = splitDocs.stream().filter(doc -> doc.getText() != null && !doc.getText().isBlank()).map(doc -> {
            String cleaned = doc.getText().replace("\u0000", "");
            doc.getMetadata(); // metadata 保持不变
            return new Document(cleaned, doc.getMetadata());
        }).toList();

        // 6. 分批写入向量库（每批最多 10 条）
        int batchSize = 10;
        for (int i = 0; i < cleanDocs.size(); i += batchSize) {
            int end = Math.min(i + batchSize, cleanDocs.size());
            vectorStore.add(cleanDocs.subList(i, end));
            System.out.println("已写入第 " + (i / batchSize + 1) + " 批");
        }

        System.out.println("全部完成，共加载 " + cleanDocs.size() + " 个文档块到向量库");
    }
}