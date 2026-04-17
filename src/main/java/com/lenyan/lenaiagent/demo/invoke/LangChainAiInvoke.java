package com.lenyan.lenaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Value;

public class LangChainAiInvoke {

    @Value("${qwen.api-key}")
    private  static String apiKey;

    public static void main(String[] args) {
        ChatLanguageModel model = QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName("qwen-max")
                .build();
        System.out.println("LangChainAi调用：" + model.chat("我是程序员lenyan"));
    }
}
