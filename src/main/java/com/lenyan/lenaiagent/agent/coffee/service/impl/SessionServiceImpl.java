package com.lenyan.lenaiagent.agent.coffee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lenyan.lenaiagent.agent.coffee.service.SessionService;
import com.lenyan.lenaiagent.domain.CoffeeChatSession;
import com.lenyan.lenaiagent.mapper.CoffeeChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 会话服务实现类
 *
 * @author 曾家乐
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl extends ServiceImpl<CoffeeChatSessionMapper, CoffeeChatSession> implements SessionService {

    private final CoffeeChatSessionMapper sessionMapper;

    @Override
    public CoffeeChatSession createSession(String sessionId, String userId, String title) {
        CoffeeChatSession session = CoffeeChatSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .title(title != null && title.length() > 200 ? title.substring(0, 200) : title)
                .status("ACTIVE")
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        sessionMapper.insert(session);
        log.info("会话已创建: sessionId={}, title={}", sessionId, title);
        return session;
    }

    @Override
    public void updateSessionIntent(String sessionId, String intent) {
        CoffeeChatSession update = new CoffeeChatSession();
        update.setIntent(intent);
        update.setUpdateTime(new Date());
        LambdaQueryWrapper<CoffeeChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoffeeChatSession::getSessionId, sessionId);
        sessionMapper.update(update, wrapper);
        log.debug("会话意图已更新: sessionId={}, intent={}", sessionId, intent);
    }

    @Override
    public void closeSession(String sessionId) {
        CoffeeChatSession update = new CoffeeChatSession();
        update.setStatus("CLOSED");
        update.setUpdateTime(new Date());
        LambdaQueryWrapper<CoffeeChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoffeeChatSession::getSessionId, sessionId);
        sessionMapper.update(update, wrapper);
        log.info("会话已关闭: sessionId={}", sessionId);
    }

    @Override
    public CoffeeChatSession getBySessionId(String sessionId) {
        LambdaQueryWrapper<CoffeeChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoffeeChatSession::getSessionId, sessionId);
        return sessionMapper.selectOne(wrapper);
    }
}
