package com.lenyan.lenimagesearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ImageSearchTool {
    //日志
    private static final Logger log = LoggerFactory.getLogger(ImageSearchTool.class);

    // 替换为你的 Pexels API 密钥（需从官网申请）
    private static final String API_KEY = "jPmBcwWdQvE21TT1rMuP0KeqWhznCnSKwv1OAcOlrKCI3Oaa0AaOa39R";

    // Pexels 常规搜索接口（请以文档为准）
    private static final String API_URL = "https://api.pexels.com/v1/search";

    @Tool(description = "根据关键词从网络搜索图片，并返回 Markdown 格式的图片链接列表")
    public String searchImage(@ToolParam(description = "搜索关键词") String query) {
        try {
            log.info("正在搜索关键词 '{}' 的图片", query);
            
            // 获取中等尺寸图片的 URL 列表
            List<String> imageUrls = searchMediumImages(query);
            
            if (imageUrls == null || imageUrls.isEmpty()) {
                return "未找到相关图片。";
            }

            // 将图片 URL 转换为 Markdown 格式并拼接
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < imageUrls.size(); i++) {
                String markdownUrl = "![](" + imageUrls.get(i) + ")";
                sb.append(markdownUrl);
                // 可选：在每个图片后添加换行符，以便在支持 Markdown 渲染的环境中更好地显示
                // sb.append("\n"); 
                log.info("图片 {}: {}", (i + 1), markdownUrl);
            }
            
            log.info("搜索完成，共找到 {} 张图片", imageUrls.size());
            return sb.toString();
        } catch (Exception e) {
            log.error("搜索图片时发生错误", e);
            return "搜索图片时出错: " + e.getMessage();
        }
    }
    
    

    /**
     * 搜索中等尺寸的图片列表
     *
     * @param query
     * @return
     */
    public List<String> searchMediumImages(String query) {
        // 设置请求头（包含API密钥）
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", API_KEY);

        // 设置请求参数（仅包含query，可根据文档补充page、per_page等参数）
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);

        // 发送 GET 请求
        String response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .execute()
                .body();

        // 解析响应JSON（假设响应结构包含"photos"数组，每个元素包含"medium"字段）
        return JSONUtil.parseObj(response)
                .getJSONArray("photos")
                .stream()
                .map(photoObj -> (JSONObject) photoObj)
                .map(photoObj -> photoObj.getJSONObject("src"))
                .map(photo -> photo.getStr("medium"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}
