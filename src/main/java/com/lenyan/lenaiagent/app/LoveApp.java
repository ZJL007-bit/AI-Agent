package com.lenyan.lenaiagent.app;

import com.lenyan.lenaiagent.advisor.MyLoggerAdvisor;
import com.lenyan.lenaiagent.advisor.ProhibitedWordAdvisor;
import com.lenyan.lenaiagent.advisor.ReReadingAdvisor;
import com.lenyan.lenaiagent.chatmemory.MySQLChatMemory;
import com.lenyan.lenaiagent.chatmemory.MybatisPlusChatMemory;
import com.lenyan.lenaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.lenyan.lenaiagent.rag.QueryRewriter;
import com.lenyan.lenaiagent.service.ChatMemoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.client.advisor.api.Advisor;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    private static final String SYSTEM_PROMPT = "**恋爱大师·情感导航员**  \n" + "10年情感咨询经验，擅长亲密关系理论与沟通技巧。提供中立建议，保护隐私。通过情绪确认、需求拆解（3-5维度）、心理学理论（如非暴力沟通）解析问题，给出2种实操策略（如\"我句式\"对话模拟），引导关系边界建立。示例：\"遗忘纪念日可能涉及记忆模式/爱意表达方式差异，建议用'观察+感受'沟通\"。不评判道德、不做医疗建议，严守伦理规范。您的专属情感顾问，随时为您解惑。";
    private final ChatClient chatClient;


    /**
     * 基于PgVector的向量存储，用于持久化向量数据检索
     */
    @Resource
    private VectorStore pgVectorVectorStore;
    /**
     * 云端RAG增强检索顾问，用于接入外部知识库服务
     */
    @Resource
    private Advisor loveAppRagCloudAdvisor;


    /**
     * 查询重写器，用于优化用户输入以提升RAG检索准确率
     */
    @Resource
    private QueryRewriter queryRewriter;

    /**
     *  通过构造器注册chatClient, 设置系统提示词,初始化chatmenory通过mybatis实现上下问记忆,通过拦截器初始化违禁词
     */
    public LoveApp(ChatModel dashscopeChatModel, ChatMemoryService chatMemoryService
            /*, MybatisPlusChatMemory mybatisPluschatMemory ,MySQLChatMemory jdbcmysqlchatMemory*/) {

        //方法1:存储在MySQL数据库中，重启后数据依然存在，适合需要持久化对话记录的场景
        //ChatMemory chatMemory = new FileBasedChatMemory(System.getProperty("user.dir") + "/tmp/chat-memory");

        //方法2:使用基于内存的聊天记忆实现，数据仅保存在当前应用内存中，重启后丢失
        //ChatMemory chatMemory = new InMemoryChatMemory();

        //方法3:存储在MySQL数据库中，重启后数据依然存在，适合需要持久化对话记录的场景
        ChatMemory chatMemory = new MybatisPlusChatMemory(chatMemoryService);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory),
                        // 记录日志
                        new MyLoggerAdvisor(),
                        // 违禁词检测 - 从文件读取违禁词
                        new ProhibitedWordAdvisor(),
                        // 复读强化阅读能力
                        new ReReadingAdvisor()
                ).build();
    }

    /**
     * AI 恋爱对话功能只是实现了基本的同步聊天功（没有敏感词过滤、rag检索功能集成)
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    /**
     * AI 恋爱报告生成功能方法（实战结构化输出转成一个实体类）
     *
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        // 生成恋爱报告, 标题为{用户名}的恋爱报告，内容为建议列表转为一个实体类
        LoveReport loveReport = chatClient.prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call().entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

    /**
     * AI 基础对话（支持多轮对话记忆，,违禁词监测SSE 流式传输）
     * 实现功能"
     * 1.- 💬 情感问题咨询
     * 2.- 📅 约会建议与规划
     * 3.- 🗣️ 沟通技巧指导
     * 4.- ✨ 浪漫惊喜设计
     * 5.- 📊 恋爱报告生成
     * 6.- 🔍 RAG 知识库增强（恋爱心理学）
     * 7.- 🚫 违禁词过滤
     * 8.- 📝 多轮对话记忆
     *
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message) // 用户输入提示词
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor()) // 开启日志，便于观察效果
               // .advisors(loveAppRagCloudAdvisor)    // 应用增强检索服务（云知识库服务）
                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))  // rag应用 （基于 PgVector 向量存储）
                // 应用自定义的 RAG 检索增强服务（文档查询器 + 上下文增强器）
//                .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(
//                                loveAppVectorStore, "单身"))
                .tools(toolCallbackProvider) // 添加工具
                .stream()
                .content()
                .onErrorResume(ProhibitedWordAdvisor.ProhibitedWordException.class, ex -> {
                    log.warn("流式接口检测到违禁词: {}", ex.getMessage());
                    return Flux.just("您的输入包含敏感内容，请修改后重试");
                });
    }

    /**
     * 恋爱报告实体记录类
     * 用于封装 AI 生成的恋爱建议报告，包含报告标题和建议列表
     */
    record LoveReport(String title, List<String> suggestions) {
    }


    /**
     * 恋爱对话功能，结合知识库问答、RAG、自定义RAG服务
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        //提取并重写后的查询文本，用于提升RAG检索的准确性
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用知识库问答
               // .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                // 应用增强检索服务（云知识库服务）
                .advisors(loveAppRagCloudAdvisor)
                // rag应用 （基于 PgVector 向量存储）
                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                // 应用自定义的 RAG 检索增强服务（文档查询器 + 上下文增强器）
                .advisors(
                        LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(
                                pgVectorVectorStore, "单身"
                        )
                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    // AI 调用工具能力
    @Resource
    private ToolCallback[] allTools;

    /**
     * AI 恋爱报告功能（支持调用工具）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * 发送用户消息，关联会话ID以维持上下文记忆（保留最近10条）
     * 启用日志记录和MCP工具调用,支持日志大勇
     * 获取并返回AI响应内容
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


}
