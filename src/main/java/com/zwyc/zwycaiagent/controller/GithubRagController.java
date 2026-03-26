package com.zwyc.zwycaiagent.controller;

import com.zwyc.zwycaiagent.rag.service.GithubRagService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rag/github")
public class GithubRagController {

    private final GithubRagService githubRagService;

    public GithubRagController(GithubRagService githubRagService) {
        this.githubRagService = githubRagService;
    }

    @PostMapping("/load")
    public String loadDocs(
            @RequestParam String owner,
            @RequestParam String repo,
            @RequestParam(defaultValue = "main") String branch,
            @RequestParam(defaultValue = "/") String path
    ) {
        try {
            githubRagService.loadGithubDocsToVectorStore(owner, repo, branch, path);
            return "文档加载成功";
        } catch (Exception e) {
            return "文档加载失败：" + e.getMessage();
        }
    }
}
