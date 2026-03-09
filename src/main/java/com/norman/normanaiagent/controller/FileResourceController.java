package com.norman.normanaiagent.controller;

import com.norman.normanaiagent.constant.FileConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FileResourceController {

    private static final Logger log = LoggerFactory.getLogger(FileResourceController.class);

    private final Path pdfDir = Paths.get(FileConstant.FILE_SAVE_DIR, "pdf");
    private final Path htmlDir = Paths.get(FileConstant.FILE_SAVE_DIR, "html");
    private final Path fileDir = Paths.get(FileConstant.FILE_SAVE_DIR, "file");

    @GetMapping("/pdf/list")
    public List<Map<String, Object>> listPdfFiles() throws IOException {
        log.info("正在获取PDF文件列表，目录: {}", pdfDir);
        return listFiles(pdfDir, "pdf");
    }

    @GetMapping("/pdf/{filename:.+}")
    public ResponseEntity<Resource> downloadPdfFile(@PathVariable String filename) {
        log.info("请求下载PDF文件: {}", filename);
        return downloadFile(pdfDir, filename, "pdf");
    }

    @GetMapping("/html/list")
    public List<Map<String, Object>> listHtmlFiles() throws IOException {
        log.info("正在获取HTML文件列表，目录: {}", htmlDir);
        return listFiles(htmlDir, "html");
    }

    @GetMapping("/html/{filename:.+}")
    public ResponseEntity<Resource> downloadHtmlFile(@PathVariable String filename) {
        log.info("请求查看HTML文件: {}", filename);
        return downloadFile(htmlDir, filename, "html");
    }

    @GetMapping("/file/list")
    public List<Map<String, Object>> listGeneralFiles() throws IOException {
        log.info("正在获取一般文件列表，目录: {}", fileDir);
        return listFiles(fileDir, "file");
    }

    @GetMapping("/file/{filename:.+}")
    public ResponseEntity<Resource> downloadGeneralFile(@PathVariable String filename) {
        log.info("请求下载一般文件: {}", filename);
        return downloadFile(fileDir, filename, "file");
    }

    private List<Map<String, Object>> listFiles(Path directory, String type) throws IOException {
        File dir = directory.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
            log.info("创建目录: {}", directory);
        }
        return Files.list(directory)
                .filter(Files::isRegularFile)
                .map(path -> {
                    Map<String, Object> fileInfo = new HashMap<>();
                    String fileName = path.getFileName().toString();
                    fileInfo.put("name", fileName);
                    fileInfo.put("url", "/api/files/" + type + "/" + fileName);
                    try {
                        fileInfo.put("size", Files.size(path));
                        fileInfo.put("lastModified", Files.getLastModifiedTime(path).toMillis());
                        fileInfo.put("type", determineFileType(fileName));
                        fileInfo.put("display", shouldDisplayInline(determineFileType(fileName)) ? "inline" : "download");
                    } catch (IOException e) {
                        log.warn("获取文件信息失败: {}", fileName, e);
                    }
                    return fileInfo;
                })
                .collect(Collectors.toList());
    }

    private ResponseEntity<Resource> downloadFile(Path directory, String filename, String fileType) {
        try {
            Path filePath = directory.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                log.warn("文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            if (!resource.isReadable()) {
                log.warn("文件不可读: {}", filePath);
                return ResponseEntity.badRequest().build();
            }
            if (!filePath.startsWith(directory.normalize())) {
                log.warn("安全错误：请求路径超出允许范围: {}", filePath);
                return ResponseEntity.badRequest().build();
            }

            String contentType = determineContentType(filePath, fileType);
            log.info("提供文件: {}, 类型: {}", filename, contentType);
            String disposition = shouldDisplayInline(contentType) ? "inline" : "attachment";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + resource.getFilename() + "\"");

            if (contentType.equals("text/html")) {
                headers.add("X-Content-Type-Options", "nosniff");
                headers.add("Content-Security-Policy",
                        "default-src 'self'; img-src 'self' https://* data:; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'; frame-src *; connect-src *;");
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("URL格式错误: {}", filename, e);
            return ResponseEntity.badRequest().build();
        }
    }

    private String determineFileType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return "pdf";
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "html";
        if (lower.endsWith(".txt")) return "text";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif")) return "image";
        return "other";
    }

    private String determineContentType(Path filePath, String fileType) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".pdf")) return "application/pdf";
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) return "text/html";
        if (fileName.endsWith(".txt")) return "text/plain";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".gif")) return "image/gif";
        if (fileName.endsWith(".json")) return "application/json";
        if (fileName.endsWith(".xml")) return "application/xml";
        if (fileName.endsWith(".csv")) return "text/csv";
        try {
            String probed = Files.probeContentType(filePath);
            if (probed != null && !probed.isEmpty()) return probed;
        } catch (IOException e) {
            log.warn("无法确定文件类型: {}", filePath);
        }
        return "application/octet-stream";
    }

    private boolean shouldDisplayInline(String contentType) {
        return contentType.equals("application/pdf") ||
                contentType.startsWith("image/") ||
                contentType.equals("text/html");
    }
}
