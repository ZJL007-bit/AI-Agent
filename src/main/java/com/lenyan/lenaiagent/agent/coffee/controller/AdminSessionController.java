package com.lenyan.lenaiagent.agent.coffee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lenyan.lenaiagent.agent.coffee.dto.SessionListVO;
import com.lenyan.lenaiagent.common.Result;
import com.lenyan.lenaiagent.domain.CoffeeChatSession;
import com.lenyan.lenaiagent.mapper.CoffeeChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端 - 会话管理控制器
 * <p>
 * 提供聊天会话的后台管理接口，包括会话的分页查询、详情查看、关闭和删除等。
 * </p>
 *
 * @author 曾家乐
 */
@Slf4j
@RestController
@RequestMapping("/agent/admin/sessions")
@RequiredArgsConstructor
public class AdminSessionController {

    private final CoffeeChatSessionMapper sessionMapper;

    private static final Map<String, String> STATUS_TEXT_MAP = Map.of(
            "ACTIVE", "进行中",
            "CLOSED", "已关闭",
            "TRANSFERRED", "已转人工"
    );

    /**
     * 分页查询会话列表
     *
     * @param page   当前页码，默认1
     * @param size   每页条数，默认10
     * @param status 会话状态筛选条件，为空时查询全部
     * @return 分页会话数据（含状态文本）
     */
    @GetMapping
    public Result<Page<SessionListVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        Page<CoffeeChatSession> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CoffeeChatSession> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(CoffeeChatSession::getStatus, status);
        }
        wrapper.orderByDesc(CoffeeChatSession::getUpdateTime);
        Page<CoffeeChatSession> sessionPage = sessionMapper.selectPage(pageParam, wrapper);

        List<SessionListVO> voList = sessionPage.getRecords().stream().map(s -> SessionListVO.builder()
                .id(s.getId())
                .sessionId(s.getSessionId())
                .userId(s.getUserId())
                .title(s.getTitle())
                .intent(s.getIntent())
                .status(s.getStatus())
                .statusText(STATUS_TEXT_MAP.getOrDefault(s.getStatus(), s.getStatus()))
                .satisfactionScore(s.getSatisfactionScore())
                .createTime(s.getCreateTime())
                .updateTime(s.getUpdateTime())
                .build()).collect(Collectors.toList());

        Page<SessionListVO> voPage = new Page<>(sessionPage.getCurrent(), sessionPage.getSize(), sessionPage.getTotal());
        voPage.setRecords(voList);
        return Result.success(voPage);
    }

    /**
     * 查询会话详情
     */
    @GetMapping("/{id}")
    public Result<CoffeeChatSession> detail(@PathVariable Long id) {
        return Result.success(sessionMapper.selectById(id));
    }

    /**
     * 关闭会话
     */
    @PutMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id) {
        CoffeeChatSession session = new CoffeeChatSession();
        session.setId(id);
        session.setStatus("CLOSED");
        sessionMapper.updateById(session);
        log.info("管理员关闭会话: id={}", id);
        return Result.ok();
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sessionMapper.deleteById(id);
        log.info("管理员删除会话: id={}", id);
        return Result.ok();
    }
}
