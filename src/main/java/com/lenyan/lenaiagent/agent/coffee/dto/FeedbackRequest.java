package com.lenyan.lenaiagent.agent.coffee.dto;

import lombok.Data;

/**
 * 用户反馈请求 DTO（数据传输对象）
 * <p>
 * 用于接收前端发送的用户对智能助手回复的反馈信息，
 * 支持点赞/踩的评价方式以及文字反馈，用于持续优化智能助手的回复质量。
 * </p>
 *
 * @author 曾家乐
 */
@Data
public class FeedbackRequest {

    /**
     * 聊天会话ID，标识反馈关联的会话
     * 用于追溯用户对哪次对话体验给出了反馈
     */
    private String sessionId;

    /**
     * 反馈类型，可选值：
     * - LIKE: 点赞，用户对回复满意
     * - DISLIKE: 踩，用户对回复不满意
     */
    private String type;

    /**
     * 用户文字反馈内容，可选项
     * 用户可以补充说明满意或不满意的原因，帮助改进助手
     */
    private String comment;
}
