package com.lenyan.lenaiagent.agent.coffee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lenyan.lenaiagent.agent.coffee.dto.OrderItemRequest;
import com.lenyan.lenaiagent.domain.CoffeeOrder;

import java.util.List;

/**
 * 订单服务接口
 * <p>
 * 提供咖啡订单的业务处理能力，包括订单的创建、确认、取消、查询等。
 * 继承 MyBatis-Plus 的 IService 接口，自动获得基础的增删改查能力。
 * 订单创建时会自动生成订单号、扣减库存并计算总金额。
 * </p>
 *
 * @author 曾家乐
 */
public interface OrderService extends IService<CoffeeOrder> {

    /**
     * 创建新订单
     * <p>
     * 根据用户在对话中选择的商品列表创建订单：
     * 1. 生成唯一订单号
     * 2. 校验商品库存是否充足
     * 3. 计算订单总金额
     * 4. 扣减商品库存
     * 5. 保存订单和订单明细
     * </p>
     *
     * @param sessionId   所属聊天会话ID
     * @param userId      下单用户标识
     * @param items       订单明细项列表，包含商品ID、数量、定制内容
     * @param remark      订单备注
     * @return 创建成功的订单实体，包含订单号和总金额等信息
     */
    CoffeeOrder createOrder(String sessionId, String userId, List<OrderItemRequest> items, String remark);

    /**
     * 确认订单
     * <p>
     * 用户确认订单内容后调用，将订单状态从 PENDING 更新为 CONFIRMED。
     * 确认后订单进入待制作状态。
     * </p>
     *
     * @param orderId 订单ID
     */
    void confirmOrder(Long orderId);

    /**
     * 取消订单
     * <p>
     * 用户取消订单时调用，将订单状态更新为 CANCELLED，
     * 同时回滚已扣减的商品库存。
     * </p>
     *
     * @param orderId 订单ID
     */
    void cancelOrder(Long orderId);

    /**
     * 根据订单号取消订单
     * <p>
     * 通过对外展示的订单号查找订单并取消。
     * 仅 PENDING 状态的订单可取消，取消后回滚库存。
     * </p>
     *
     * @param orderNo 订单编号
     * @return 取消后的订单实体
     */
    CoffeeOrder cancelOrderByOrderNo(String orderNo);

    /**
     * 根据会话ID查询订单列表（含订单明细）
     *
     * @param sessionId 聊天会话ID
     * @return 该会话下的所有订单
     */
    List<CoffeeOrder> getOrdersBySessionId(String sessionId);
}
