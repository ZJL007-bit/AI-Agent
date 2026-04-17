package com.lenyan.lenaiagent.tools;

import com.lenyan.lenaiagent.constant.FileConstant;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * HTML内嵌页面生成工具测试类
 * 用于测试带有iframe内嵌页面的HTML页面生成
 * <p>
 * 功能测试点：
 * 1. 单个页面内嵌功能
 * 2. 多个页面内嵌功能
 * 3. 百度页面内嵌测试方法
 * 4. 内嵌页面加载处理和响应式显示
 */
public class HtmlEmbeddingToolTest {

    /**
     * 主方法，测试HTML内嵌页面生成功能
     */
    public static void main(String[] args) {
        HtmlGenerationTool htmlTool = new HtmlGenerationTool();

        System.out.println("=== HTML内嵌页面生成工具测试 ===\n");

        // 确保HTML目录存在
        String htmlDir = FileConstant.FILE_SAVE_DIR + "/html";
        new File(htmlDir).mkdirs();
        System.out.println("HTML文件目录: " + htmlDir);

        // 测试单个页面内嵌
        testSinglePageEmbedding(htmlTool);

        // 测试多个页面内嵌
        testMultiplePageEmbedding(htmlTool);

        System.out.println("\n=== 测试结束 ===");
        System.out.println("生成的HTML文件位于: " + htmlDir);
    }

    /**
     * 测试单个页面内嵌功能
     */
    private static void testSinglePageEmbedding(HtmlGenerationTool htmlTool) {
        System.out.println("\n1. 测试单个页面内嵌功能:");

        String title = "单个页面内嵌测试";
        String content = "<p>这是一个测试单个页面内嵌的示例。下面将显示内嵌的百度页面：</p>\n" +
                "{{embed:https://mbd.baidu.com/newspage/data/dtlandingsuper?nid=dt_5035641807220252494}}\n" +
                "<p>上面是一个内嵌的百度页面，展示了如何将外部内容无缝集成到我们的文章中。</p>";

        String result = htmlTool.generateHtmlWithEmbeddedPages(title, content, null, "single_page_embed_test");
        System.out.println(result);

        // 验证文件是否存在及内容
        Path htmlFile = Paths.get(FileConstant.FILE_SAVE_DIR, "html", "single_page_embed_test.html");
        validateHtmlFile(htmlFile, "iframe", "内嵌的百度页面", true);
    }

    /**
     * 测试多个页面内嵌功能
     */
    private static void testMultiplePageEmbedding(HtmlGenerationTool htmlTool) {
        System.out.println("\n2. 测试多个页面内嵌功能:");

        String title = "多个页面内嵌测试";
        String content = "<h2>政府网站导航</h2>\n" +
                "<p>首先，让我们看看中国政府网站：</p>\n" +
                "{{embed:https://www.gov.cn/}}\n" +
                "<p>这是中国政府的官方网站，提供了最新的政策信息和公共服务。</p>\n\n" +
                "<h2>天气预报服务</h2>\n" +
                "<p>接下来是美国国家气象局的API文档：</p>\n" +
                "{{embed:https://www.weather.gov/documentation/services-web-api}}\n" +
                "<p>这个API提供了详细的天气数据，可以用于开发天气相关的应用。</p>\n\n" +
                "<h2>新闻资讯</h2>\n" +
                "<p>最后，这是一个百度新闻页面：</p>\n" +
                "{{embed:https://mbd.baidu.com/newspage/data/dtlandingsuper?nid=dt_5035641807220252494}}\n" +
                "<p>通过这个示例，我们可以看到如何在文章中自然地融入多个外部页面。</p>";

        String result = htmlTool.generateHtmlWithEmbeddedPages(title, content, null, "multiple_pages_embed_test");
        System.out.println(result);

        // 验证文件是否存在及内容
        Path htmlFile = Paths.get(FileConstant.FILE_SAVE_DIR, "html", "multiple_pages_embed_test.html");
        validateHtmlFile(htmlFile, "iframe src=", "政府网站导航", true);
    }


    /**
     * 验证生成的HTML文件
     *
     * @param filePath        HTML文件路径
     * @param contentToCheck1 要检查的内容1
     * @param contentToCheck2 要检查的内容2
     * @param shouldExist     文件是否应该存在
     * @return 验证是否通过
     */
    private static boolean validateHtmlFile(Path filePath, String contentToCheck1, String contentToCheck2, boolean shouldExist) {
        File file = filePath.toFile();
        boolean exists = file.exists();

        if (exists != shouldExist) {
            System.out.println("✗ 文件" + (shouldExist ? "不存在" : "不应该存在") + ": " + filePath);
            return false;
        }

        if (!exists) {
            return !shouldExist;
        }

        System.out.println("✓ 文件存在: " + filePath);

        // 检查文件内容
        try {
            String content = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            boolean containsContent1 = content.contains(contentToCheck1);
            boolean containsContent2 = content.contains(contentToCheck2);

            if (containsContent1 && containsContent2) {
                System.out.println("✓ 文件内容正确，含有所需内容");
                return true;
            } else {
                System.out.println("✗ 文件内容不符合预期！");
                System.out.println("  - 是否含有 '" + contentToCheck1 + "': " + containsContent1);
                System.out.println("  - 是否含有 '" + contentToCheck2 + "': " + containsContent2);
                return false;
            }
        } catch (IOException e) {
            System.out.println("✗ 读取文件内容失败: " + e.getMessage());
            return false;
        }
    }
} 