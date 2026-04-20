package com.lenyan.lenaiagent.controller;

import com.lenyan.lenaiagent.agent.LenHealthAssistant;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 健康助手控制器
 */
@RestController
@RequestMapping("/ai/health")
public class HealthController {

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 流式调用 云医通健康助手
     *
     * @param message
     * @return
     */
    @GetMapping("/chat")
    public SseEmitter doChatWithHealthAssistant(String message) {
        LenHealthAssistant healthAssistant = new LenHealthAssistant(allTools, dashscopeChatModel);
        return healthAssistant.runStream(message);
    }
}
