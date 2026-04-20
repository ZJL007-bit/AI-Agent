package com.lenyan.lenaiagent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工具注册配置类
 */
@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    /**
     * 注册所有AI工具
     */
    @Bean
    public ToolCallback[] allTools(EmailSendingTool emailSendingTool) {
        // 实例化所有工具
        return ToolCallbacks.from(
                new FileOperationTool(),          // 文件操作工具
                new WebSearchTool(searchApiKey),  // 网络搜索工具
                new WebScrapingTool(),            // 网页抓取工具
                new ResourceDownloadTool(),       // 资源下载工具
                new TerminalOperationTool(),      // 终端操作工具
                new PDFGenerationTool(),          // PDF生成工具
                new HtmlGenerationTool(),         // HTML生成工具
                new ImageSearchTool(),            // 图片搜索工具
                new DateTimeTool(),               // 日期时间工具
                new TerminateTool(),              // 终止工具
                emailSendingTool          // 邮件发送工具
        );
    }
}
