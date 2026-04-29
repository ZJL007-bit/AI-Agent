package com.lenyan.lenaiagent.agent.coffee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单列表视图对象
 * <p>
 * 用于订单管理列表展示，包含聚合计算字段。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListVO {

    private Long id;
    private String orderNo;
    private String sessionId;
    private String userId;
    private String status;
    private String statusText;
    private BigDecimal totalAmount;
    private String remark;
    private Date createTime;
    private Date updateTime;

    /** 商品概要，如"美式咖啡 x1, 拿铁 x1" */
    private String productSummary;

    /** 商品总件数 */
    private Integer itemCount;
}
