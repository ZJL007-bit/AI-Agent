package com.lenyan.lenaiagent.agent.coffee.controller;

import com.lenyan.lenaiagent.agent.coffee.dto.FeedbackRequest;
import com.lenyan.lenaiagent.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 咖啡智能助手 - 用户反馈控制器
 * <p>
 * 提供用户对智能助手回复质量进行评价的接口。
 * 支持点赞/踩的快捷评价和文字反馈，收集的数据用于
 * 持续优化智能助手的回复策略和模型效果。
 * </p>
 *
 * @author 曾家乐
 */
@Slf4j
@RestController
@RequestMapping("/agent/feedback")
@RequiredArgsConstructor
public class AgentFeedbackController {

    /**
     * 提交用户反馈
     * <p>
     * 记录用户对智能助手某次回复的评价，包括：
     * - 点赞（LIKE）：用户对回复满意
     * - 踩（DISLIKE）：用户对回复不满意
     * - 文字评论：用户的具体意见或建议
     * </p>
     * <p>
     * 反馈数据可用于：
     * 1. 统计智能助手的服务满意度指标
     * 2. 定位回复质量较差的场景，有针对性地优化提示词
     * 3. 为后续模型微调积累高质量的正负样本
     * </p>
     *
     * @param request 反馈请求，包含会话ID、反馈类型和评论内容
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> feedback(@RequestBody FeedbackRequest request) {
        log.info("收到用户反馈: sessionId={}, type={}, comment={}",
                request.getSessionId(), request.getType(), request.getComment());
        // TODO: 将反馈数据持久化存储，用于后续分析和优化
        return Result.ok();
    }
}
