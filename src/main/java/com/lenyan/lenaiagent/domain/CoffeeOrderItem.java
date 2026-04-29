package com.lenyan.lenaiagent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 咖啡智能助手 - 订单明细项实体类
 * <p>
 * 用于记录订单中每一个具体商品的购买信息，
 * 包括商品名称、数量、单价、小计金额及个性化定制内容等。
 * 多个订单明细项通过 orderId 关联到同一个 {@link CoffeeOrder}。
 * </p>
 *
 * @author 曾家乐
 * @TableName coffee_order_item
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "coffee_order_item")
public class CoffeeOrderItem implements Serializable {

    /**
     * 订单明细项主键ID，自增生成
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属订单ID，关联 {@link CoffeeOrder#id}
     * 一个订单可以包含多个明细项，如一杯拿铁和一杯美式
     */
    @TableField(value = "order_id")
    private Long orderId;

    /**
     * 商品ID，关联 {@link CoffeeProduct#id}
     * 用于追溯该明细项对应的咖啡商品信息
     */
    @TableField(value = "product_id")
    private Long productId;

    /**
     * 商品名称，冗余存储以避免商品信息变更后订单数据不一致
     * 例如："经典拿铁"、"冰美式"
     */
    @TableField(value = "product_name")
    private String productName;

    /**
     * 购买数量，如点了2杯拿铁则 quantity = 2
     */
    @TableField(value = "quantity")
    private Integer quantity;

    /**
     * 商品单价，单位为元
     * 记录下单时的商品价格，不受后续价格调整影响
     */
    @TableField(value = "unit_price")
    private BigDecimal unitPrice;

    /**
     * 小计金额 = unitPrice × quantity，单位为元
     * 在创建订单明细项时计算得出
     */
    @TableField(value = "subtotal")
    private BigDecimal subtotal;

    /**
     * 个性化定制内容，如"少冰"、"燕麦奶"、"半糖"等
     * 用于满足用户对咖啡的个性化口味需求
     */
    @TableField(value = "customization")
    private String customization;

    /**
     * 创建时间，订单明细项创建的时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 逻辑删除标志，true 表示已删除
     */
    @TableField(value = "is_delete")
    @TableLogic
    private Boolean isDelete;

    /**
     * 序列化版本号
     */
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
