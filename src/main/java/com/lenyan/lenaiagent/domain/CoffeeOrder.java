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
import java.util.List;

/**
 * 咖啡智能助手 - 咖啡订单实体类
 * <p>
 * 用于记录用户通过咖啡智能助手下单的订单信息，
 * 包括用户标识、订单状态、总金额及关联的聊天会话等。
 * 每个订单可包含多个订单明细项（{@link CoffeeOrderItem}），通过 orderId 关联。
 * </p>
 *
 * @author 曾家乐
 * @TableName coffee_order
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "coffee_order")
public class CoffeeOrder implements Serializable {

    /**
     * 订单主键ID，自增生成
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单唯一编号，对外展示的订单号
     * 格式如：CO20260427001，便于用户查询和客服追踪
     */
    @TableField(value = "order_no")
    private String orderNo;

    /**
     * 所属聊天会话ID，关联 {@link CoffeeChatSession#sessionId}
     * 用于追溯订单是从哪次对话中产生的
     */
    @TableField(value = "session_id")
    private String sessionId;

    /**
     * 下单用户标识，关联 {@link CoffeeChatSession#userId}
     * 用于区分不同用户的订单
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * 订单状态，可选值：
     * - PENDING: 待确认，用户提交订单等待确认
     * - CONFIRMED: 已确认，用户确认订单内容
     * - PREPARING: 制作中，咖啡师已开始制作
     * - COMPLETED: 已完成，订单制作完成
     * - CANCELLED: 已取消，用户取消订单
     */
    @TableField(value = "status")
    private String status;

    /**
     * 订单总金额，单位为元
     * 由所有订单明细项的小计金额累加计算得出
     */
    @TableField(value = "total_amount")
    private BigDecimal totalAmount;

    /**
     * 用户的特殊备注或要求，如"少冰"、"半糖"等
     * 适用于整个订单级别的备注，单品备注在 {@link CoffeeOrderItem#customization} 中
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 订单创建时间，即用户提交订单的时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 订单最后更新时间，订单状态变更时刷新
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 逻辑删除标志，true 表示已删除
     */
    @TableField(value = "is_delete")
    @TableLogic
    private Boolean isDelete;

    /**
     * 订单明细项列表，非数据库字段，仅用于数据传递
     */
    @TableField(exist = false)
    private List<CoffeeOrderItem> items;

    /**
     * 序列化版本号
     */
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
