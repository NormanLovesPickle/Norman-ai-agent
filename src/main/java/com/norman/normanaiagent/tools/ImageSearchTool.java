package com.norman.normanaiagent.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImageSearchTool {

    private static final Logger log = LoggerFactory.getLogger(ImageSearchTool.class);
    private static final String API_URL = "https://api.pexels.com/v1/search";
    private static final int TIMEOUT = 10000;

    private final String apiKey;

    public ImageSearchTool(String apiKey) {
        this.apiKey = apiKey != null ? apiKey : "";
    }

    @Tool(description = "Search for images from the web using keywords")
    public String searchImage(@ToolParam(description = "Search query keyword") String query) {
        if (StrUtil.isBlank(query)) {
            return "搜索关键词不能为空";
        }
        if (StrUtil.isBlank(apiKey)) {
            return "图片搜索失败: 请配置 pexels.api-key";
        }
        try {
            log.info("正在搜索图片: {}", query);
            List<String> imageUrls = searchMediumImages(query);
            if (imageUrls.isEmpty()) {
                return "未找到与 '" + query + "' 相关的图片";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < imageUrls.size(); i++) {
                sb.append("![](").append(imageUrls.get(i)).append(")");
                if (i < imageUrls.size() - 1) {
                    sb.append("\n\n");
                }
            }
            log.info("搜索结果: {} 张图片", imageUrls.size());
            return sb.toString();
        } catch (Exception e) {
            log.error("图片搜索出错", e);
            return "图片搜索出错: " + e.getMessage();
        }
    }

    private List<String> searchMediumImages(String query) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", apiKey);
            Map<String, Object> params = new HashMap<>();
            params.put("query", query);
            params.put("per_page", 5);
            params.put("locale", "zh-CN");

            HttpResponse httpResponse = HttpUtil.createGet(API_URL)
                    .addHeaders(headers)
                    .form(params)
                    .timeout(TIMEOUT)
                    .execute();

            if (httpResponse.getStatus() != 200) {
                log.error("API请求失败，状态码: {}, 响应内容: {}", httpResponse.getStatus(), httpResponse.body());
                return new ArrayList<>();
            }

            String response = httpResponse.body();
            if (!JSONUtil.isJson(response)) {
                log.error("API返回非JSON格式响应: {}", response);
                return new ArrayList<>();
            }

            JSONObject jsonObject = JSONUtil.parseObj(response);
            if (jsonObject.containsKey("error")) {
                log.error("API返回错误: {}", jsonObject.getStr("error"));
                return new ArrayList<>();
            }
            if (!jsonObject.containsKey("photos") || jsonObject.getJSONArray("photos") == null) {
                log.error("API响应中未找到photos数组或为null");
                return new ArrayList<>();
            }

            JSONArray photosArray = jsonObject.getJSONArray("photos");
            if (photosArray.isEmpty()) {
                return new ArrayList<>();
            }

            return photosArray.stream()
                    .map(photoObj -> (JSONObject) photoObj)
                    .map(photoObj -> {
                        if (!photoObj.containsKey("src")) return null;
                        return photoObj.getJSONObject("src");
                    })
                    .filter(srcObj -> srcObj != null)
                    .map(srcObj -> {
                        if (srcObj.containsKey("medium")) return srcObj.getStr("medium");
                        if (srcObj.containsKey("small")) return srcObj.getStr("small");
                        if (srcObj.containsKey("original")) return srcObj.getStr("original");
                        return null;
                    })
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());
        } catch (HttpException e) {
            log.error("HTTP请求出错: {}", e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("搜索图片过程中出错", e);
            return new ArrayList<>();
        }
    }
}
