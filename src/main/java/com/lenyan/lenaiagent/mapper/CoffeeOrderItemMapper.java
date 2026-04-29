package com.lenyan.lenaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lenyan.lenaiagent.domain.CoffeeOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 咖啡订单明细项 Mapper 接口
 * <p>
 * 提供对 coffee_order_item 表的数据库操作，包括基本的增删改查（继承自 BaseMapper）。
 * 每个订单明细项关联一个订单和一件商品，记录具体购买信息。
 * </p>
 *
 * @author 曾家乐
 */
@Mapper
public interface CoffeeOrderItemMapper extends BaseMapper<CoffeeOrderItem> {

}
