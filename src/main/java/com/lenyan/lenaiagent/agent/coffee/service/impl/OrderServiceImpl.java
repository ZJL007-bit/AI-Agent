package com.lenyan.lenaiagent.agent.coffee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lenyan.lenaiagent.agent.coffee.dto.OrderItemRequest;
import com.lenyan.lenaiagent.agent.coffee.service.InventoryService;
import com.lenyan.lenaiagent.agent.coffee.service.OrderService;
import com.lenyan.lenaiagent.domain.CoffeeOrder;
import com.lenyan.lenaiagent.domain.CoffeeOrderItem;
import com.lenyan.lenaiagent.domain.CoffeeProduct;
import com.lenyan.lenaiagent.mapper.CoffeeOrderItemMapper;
import com.lenyan.lenaiagent.mapper.CoffeeOrderMapper;
import com.lenyan.lenaiagent.mapper.CoffeeProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单服务实现类
 * <p>
 * 实现了 {@link OrderService} 接口，提供订单管理的核心业务逻辑。
 * 继承 MyBatis-Plus 的 ServiceImpl，自动获得基础的增删改查能力。
 * 订单创建使用事务保证数据一致性，确保订单、订单明细和库存的同步更新。
 * </p>
 *
 * @author 曾家乐
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<CoffeeOrderMapper, CoffeeOrder> implements OrderService {

    /**
     * 订单 Mapper，操作数据库中的订单数据
     */
    private final CoffeeOrderMapper orderMapper;

    /**
     * 订单明细项 Mapper，操作数据库中的订单明细数据
     */
    private final CoffeeOrderItemMapper orderItemMapper;

    /**
     * 商品 Mapper，查询商品信息用于计算订单金额
     */
    private final CoffeeProductMapper productMapper;

    /**
     * 库存服务，处理库存校验、扣减和回滚
     */
    private final InventoryService inventoryService;

    /**
     * 创建新订单
     * <p>
     * 实现逻辑：
     * 1. 生成唯一订单号（格式：CO + 年月日 + 3位序号）
     * 2. 遍历订单明细项，校验每个商品的库存是否充足
     * 3. 查询商品信息，计算每个明细项的小计金额
     * 4. 累加所有明细项的小计得到订单总金额
     * 5. 扣减每个商品的库存
     * 6. 保存订单和所有订单明细项
     * </p>
     * <p>
     * 使用 @Transactional 注解保证事务一致性：
     * 如果任一步骤失败，整个操作回滚，不会产生脏数据。
     * </p>
     *
     * @param sessionId 所属聊天会话ID
     * @param userId    下单用户标识
     * @param items     订单明细项列表
     * @param remark    订单备注
     * @return 创建成功的订单实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoffeeOrder createOrder(String sessionId, String userId, List<OrderItemRequest> items, String remark) {
        // 生成唯一订单号
        String orderNo = generateOrderNo();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 创建订单实体
        CoffeeOrder order = CoffeeOrder.builder()
                .orderNo(orderNo)
                .sessionId(sessionId)
                .userId(userId)
                .status("PENDING")
                .totalAmount(totalAmount)
                .remark(remark)
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        orderMapper.insert(order);

        // 遍历订单明细项，逐个处理
        for (OrderItemRequest item : items) {
            // 校验库存是否充足
            if (!inventoryService.checkStock(item.getProductId(), item.getQuantity())) {
                throw new RuntimeException("商品库存不足: productId=" + item.getProductId());
            }

            // 查询商品信息，获取当前价格
            CoffeeProduct product = productMapper.selectById(item.getProductId());
            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            // 创建订单明细项
            CoffeeOrderItem orderItem = CoffeeOrderItem.builder()
                    .orderId(order.getId())
                    .productId(item.getProductId())
                    .productName(product.getName())
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .customization(item.getCustomization())
                    .createTime(new Date())
                    .build();
            orderItemMapper.insert(orderItem);

            // 累加到订单总金额
            totalAmount = totalAmount.add(subtotal);

            // 扣减库存
            inventoryService.deductStock(item.getProductId(), item.getQuantity());
        }

        // 更新订单总金额
        order.setTotalAmount(totalAmount);
        orderMapper.updateById(order);

        log.info("订单创建成功: orderNo={}, totalAmount={}", orderNo, totalAmount);
        return order;
    }

    /**
     * 确认订单
     * <p>
     * 将订单状态从 PENDING 更新为 CONFIRMED，表示用户已确认订单内容。
     * </p>
     *
     * @param orderId 订单ID
     */
    @Override
    public void confirmOrder(Long orderId) {
        CoffeeOrder order = new CoffeeOrder();
        order.setId(orderId);
        order.setStatus("CONFIRMED");
        order.setUpdateTime(new Date());
        orderMapper.updateById(order);
        log.info("订单已确认: orderId={}", orderId);
    }

    /**
     * 取消订单
     * <p>
     * 实现逻辑：
     * 1. 将订单状态更新为 CANCELLED
     * 2. 查询订单下的所有明细项
     * 3. 逐个回滚商品库存
     * </p>
     *
     * @param orderId 订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        // 更新订单状态为已取消
        CoffeeOrder order = new CoffeeOrder();
        order.setId(orderId);
        order.setStatus("CANCELLED");
        order.setUpdateTime(new Date());
        orderMapper.updateById(order);

        // 查询订单下所有明细项，逐个回滚库存
        LambdaQueryWrapper<CoffeeOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoffeeOrderItem::getOrderId, orderId);
        List<CoffeeOrderItem> items = orderItemMapper.selectList(wrapper);

        for (CoffeeOrderItem item : items) {
            inventoryService.rollbackStock(item.getProductId(), item.getQuantity());
        }

        log.info("订单已取消: orderId={}", orderId);
    }

    /**
     * 根据订单号取消订单
     * <p>
     * 通过 orderNo 查找订单，校验状态为 PENDING 后才执行取消，
     * 取消时回滚库存。非 PENDING 状态的订单不可取消。
     * </p>
     *
     * @param orderNo 订单编号
     * @return 取消后的订单实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoffeeOrder cancelOrderByOrderNo(String orderNo) {
        LambdaQueryWrapper<CoffeeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoffeeOrder::getOrderNo, orderNo);
        CoffeeOrder order = orderMapper.selectOne(wrapper);
        if (order == null) {
            throw new RuntimeException("订单不存在: " + orderNo);
        }
        if ("CANCELLED".equals(order.getStatus())) {
            throw new RuntimeException("订单已取消，无需重复操作");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("订单状态为" + order.getStatus() + "，无法取消");
        }
        cancelOrder(order.getId());
        return orderMapper.selectById(order.getId());
    }

    /**
     * 根据会话ID查询订单列表（含订单明细）
     */
    @Override
    public List<CoffeeOrder> getOrdersBySessionId(String sessionId) {
        LambdaQueryWrapper<CoffeeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoffeeOrder::getSessionId, sessionId);
        wrapper.orderByDesc(CoffeeOrder::getCreateTime);
        List<CoffeeOrder> orders = orderMapper.selectList(wrapper);

        // 填充每个订单的明细项
        for (CoffeeOrder order : orders) {
            LambdaQueryWrapper<CoffeeOrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(CoffeeOrderItem::getOrderId, order.getId());
            order.setItems(orderItemMapper.selectList(itemWrapper));
        }
        return orders;
    }

    /**
     * 生成唯一订单号
     * <p>
     * 格式：CO + 年月日 + 3位随机序号，例如 CO20260427001
     * 确保每个订单号全局唯一，便于用户查询和客服追踪。
     * </p>
     *
     * @return 唯一订单号字符串
     */
    private String generateOrderNo() {
        return "CO" + System.currentTimeMillis();
    }
}
