package com.lenyan.lenaiagent.controller;
import com.lenyan.lenaiagent.agent.LenManus;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Manus 超级智能体控制器
 */
@RestController
@RequestMapping("/ai/manus")
public class ManusController {

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 流式调用 Manus 超级智能体
     * * 核心能力
     * - 🔍 网络搜索与信息检索
     * - 📄 文件操作（创建、读取、写入）
     * - 🌐 网页抓取与解析
     * - 📥 资源下载
     * - 💻 终端命令执行
     * - 📊 PDF 文档生成
     * - 🎨 HTML 页面生成
     * - ⏰ 日期时间查询
     * - 🛑 智能终止交互
     * @param message
     * @return
     */
    @GetMapping("/chat")
    public SseEmitter doChatWithManus(String message) {
        LenManus lenManus = new LenManus(allTools, dashscopeChatModel);
        return lenManus.runStream(message);
    }

}
