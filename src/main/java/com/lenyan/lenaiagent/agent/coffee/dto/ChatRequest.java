package com.lenyan.lenaiagent.agent.coffee.dto;

import lombok.Data;

/**
 * 聊天请求 DTO（数据传输对象）
 * <p>
 * 用于接收前端发送的聊天请求参数，包含用户输入的消息内容
 * 以及当前会话标识，是用户与咖啡智能助手交互的入口数据结构。
 * </p>
 *
 * @author 曾家乐
 */
@Data
public class ChatRequest {

    /**
     * 用户输入的消息内容
     * 例如："推荐一杯适合夏天的咖啡"、"我要一杯冰拿铁"等
     * 不能为空，否则会返回参数校验错误
     */
    private String message;

    /**
     * 聊天会话唯一标识
     * 首次对话时可传空，后端会自动生成新的 sessionId；
     * 后续对话需携带此标识以保持上下文连续性
     */
    private String sessionId;

    /**
     * 用户标识，用于区分不同用户
     * 可选参数，如果不传则使用默认的匿名用户标识
     */
    private String userId;
}
