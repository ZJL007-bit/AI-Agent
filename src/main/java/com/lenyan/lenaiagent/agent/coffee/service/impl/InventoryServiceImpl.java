package com.lenyan.lenaiagent.agent.coffee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lenyan.lenaiagent.agent.coffee.service.InventoryService;
import com.lenyan.lenaiagent.domain.CoffeeProduct;
import com.lenyan.lenaiagent.mapper.CoffeeProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 库存服务实现类
 * <p>
 * 实现了 {@link InventoryService} 接口，提供库存管理的核心逻辑。
 * 通过 MyBatis-Plus 操作数据库中的商品库存数据，
 * 在库存扣减和回滚时保证数据一致性。
 * </p>
 * @author 曾家乐
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    /**
     * 商品 Mapper，操作数据库中的商品数据
     */
    private final CoffeeProductMapper productMapper;

    /**
     * 检查商品库存是否充足
     * <p>
     * 实现逻辑：
     * 1. 根据 productId 查询商品信息
     * 2. 比较商品当前库存与所需数量
     * 3. 库存 ≥ 所需数量时返回 true，否则返回 false
     * </p>
     *
     * @param productId 商品ID
     * @param quantity  需要购买的数量
     * @return true 表示库存充足，false 表示库存不足
     */
    @Override
    public boolean checkStock(Long productId, Integer quantity) {
        CoffeeProduct product = productMapper.selectById(productId);
        if (product == null) {
            log.warn("商品不存在: productId={}", productId);
            return false;
        }
        return product.getStock() >= quantity;
    }

    /**
     * 扣减商品库存
     * <p>
     * 实现逻辑：
     * 1. 查询商品当前库存
     * 2. 计算扣减后的新库存值
     * 3. 更新数据库中的库存字段
     * 注意：当前实现未加分布式锁，在高并发场景下可能存在超卖风险
     * </p>
     *
     * @param productId 商品ID
     * @param quantity  需要扣减的数量
     */
    @Override
    public void deductStock(Long productId, Integer quantity) {
        CoffeeProduct product = productMapper.selectById(productId);
        if (product != null) {
            product.setStock(product.getStock() - quantity);
            productMapper.updateById(product);
            log.info("库存扣减: productId={}, quantity={}, remaining={}", productId, quantity, product.getStock());
        }
    }

    /**
     * 回滚商品库存
     * <p>
     * 实现逻辑：
     * 1. 查询商品当前库存
     * 2. 将之前扣减的数量加回库存
     * 3. 更新数据库中的库存字段
     * 在订单取消时调用，保证库存数据与实际销售情况一致
     * </p>
     *
     * @param productId 商品ID
     * @param quantity  需要回滚的数量
     */
    @Override
    public void rollbackStock(Long productId, Integer quantity) {
        CoffeeProduct product = productMapper.selectById(productId);
        if (product != null) {
            product.setStock(product.getStock() + quantity);
            productMapper.updateById(product);
            log.info("库存回滚: productId={}, quantity={}, remaining={}", productId, quantity, product.getStock());
        }
    }

    /**
     * 查询所有在售且有库存的商品列表
     * <p>
     * 实现逻辑：
     * 1. 查询上架状态（isActive = true）的商品
     * 2. 过滤库存大于0的商品
     * 用于智能助手推荐和商品查询场景
     * </p>
     *
     * @return 在售且有库存的商品列表
     */
    @Override
    public List<CoffeeProduct> getAvailableProducts() {
        LambdaQueryWrapper<CoffeeProduct> wrapper = new LambdaQueryWrapper<>();
        // 只查询上架状态的商品
        wrapper.eq(CoffeeProduct::getIsActive, true);
        // 只查询库存大于0的商品，排除缺货商品
        wrapper.gt(CoffeeProduct::getStock, 0);
        return productMapper.selectList(wrapper);
    }
}
