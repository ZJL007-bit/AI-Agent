package com.lenyan.lenaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lenyan.lenaiagent.domain.CoffeeChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 咖啡聊天会话 Mapper 接口
 * <p>
 * 提供对 coffee_chat_session 表的数据库操作，包括基本的增删改查（继承自 BaseMapper）
 * 以及针对会话管理的自定义查询方法。
 * </p>
 *
 * @author 曾家乐
 */
@Mapper
public interface CoffeeChatSessionMapper extends BaseMapper<CoffeeChatSession> {

}
