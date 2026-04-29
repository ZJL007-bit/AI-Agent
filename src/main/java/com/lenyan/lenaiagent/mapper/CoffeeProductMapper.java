package com.lenyan.lenaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lenyan.lenaiagent.domain.CoffeeProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * 咖啡商品 Mapper 接口
 * <p>
 * 提供对 coffee_product 表的数据库操作，包括基本的增删改查（继承自 BaseMapper）。
 * 商品的查询、上下架、库存更新等操作通过此接口实现。
 * </p>
 *
 * @author 曾家乐
 */
@Mapper
public interface CoffeeProductMapper extends BaseMapper<CoffeeProduct> {

}
