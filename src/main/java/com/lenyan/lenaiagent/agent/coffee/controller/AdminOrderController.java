package com.lenyan.lenaiagent.agent.coffee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lenyan.lenaiagent.agent.coffee.dto.OrderListVO;
import com.lenyan.lenaiagent.agent.coffee.service.OrderService;
import com.lenyan.lenaiagent.common.Result;
import com.lenyan.lenaiagent.domain.CoffeeOrder;
import com.lenyan.lenaiagent.domain.CoffeeOrderItem;
import com.lenyan.lenaiagent.mapper.CoffeeOrderItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端 - 订单管理控制器
 * <p>
 * 提供咖啡订单的后台管理接口，包括订单的分页查询、详情查看、状态更新等。
 * 仅管理员可访问，用于门店工作人员管理用户通过智能助手下单的订单。
 * </p>
 *
 * @author 曾家乐
 */
@RestController
@RequestMapping("/agent/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    private final CoffeeOrderItemMapper orderItemMapper;

    private static final Map<String, String> STATUS_TEXT_MAP = Map.of(
            "PENDING", "待确认",
            "CONFIRMED", "已确认",
            "PREPARING", "制作中",
            "COMPLETED", "已完成",
            "CANCELLED", "已取消"
    );

    /**
     * 分页查询订单列表（含商品概要和总件数）
     */
    @GetMapping
    public Result<Page<OrderListVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        Page<CoffeeOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CoffeeOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(CoffeeOrder::getStatus, status);
        }
        wrapper.orderByDesc(CoffeeOrder::getCreateTime);
        Page<CoffeeOrder> orderPage = orderService.page(pageParam, wrapper);

        // 转为 VO，填充聚合字段
        Page<OrderListVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<OrderListVO> voList = orderPage.getRecords().stream().map(order -> {
            // 查询订单明细
            List<CoffeeOrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<CoffeeOrderItem>().eq(CoffeeOrderItem::getOrderId, order.getId()));
            int itemCount = items.stream().mapToInt(CoffeeOrderItem::getQuantity).sum();
            String productSummary = items.stream()
                    .map(i -> i.getProductName() + " x" + i.getQuantity())
                    .collect(Collectors.joining(", "));

            return OrderListVO.builder()
                    .id(order.getId())
                    .orderNo(order.getOrderNo())
                    .sessionId(order.getSessionId())
                    .userId(order.getUserId())
                    .status(order.getStatus())
                    .statusText(STATUS_TEXT_MAP.getOrDefault(order.getStatus(), order.getStatus()))
                    .totalAmount(order.getTotalAmount())
                    .remark(order.getRemark())
                    .createTime(order.getCreateTime())
                    .updateTime(order.getUpdateTime())
                    .productSummary(productSummary)
                    .itemCount(itemCount)
                    .build();
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return Result.success(voPage);
    }

    /**
     * 查询订单详情
     * <p>
     * 根据订单ID查询完整的订单信息，用于管理员查看订单详细内容。
     * </p>
     *
     * @param id 订单主键ID
     * @return 订单详情信息
     */
    @GetMapping("/{id}")
    public Result<CoffeeOrder> detail(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    /**
     * 更新订单状态
     * <p>
     * 管理员可手动更新订单状态，如将"待确认"改为"已确认"、
     * "已确认"改为"制作中"、"制作中"改为"已完成"等。
     * </p>
     *
     * @param id     订单主键ID
     * @param status 新的订单状态
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        CoffeeOrder order = new CoffeeOrder();
        order.setId(id);
        order.setStatus(status);
        orderService.updateById(order);
        return Result.ok();
    }
}
