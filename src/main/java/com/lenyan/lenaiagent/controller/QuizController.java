package com.lenyan.lenaiagent.controller;

import com.lenyan.lenaiagent.agent.QuizAssistant;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
/**
 * Quiz 智慧问答智能体助手
 */
@RestController
@RequestMapping("/ai/quiz")
public class QuizController {

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;
    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/chat")
    public SseEmitter doChatWithquiz(String message) {
        QuizAssistant quizAssistant = new QuizAssistant(allTools, dashscopeChatModel);
        return quizAssistant.runStream(message);
    }
}
