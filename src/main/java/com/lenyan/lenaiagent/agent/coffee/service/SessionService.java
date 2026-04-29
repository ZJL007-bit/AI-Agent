package com.lenyan.lenaiagent.agent.coffee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lenyan.lenaiagent.domain.CoffeeChatSession;

/**
 * 会话服务接口
 * <p>
 * 提供聊天会话的生命周期管理，包括创建、意图更新、关闭和查询。
 * 继承 MyBatis-Plus 的 IService，自动获得基础 CRUD 能力。
 * </p>
 *
 * @author 曾家乐
 */
public interface SessionService extends IService<CoffeeChatSession> {

    /**
     * 创建新会话
     *
     * @param sessionId 会话唯一标识
     * @param userId    用户标识
     * @param title     会话标题（通常为第一条用户消息）
     * @return 创建成功的会话实体
     */
    CoffeeChatSession createSession(String sessionId, String userId, String title);

    /**
     * 更新会话意图
     *
     * @param sessionId 会话唯一标识
     * @param intent    意图分类结果
     */
    void updateSessionIntent(String sessionId, String intent);

    /**
     * 关闭会话
     *
     * @param sessionId 会话唯一标识
     */
    void closeSession(String sessionId);

    /**
     * 根据 sessionId 查询会话
     *
     * @param sessionId 会话唯一标识
     * @return 会话实体，不存在则返回 null
     */
    CoffeeChatSession getBySessionId(String sessionId);
}
