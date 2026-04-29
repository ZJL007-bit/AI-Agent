package com.lenyan.lenaiagent.agent.coffee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lenyan.lenaiagent.agent.coffee.dto.ChatRequest;
import com.lenyan.lenaiagent.agent.coffee.service.OrderService;
import com.lenyan.lenaiagent.agent.coffee.service.SessionService;
import com.lenyan.lenaiagent.agent.coffee.workflow.AgentGraphConfig;
import com.lenyan.lenaiagent.agent.coffee.workflow.AgentState;
import com.lenyan.lenaiagent.common.Result;
import com.lenyan.lenaiagent.domain.CoffeeChatSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 咖啡智能助手 - 聊天控制器（SSE 流式输出）
 */
@Slf4j
@RestController
@RequestMapping("/agent/chat")
@RequiredArgsConstructor
public class AgentChatController {

    private final AgentGraphConfig agentGraphConfig;

    private final OrderService orderService;

    private final SessionService sessionService;

    /**
     * SSE 流式聊天接口
     * <p>
     * 接收用户消息后，异步执行工作流并通过 SSE 实时推送进度和结果。
     * 事件类型：
     * - status:  工作流节点执行进度，data.node 为节点名称
     * - response: 最终回复内容，data.content 为回复文本，data.sessionId 为会话ID
     * - error:   异常信息，data.message 为错误描述
     * </p>
     */
    @PostMapping
    public SseEmitter chat(@RequestBody ChatRequest request) {
        log.info("收到聊天请求: sessionId={}, message={}", request.getSessionId(), request.getMessage());

        String sessionId = request.getSessionId() != null && !request.getSessionId().isEmpty()
                ? request.getSessionId()
                : UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        SseEmitter emitter = new SseEmitter(300_000L);

        Thread.startVirtualThread(() -> {
            try {
                AgentState state = new AgentState();
                state.setSessionId(sessionId);
                state.setUserId(request.getUserId());
                state.setUserMessage(request.getMessage());

                agentGraphConfig.runWorkflow(state, nodeName -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("status")
                                .data(Map.of("node", nodeName)));
                    } catch (IOException e) {
                        // 客户端已断开，忽略
                    }
                });

                Map<String, Object> responseData = new LinkedHashMap<>();
                responseData.put("content", state.getResponse() != null ? state.getResponse() : "");
                responseData.put("sessionId", state.getSessionId());
                // 转发支付信息
                Object paymentEnabled = state.getContext("paymentEnabled");
                if (Boolean.TRUE.equals(paymentEnabled)) {
                    responseData.put("paymentEnabled", true);
                    responseData.put("orderNo", state.getContext("orderNo"));
                    responseData.put("orderTotal", state.getContext("orderTotal"));
                    responseData.put("orderStatus", state.getContext("orderStatus"));
                    responseData.put("orderCreateTime", state.getContext("orderCreateTime"));
                    responseData.put("orderProductSummary", state.getContext("orderProductSummary"));
                    responseData.put("orderItemCount", state.getContext("orderItemCount"));
                }

                emitter.send(SseEmitter.event()
                        .name("response")
                        .data(responseData));

                // 持久化会话
                try {
                    com.lenyan.lenaiagent.domain.CoffeeChatSession existing =
                            sessionService.getBySessionId(sessionId);
                    String intent = (String) state.getContext("intent");
                    if (existing == null) {
                        sessionService.createSession(sessionId, state.getUserId(), state.getUserMessage());
                    }
                    if (intent != null && !intent.isEmpty()) {
                        sessionService.updateSessionIntent(sessionId, intent);
                    }
                } catch (Exception e) {
                    log.error("保存会话失败: sessionId={}", sessionId, e);
                }

                emitter.complete();
            } catch (Exception e) {
                log.error("工作流执行异常: sessionId={}", sessionId, e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of("message", e.getMessage() != null ? e.getMessage() : "系统异常，请稍后重试")));
                } catch (IOException ex) {
                    // ignore
                }
                emitter.complete();
            }
        });

        emitter.onCompletion(() -> log.info("SSE 连接完成: sessionId={}", sessionId));
        emitter.onTimeout(() -> log.warn("SSE 连接超时: sessionId={}", sessionId));

        return emitter;
    }

    /**
     * 取消订单
     * <p>
     * 用户通过订单号取消待支付订单，取消后订单状态变为 CANCELLED 并回滚库存。
     * </p>
     *
     * @param body 包含 orderNo 的请求体
     * @return 取消结果
     */
    @PostMapping("/cancel")
    public Result<?> cancelOrder(@RequestBody Map<String, String> body) {
        String orderNo = body.get("orderNo");
        if (orderNo == null || orderNo.isEmpty()) {
            return Result.error(400, "订单号不能为空");
        }
        try {
            orderService.cancelOrderByOrderNo(orderNo);
            log.info("用户取消订单成功: orderNo={}", orderNo);
            return Result.ok();
        } catch (RuntimeException e) {
            log.warn("取消订单失败: orderNo={}, error={}", orderNo, e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 查询会话列表
     * <p>
     * 根据前端传入的 sessionId 列表查询对应的会话元信息（标题、状态、时间等）。
     * 用于前端会话记录面板展示历史会话。
     * </p>
     *
     * @param sessionIds 逗号分隔的 sessionId 列表
     * @return 会话信息列表
     */
    @GetMapping("/sessions")
    public Result<?> listSessions(@RequestParam(required = false) String sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<String> ids = Arrays.asList(sessionIds.split(","));
        List<CoffeeChatSession> sessions = sessionService.list(
                new LambdaQueryWrapper<CoffeeChatSession>()
                        .in(CoffeeChatSession::getSessionId, ids)
                        .orderByDesc(CoffeeChatSession::getCreateTime)
        );
        List<Map<String, Object>> result = sessions.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("sessionId", s.getSessionId());
            m.put("title", s.getTitle());
            m.put("intent", s.getIntent());
            m.put("status", s.getStatus());
            m.put("createTime", s.getCreateTime());
            return m;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 关闭/清除会话
     * <p>
     * 用户点击"清除记录"或"新建会话"时调用，
     * 将当前会话状态标记为 CLOSED。
     * </p>
     *
     * @param body 包含 sessionId 的请求体
     * @return 操作结果
     */
    @DeleteMapping("/session")
    public Result<?> clearSession(@RequestBody Map<String, String> body) {
        String sessionId = body.get("sessionId");
        if (sessionId == null || sessionId.isEmpty()) {
            return Result.error(400, "sessionId 不能为空");
        }
        try {
            sessionService.closeSession(sessionId);
            log.info("用户关闭会话: sessionId={}", sessionId);
            return Result.ok();
        } catch (Exception e) {
            log.warn("关闭会话失败: sessionId={}", sessionId, e);
            return Result.error(500, "关闭会话失败");
        }
    }
}
