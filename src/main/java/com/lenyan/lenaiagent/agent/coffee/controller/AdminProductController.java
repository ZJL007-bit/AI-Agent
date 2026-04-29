package com.lenyan.lenaiagent.agent.coffee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lenyan.lenaiagent.common.Result;
import com.lenyan.lenaiagent.domain.CoffeeProduct;
import com.lenyan.lenaiagent.mapper.CoffeeProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 商品管理控制器
 * <p>
 * 提供咖啡商品的后台管理接口，包括商品的增删改查、上下架及库存管理。
 * 仅管理员可访问，用于门店工作人员维护咖啡菜单中的商品信息。
 * </p>
 *
 * @author 曾家乐
 */
@RestController
@RequestMapping("/agent/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    /**
     * 商品 Mapper，直接操作数据库中的商品数据
     */
    private final CoffeeProductMapper productMapper;

    /**
     * 分页查询商品列表
     * <p>
     * 支持按商品分类筛选，按创建时间倒序排列。
     * </p>
     *
     * @param page     当前页码，默认1
     * @param size     每页条数，默认10
     * @param category 商品分类筛选条件，为空时查询全部分类
     * @return 分页商品数据
     */
    @GetMapping
    public Result<Page<CoffeeProduct>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category) {
        Page<CoffeeProduct> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CoffeeProduct> wrapper = new LambdaQueryWrapper<>();
        // 如果指定了分类筛选条件，则只查询该分类的商品
        if (category != null && !category.isEmpty()) {
            wrapper.eq(CoffeeProduct::getCategory, category);
        }
        // 按创建时间倒序排列，最新添加的商品排在前面
        wrapper.orderByDesc(CoffeeProduct::getStock);
        return Result.success(productMapper.selectPage(pageParam, wrapper));
    }

    /**
     * 新增商品
     * <p>
     * 管理员添加新的咖啡商品到菜单中，新品默认为上架状态。
     * </p>
     *
     * @param product 商品信息，从前端表单提交
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> add(@RequestBody CoffeeProduct product) {
        productMapper.insert(product);
        return Result.ok();
    }

    /**
     * 更新商品信息
     * <p>
     * 管理员可修改商品名称、描述、价格、分类等信息。
     * </p>
     *
     * @param id      商品主键ID
     * @param product 更新后的商品信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CoffeeProduct product) {
        product.setId(id);
        productMapper.updateById(product);
        return Result.ok();
    }

    /**
     * 删除商品（逻辑删除）
     * <p>
     * 将商品标记为已删除，不会从数据库物理删除。
     * 已删除的商品不会出现在前台菜单和搜索结果中。
     * </p>
     *
     * @param id 商品主键ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productMapper.deleteById(id);
        return Result.ok();
    }

    /**
     * 商品上下架操作
     * <p>
     * 切换商品的上架状态：上架商品可被智能助手推荐和搜索到，
     * 下架商品则不会出现在用户端的任何展示中。
     * </p>
     *
     * @param id       商品主键ID
     * @param isActive 上架状态，true=上架，false=下架
     * @return 操作结果
     */
    @PutMapping("/{id}/active")
    public Result<Void> toggleActive(@PathVariable Long id, @RequestParam Boolean isActive) {
        CoffeeProduct product = new CoffeeProduct();
        product.setId(id);
        product.setIsActive(isActive);
        productMapper.updateById(product);
        return Result.ok();
    }
}
