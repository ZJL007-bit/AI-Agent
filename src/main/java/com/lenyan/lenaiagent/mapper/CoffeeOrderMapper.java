package com.lenyan.lenaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lenyan.lenaiagent.domain.CoffeeOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 咖啡订单 Mapper 接口
 * <p>
 * 提供对 coffee_order 表的数据库操作，包括基本的增删改查（继承自 BaseMapper）
 * 以及订单相关的自定义查询方法。订单的创建、状态更新等操作通过此接口实现。
 * </p>
 *
 * @author 曾家乐
 */
@Mapper
public interface CoffeeOrderMapper extends BaseMapper<CoffeeOrder> {

}
