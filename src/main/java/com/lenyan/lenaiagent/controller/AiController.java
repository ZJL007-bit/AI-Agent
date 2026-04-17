package com.lenyan.lenaiagent.controller;


import com.lenyan.lenaiagent.agent.LenHealthAssistant;
import com.lenyan.lenaiagent.agent.LenManus;
import com.lenyan.lenaiagent.agent.QuizAssistant;
import com.lenyan.lenaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 同步调用 AI 恋爱大师应用
     * 特点：阻塞式等待完整响应，适合简单问答或对实时性要求不高的场景。
     *
     * @param message 用户消息
     * @param chatId 会话ID
     * @return 完整的AI回复字符串
     */
    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    /**
     * SSE 流式调用 AI 恋爱大师应用 (Flux<String>)
     * 特点：返回 Reactor Flux 流，Spring WebFlux 自动处理为 text/event-stream。
     * 优势：非阻塞，背压支持好，适合响应式编程栈。前端需处理 SSE 格式或纯文本流。
     *
     * @param message 用户消息
     * @param chatId 会话ID
     * @return 字符串类型的响应式流
     */
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId);
    }

    /**
     * SSE 流式调用 AI 恋爱大师应用 (Flux<ServerSentEvent<String>>)
     * 特点：显式构建 ServerSentEvent 对象，可以携带更多 SSE 元数据（如 ID、事件类型、重试时间等）。
     * 优势：比纯字符串流更规范，适合需要精细控制 SSE 协议行为的场景。
     *
     * @param message 用户消息
     * @param chatId 会话ID
     * @return ServerSentEvent 类型的响应式流
     */
    @GetMapping(value = "/love_app/chat/server_sent_event", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * SSE 流式调用 AI 恋爱大师应用 (SseEmitter)
     * 特点：使用 Spring MVC 传统的 SseEmitter，基于 Servlet 线程模型。
     * 优势：兼容传统 Spring MVC 应用，无需引入 WebFlux 依赖即可实现流式输出。
     * 注意：需要手动管理订阅和异常处理，长时间连接需注意超时设置。
     *
     * @param message 用户消息
     * @param chatId 会话ID
     * @return SseEmitter 对象
     */
    @GetMapping(value = "/love_app/chat/sse_emitter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doChatWithLoveAppServerSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L); // 3 分钟超时
        
        // 获取 Flux 响应式数据流并且直接通过订阅推送给 SseEmitter
        loveApp.doChatByStream(message, chatId)
                .subscribe(
                    chunk -> {
                        try {
                            sseEmitter.send(chunk);
                        } catch (IOException e) {
                            sseEmitter.completeWithError(e);
                        }
                    },
                    sseEmitter::completeWithError,
                    sseEmitter::complete
                );
        
        return sseEmitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        LenManus lenManus = new LenManus(allTools, dashscopeChatModel);
        return lenManus.runStream(message);
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/quiz/chat")
    public SseEmitter doChatWithquiz(String message) {
        QuizAssistant quizAssistant = new QuizAssistant(allTools, dashscopeChatModel);
        return quizAssistant.runStream(message);
    }

    /**
     * 流式调用 云医通健康助手
     *
     * @param message
     * @return
     */
    @GetMapping("/health/chat")
    public SseEmitter doChatWithHealthAssistant(String message) {
        LenHealthAssistant healthAssistant = new LenHealthAssistant(allTools, dashscopeChatModel);
        return healthAssistant.runStream(message);
    }
}
