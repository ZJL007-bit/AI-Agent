package com.lenyan.lenaiagent.agent.coffee.dto;

import lombok.Data;

/**
 * 订单明细项请求 DTO（数据传输对象）
 * <p>
 * 用于接收前端提交的订单中每个商品的购买信息，
 * 在用户确认下单时将商品列表传递给后端。
 * 每个实例代表订单中的一种商品及其购买数量和定制要求。
 * </p>
 *
 * @author 曾家乐
 */
@Data
public class OrderItemRequest {

    /**
     * 商品ID，关联咖啡商品表中的唯一标识
     * 用于从数据库查询商品详情（名称、价格等）
     */
    private Long productId;

    /**
     * 商品名称，前端冗余传参，用于快速展示
     * 实际以数据库中 productId 对应的商品信息为准
     */
    private String productName;

    /**
     * 购买数量，必须大于0
     * 例如：点2杯拿铁则 quantity = 2
     */
    private Integer quantity;

    /**
     * 个性化定制内容，如"少冰"、"燕麦奶"、"半糖"等
     * 为空表示使用默认制作方式
     */
    private String customization;
}
