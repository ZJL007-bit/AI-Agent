package com.lenyan.lenaiagent.agent.coffee.service;

import com.lenyan.lenaiagent.domain.CoffeeProduct;

import java.util.List;

/**
 * 库存服务接口
 * <p>
 * 提供咖啡商品的库存管理能力，包括库存查询、扣减和回滚等。
 * 在用户下单时需要检查商品库存是否充足，下单成功后扣减库存；
 * 在订单取消时需要回滚库存，防止库存数据不一致。
 * </p>
 *
 * @author 曾家乐
 */
public interface InventoryService {

    /**
     * 检查商品库存是否充足
     * <p>
     * 在用户下单前调用，判断指定商品的库存是否满足购买数量要求。
     * 如果库存不足，智能助手会提示用户该商品暂时缺货或减少购买数量。
     * </p>
     *
     * @param productId 商品ID
     * @param quantity  需要购买的数量
     * @return true 表示库存充足，false 表示库存不足
     */
    boolean checkStock(Long productId, Integer quantity);

    /**
     * 扣减商品库存
     * <p>
     * 在订单确认后调用，将指定商品的库存数量减少。
     * 需要保证原子性，防止并发下单导致超卖。
     * </p>
     *
     * @param productId 商品ID
     * @param quantity  需要扣减的数量
     */
    void deductStock(Long productId, Integer quantity);

    /**
     * 回滚商品库存
     * <p>
     * 在订单取消时调用，将之前扣减的库存数量恢复。
     * 保证库存数据与实际销售情况一致。
     * </p>
     *
     * @param productId 商品ID
     * @param quantity  需要回滚的数量
     */
    void rollbackStock(Long productId, Integer quantity);

    /**
     * 查询所有在售且有库存的商品列表
     * <p>
     * 用于智能助手推荐和商品查询场景，只返回上架且有库存的商品。
     * </p>
     *
     * @return 在售且有库存的商品列表
     */
    List<CoffeeProduct> getAvailableProducts();
}
