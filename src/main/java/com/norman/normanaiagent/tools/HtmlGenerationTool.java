package com.norman.normanaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.norman.normanaiagent.constant.FileConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlGenerationTool {

    private static final Logger log = LoggerFactory.getLogger(HtmlGenerationTool.class);

    private final String HTML_DIR = FileConstant.FILE_SAVE_DIR + "/html";

    public HtmlGenerationTool() {
        FileUtil.mkdir(HTML_DIR);
    }

    @Tool(description = "Generate an attractive HTML file with animations and responsive design")
    public String generateHtml(
            @ToolParam(description = "The title of the HTML document") String title,
            @ToolParam(description = "The HTML content (body part, should NOT include the title)") String content,
            @ToolParam(description = "Optional filename (without extension), will generate if empty") String filename
    ) {
        try {
            String safeFilename = getSafeFilename(filename);
            String htmlFilename = safeFilename + ".html";
            Path htmlPath = Paths.get(HTML_DIR, htmlFilename);
            String htmlContent = buildHtmlDocument(title, content);
            FileUtil.writeString(htmlContent, htmlPath.toString(), StandardCharsets.UTF_8);
            log.info("HTML文件生成成功: {}", htmlPath);
            String downloadLink = "/api/files/html/" + htmlFilename;
            return String.format("HTML生成成功！[点击查看HTML页面](%s)", downloadLink);
        } catch (Exception e) {
            log.error("HTML生成失败", e);
            return "HTML生成失败: " + e.getMessage();
        }
    }

    @Tool(description = "Generate an HTML file with embedded pages using iframes")
    public String generateHtmlWithEmbeddedPages(
            @ToolParam(description = "The title of the HTML document") String title,
            @ToolParam(description = "The HTML content with optional {{embed:URL}} placeholders") String content,
            @ToolParam(description = "List of URLs to embed (optional if content contains embed tags)") List<String> embedUrls,
            @ToolParam(description = "Optional filename (without extension)") String filename
    ) {
        try {
            String safeFilename = getSafeFilename(filename);
            String htmlFilename = safeFilename + ".html";
            Path htmlPath = Paths.get(HTML_DIR, htmlFilename);
            String processedContent = processEmbedTags(content, embedUrls);
            String htmlContent = buildHtmlWithEmbeddedPages(title, processedContent);
            FileUtil.writeString(htmlContent, htmlPath.toString(), StandardCharsets.UTF_8);
            log.info("HTML文件(带内嵌页面)生成成功: {}", htmlPath);
            String downloadLink = "/api/files/html/" + htmlFilename;
            return String.format("HTML生成成功！[点击查看HTML页面](%s)", downloadLink);
        } catch (Exception e) {
            log.error("HTML生成失败", e);
            return "HTML生成失败: " + e.getMessage();
        }
    }

    private String processEmbedTags(String content, List<String> embedUrls) {
        if (content == null) {
            content = "";
        }
        StringBuilder processedContent = new StringBuilder();
        Pattern embedPattern = Pattern.compile("\\{\\{embed:([^}]+)\\}\\}");
        Matcher matcher = embedPattern.matcher(content);
        int lastEnd = 0;
        int embedCount = 0;

        while (matcher.find()) {
            processedContent.append(content.substring(lastEnd, matcher.start()));
            String url = matcher.group(1);
            processedContent.append(generateEmbedHtml(url, embedCount++));
            lastEnd = matcher.end();
        }
        processedContent.append(content.substring(lastEnd));

        if (embedUrls != null && !embedUrls.isEmpty() && embedCount == 0) {
            processedContent.append("\n<div class=\"additional-embeds\">\n");
            for (int i = 0; i < embedUrls.size(); i++) {
                String url = embedUrls.get(i);
                if (url != null && !url.trim().isEmpty()) {
                    processedContent.append(generateEmbedHtml(url, i));
                }
            }
            processedContent.append("</div>\n");
        }
        return processedContent.toString();
    }

    private String generateEmbedHtml(String url, int index) {
        if (url == null || url.trim().isEmpty()) {
            return "";
        }
        StringBuilder embedHtml = new StringBuilder();
        embedHtml.append("    <div class=\"embed-item my-4\">\n");
        embedHtml.append("        <div class=\"embed-container\">\n");
        embedHtml.append("            <iframe src=\"").append(escapeHtml(url.trim())).append("\" class=\"embedded-iframe\" loading=\"lazy\" allowfullscreen></iframe>\n");
        embedHtml.append("            <div class=\"iframe-loader\" id=\"loader-").append(index).append("\">\n");
        embedHtml.append("                <div class=\"loader-spinner\"></div>\n");
        embedHtml.append("                <div>正在加载页面...</div>\n");
        embedHtml.append("            </div>\n");
        embedHtml.append("        </div>\n");
        embedHtml.append("        <div class=\"embed-source\">源: <a href=\"").append(escapeHtml(url.trim())).append("\" target=\"_blank\" rel=\"noopener\">").append(escapeHtml(url.trim())).append("</a></div>\n");
        embedHtml.append("    </div>\n");
        return embedHtml.toString();
    }

    private String buildHtmlWithEmbeddedPages(String title, String contentWithEmbeds) {
        String processedContent = processContentForTitle(contentWithEmbeds, title);
        String extraStyles =
                "        .embed-item { margin: 2rem 0; border: 1px solid var(--border-color); border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px var(--shadow-color); transition: transform 0.3s ease, box-shadow 0.3s ease; }\n" +
                "        .embed-item:hover { transform: translateY(-5px); box-shadow: 0 8px 15px var(--shadow-color); }\n" +
                "        .embed-container { position: relative; height: 600px; width: 100%; background-color: var(--bg-color); }\n" +
                "        .embedded-iframe { width: 100%; height: 100%; border: none; }\n" +
                "        .iframe-loader { position: absolute; top: 0; left: 0; width: 100%; height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center; background-color: var(--bg-color); z-index: 2; transition: opacity 0.5s ease, visibility 0.5s ease; }\n" +
                "        .iframe-loader .loader-spinner { margin-bottom: 10px; }\n" +
                "        .embed-source { padding: 10px 15px; font-size: 0.9rem; border-top: 1px solid var(--border-color); background-color: var(--card-bg); }\n" +
                "        .embed-source a { color: var(--primary-color); word-break: break-all; text-decoration: none; }\n" +
                "        .embed-source a:hover { text-decoration: underline; }\n" +
                "        .additional-embeds { margin-top: 3rem; padding-top: 2rem; border-top: 1px solid var(--border-color); }\n" +
                "        @media (max-width: 768px) { .embed-container { height: 400px; } }\n";
        String extraScripts =
                "            document.querySelectorAll('.embedded-iframe').forEach(function(iframe) {\n" +
                "                const container = iframe.closest('.embed-container');\n" +
                "                const loaderId = container.querySelector('.iframe-loader').id;\n" +
                "                const loader = document.getElementById(loaderId);\n" +
                "                iframe.addEventListener('load', function() {\n" +
                "                    setTimeout(function() {\n" +
                "                        loader.style.opacity = '0';\n" +
                "                        setTimeout(function() { loader.style.visibility = 'hidden'; }, 500);\n" +
                "                    }, 1000);\n" +
                "                });\n" +
                "                iframe.addEventListener('error', function() { loader.innerHTML = '<div>加载页面失败</div>'; });\n" +
                "            });\n";
        return buildFullHtmlDocument(title, processedContent, extraStyles, extraScripts);
    }

    private String buildFullHtmlDocument(String title, String content, String extraStyles, String extraScripts) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>\n")
                .append("<html lang=\"zh-CN\">\n")
                .append("<head>\n")
                .append("    <meta charset=\"UTF-8\">\n")
                .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
                .append("    <title>").append(escapeHtml(title)).append("</title>\n")
                .append("    <style>\n")
                .append("        :root { --primary-color: #3498db; --secondary-color: #2c3e50; --accent-color: #e74c3c; --bg-color: #f8f9fa; --text-color: #333; --card-bg: #ffffff; --border-color: #ddd; --shadow-color: rgba(0,0,0,0.1); --animation-duration: 1.2s; --animation-delay: 0.1s; }\n")
                .append("        [data-theme=\"dark\"] { --primary-color: #61dafb; --secondary-color: #f1f1f1; --accent-color: #ff6b6b; --bg-color: #121212; --text-color: #e0e0e0; --card-bg: #1e1e1e; --border-color: #444; --shadow-color: rgba(255,255,255,0.05); }\n")
                .append("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.8; color: var(--text-color); background-color: var(--bg-color); padding-top: 30px; opacity: 1; transition: background-color 0.3s ease, color 0.3s ease; scroll-behavior: smooth; }\n\n");
        if (extraStyles != null && !extraStyles.isEmpty()) {
            htmlBuilder.append(extraStyles);
        }
        htmlBuilder.append("        .loader-spinner { width: 40px; height: 40px; border: 3px solid var(--border-color); border-top-color: var(--primary-color); border-radius: 50%; animation: spin 1s linear infinite; }\n")
                .append("        @keyframes spin { to { transform: rotate(360deg); } }\n")
                .append("        .page-loader { position: fixed; top: 0; left: 0; width: 100%; height: 100%; display: flex; justify-content: center; align-items: center; background: var(--bg-color); z-index: 9999; transition: opacity 0.5s ease; }\n")
                .append("        .theme-toggle { position: fixed; top: 20px; right: 20px; cursor: pointer; z-index: 1000; padding: 8px; border-radius: 50%; background: var(--card-bg); box-shadow: 0 2px 8px var(--shadow-color); }\n")
                .append("        .container { max-width: 900px; margin: 0 auto; padding: 20px; }\n")
                .append("        .content { margin-top: 20px; }\n")
                .append("        .slide-up { animation: slideUp 0.6s ease-out; }\n")
                .append("        @keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }\n")
                .append("        .fade-in { animation: fadeIn 0.8s ease-out; }\n")
                .append("        @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }\n")
                .append("    </style>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("    <div class=\"page-loader\" id=\"pageLoader\"><div class=\"loader-spinner\"></div></div>\n")
                .append("    <div class=\"theme-toggle\" id=\"themeToggle\" title=\"切换明暗主题\">\n")
                .append("        <svg xmlns=\"http://www.w3.org/2000/svg\" width=\"20\" height=\"20\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><path d=\"M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z\"></path></svg>\n")
                .append("    </div>\n")
                .append("    <div class=\"container fade-in\">\n")
                .append("        <h1 class=\"slide-up\">").append(escapeHtml(title)).append("</h1>\n")
                .append("        <div class=\"content\">\n").append(content).append("\n")
                .append("        </div>\n")
                .append("    </div>\n")
                .append("    <script>\n")
                .append("        document.addEventListener('DOMContentLoaded', function() {\n")
                .append("            const themeToggle = document.getElementById('themeToggle');\n")
                .append("            const prefersDarkMode = window.matchMedia('(prefers-color-scheme: dark)').matches;\n")
                .append("            const savedTheme = localStorage.getItem('theme');\n")
                .append("            if (savedTheme === 'dark' || (!savedTheme && prefersDarkMode)) { document.documentElement.setAttribute('data-theme', 'dark'); }\n")
                .append("            themeToggle.addEventListener('click', function() {\n")
                .append("                const currentTheme = document.documentElement.getAttribute('data-theme');\n")
                .append("                if (currentTheme === 'dark') { document.documentElement.removeAttribute('data-theme'); localStorage.setItem('theme', 'light'); }\n")
                .append("                else { document.documentElement.setAttribute('data-theme', 'dark'); localStorage.setItem('theme', 'dark'); }\n")
                .append("            });\n")
                .append("            setTimeout(function() {\n")
                .append("                document.getElementById('pageLoader').style.opacity = '0';\n")
                .append("                setTimeout(() => { document.getElementById('pageLoader').style.display = 'none'; }, 500);\n")
                .append("            }, 800);\n");
        if (extraScripts != null && !extraScripts.isEmpty()) {
            htmlBuilder.append(extraScripts);
        }
        htmlBuilder.append("        });\n")
                .append("    </script>\n")
                .append("</body>\n")
                .append("</html>");
        return htmlBuilder.toString();
    }

    private String getSafeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "html_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        }
        return filename.trim().replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_");
    }

    private String buildHtmlDocument(String title, String bodyContent) {
        String processedContent = processContentForTitle(bodyContent, title);
        return buildFullHtmlDocument(title, processedContent, "", "");
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private boolean similarText(String str1, String str2) {
        if (str1 == null || str2 == null) return false;
        String normalized1 = str1.replaceAll("\\s+", " ").trim().toLowerCase();
        String normalized2 = str2.replaceAll("\\s+", " ").trim().toLowerCase();
        if (normalized1.equals(normalized2)) return true;
        int len1 = normalized1.length();
        int len2 = normalized2.length();
        if (Math.abs(len1 - len2) > Math.max(len1, len2) * 0.2) return false;
        int distance = levenshteinDistance(normalized1, normalized2);
        int threshold = Math.min(3, Math.max(len1, len2) / 10);
        return distance <= threshold;
    }

    private int levenshteinDistance(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];
        for (int i = 0; i <= len1; i++) {
            for (int j = 0; j <= len2; j++) {
                if (i == 0) dp[i][j] = j;
                else if (j == 0) dp[i][j] = i;
                else dp[i][j] = Math.min(
                        dp[i - 1][j - 1] + (str1.charAt(i - 1) == str2.charAt(j - 1) ? 0 : 1),
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                );
            }
        }
        return dp[len1][len2];
    }

    private String processContentForTitle(String bodyContent, String title) {
        String processedContent = bodyContent;
        if (title != null && !title.trim().isEmpty()) {
            String plainTitle = title.trim();
            try {
                Pattern h1Pattern = Pattern.compile("(?i)(?s)<h1[^>]*>([\\s\\S]*?)</h1>");
                Matcher matcher = h1Pattern.matcher(processedContent);
                StringBuilder resultContent = new StringBuilder();
                int lastEnd = 0;
                while (matcher.find()) {
                    String h1Content = matcher.group(1).trim();
                    String cleanedH1 = h1Content.replaceAll("<[^>]+>", "").trim();
                    String cleanedTitle = plainTitle.replaceAll("<[^>]+>", "").trim();
                    if (cleanedH1.equalsIgnoreCase(cleanedTitle) || similarText(cleanedH1, cleanedTitle)) {
                        resultContent.append(processedContent.substring(lastEnd, matcher.start()));
                    } else {
                        resultContent.append(processedContent.substring(lastEnd, matcher.end()));
                    }
                    lastEnd = matcher.end();
                }
                if (lastEnd < processedContent.length()) {
                    resultContent.append(processedContent.substring(lastEnd));
                }
                processedContent = resultContent.toString();
            } catch (Exception e) {
                log.warn("移除HTML标题时出错: {}", e.getMessage());
            }
        }
        return processedContent;
    }
}
