package com.norman.normanaiagent.rag.documentreader;

import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.ai.document.Document;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class GitHubDocumentLoader {

    private final GitHub gitHub;
    private final String owner;
    private final String repo;
    private final String branch;
    private String defaultBranch;

    public GitHubDocumentLoader(GitHub gitHub, String owner, String repo, String branch) {
        Assert.notNull(gitHub, "GitHub实例不能为空");
        Assert.notNull(owner, "仓库所有者不能为空");
        Assert.notNull(repo, "仓库名称不能为空");
        this.gitHub = gitHub;
        this.owner = owner;
        this.repo = repo;
        this.branch = branch != null ? branch : "main";
    }

    public static Builder builder() {
        return new Builder();
    }

    public Document loadDocument(String path) {
        try {
            return Optional.ofNullable(normalizePath(path))
                    .flatMap(this::loadContentSafely)
                    .map(this::createDocumentSafely)
                    .orElseThrow(() -> new RuntimeException("加载文档失败: " + path));
        } catch (Exception e) {
            log.error("加载文档失败: {}", path);
            throw new RuntimeException("加载文档失败: " + path, e);
        }
    }

    public List<Document> loadDocuments(String path) {
        try {
            List<GHContent> contents = loadDirectoryContentWithBranchFallback(normalizePath(path));
            return contents.stream()
                    .flatMap(content -> {
                        if (content.isFile()) {
                            try {
                                return Stream.of(createDocument(content));
                            } catch (Exception e) {
                                return Stream.empty();
                            }
                        } else if (content.isDirectory()) {
                            return loadDocuments(content.getPath()).stream();
                        }
                        return Stream.empty();
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("加载目录失败: {}", path);
            throw new RuntimeException("加载目录失败: " + path, e);
        }
    }

    private Optional<GHContent> loadContentSafely(String path) {
        try {
            return Optional.of(loadContentWithBranchFallback(path));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Document createDocumentSafely(GHContent content) {
        try {
            return createDocument(content);
        } catch (IOException e) {
            throw new RuntimeException("创建文档失败", e);
        }
    }

    private GHContent loadContentWithBranchFallback(String path) throws IOException {
        try {
            return tryLoadContent(path, branch);
        } catch (GHFileNotFoundException e) {
            String defaultBranch = getDefaultBranch();
            if (!branch.equals(defaultBranch)) {
                return tryLoadContent(path, defaultBranch);
            }
            throw e;
        }
    }

    private GHContent tryLoadContent(String path, String branchName) throws IOException {
        GHContent content = getRepository().getFileContent(path, branchName);
        Assert.isTrue(content.isFile(), "路径必须指向文件");
        return content;
    }

    private List<GHContent> loadDirectoryContentWithBranchFallback(String path) throws IOException {
        try {
            return getRepository().getDirectoryContent(path.isEmpty() ? "/" : path, branch);
        } catch (GHFileNotFoundException e) {
            String defaultBranch = getDefaultBranch();
            if (!branch.equals(defaultBranch)) {
                return getRepository().getDirectoryContent(path.isEmpty() ? "/" : path, defaultBranch);
            }
            throw e;
        }
    }

    private String getDefaultBranch() throws IOException {
        return Optional.ofNullable(defaultBranch)
                .orElseGet(() -> {
                    try {
                        return defaultBranch = getRepository().getDefaultBranch();
                    } catch (IOException e) {
                        throw new RuntimeException("获取默认分支失败", e);
                    }
                });
    }

    private String normalizePath(String path) {
        return (path == null || path.isEmpty()) ? "" : path.startsWith("/") ? path.substring(1) : path;
    }

    public Map<String, Object> getRepositoryInfo() {
        try {
            GHRepository repository = getRepository();
            this.defaultBranch = repository.getDefaultBranch();
            return Map.of(
                    "name", Objects.toString(repository.getName(), ""),
                    "description", Objects.toString(repository.getDescription(), ""),
                    "stars", repository.getStargazersCount(),
                    "forks", repository.getForksCount(),
                    "language", Objects.toString(repository.getLanguage(), ""),
                    "defaultBranch", this.defaultBranch,
                    "htmlUrl", Optional.ofNullable(repository.getHtmlUrl()).map(Object::toString).orElse(""),
                    "cloneUrl", Objects.toString(repository.getHttpTransportUrl(), "")
            );
        } catch (IOException e) {
            log.error("获取仓库信息失败: {}/{}", owner, repo);
            throw new RuntimeException("获取仓库信息失败: " + owner + "/" + repo, e);
        }
    }

    private GHRepository getRepository() throws IOException {
        return gitHub.getRepository(owner + "/" + repo);
    }

    private Document createDocument(GHContent content) throws IOException {
        return new Document(
                content.getContent(),
                Map.of(
                        "github_file_name", content.getName(),
                        "github_file_path", content.getPath(),
                        "github_file_sha", content.getSha(),
                        "github_html_url", content.getHtmlUrl()
                )
        );
    }

    public static class Builder {
        private GitHub gitHub;
        private String owner;
        private String repo;
        private String branch;

        public Builder gitHub(GitHub gitHub) {
            this.gitHub = gitHub;
            return this;
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder repo(String repo) {
            this.repo = repo;
            return this;
        }

        public Builder branch(String branch) {
            this.branch = branch;
            return this;
        }

        public GitHubDocumentLoader build() {
            return new GitHubDocumentLoader(gitHub, owner, repo, branch);
        }
    }
}
