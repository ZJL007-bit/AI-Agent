package com.lenyan.lenaiagent.agent.coffee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lenyan.lenaiagent.agent.coffee.service.RecommendationService;
import com.lenyan.lenaiagent.domain.CoffeeProduct;
import com.lenyan.lenaiagent.mapper.CoffeeProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品推荐服务实现类
 * <p>
 * 实现了 {@link RecommendationService} 接口，提供商品推荐、分类查询和关键词搜索功能。
 * 当前实现基于关键词匹配和分类筛选进行推荐，后续可接入 AI 模型实现更智能的语义推荐。
 * </p>
 *
 * @author 曾家乐
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    /**
     * 商品 Mapper，查询数据库中的商品数据
     */
    private final CoffeeProductMapper productMapper;

    /**
     * 根据用户偏好推荐商品
     * <p>
     * 实现逻辑：
     * 1. 查询所有在售且有库存的商品
     * 2. 根据用户偏好关键词在商品名称和描述中进行模糊匹配
     * 3. 返回匹配的商品列表
     * </p>
     * <p>
     * 示例：
     * - 用户输入"适合夏天的冰饮" → 推荐冷饮分类的商品
     * - 用户输入"提神" → 推荐含咖啡因较高的商品
     * </p>
     *
     * @param userPreference 用户偏好描述
     * @return 推荐的商品列表
     */
    @Override
    public List<CoffeeProduct> recommend(String userPreference) {
        log.debug("商品推荐: preference={}", userPreference);
        LambdaQueryWrapper<CoffeeProduct> wrapper = new LambdaQueryWrapper<>();
        // 只查询上架状态且有库存的商品
        wrapper.eq(CoffeeProduct::getIsActive, true);
        wrapper.gt(CoffeeProduct::getStock, 0);
        // 在商品名称或描述中模糊匹配用户偏好关键词
        wrapper.and(w -> w
                .like(CoffeeProduct::getName, userPreference)
                .or()
                .like(CoffeeProduct::getDescription, userPreference)
                .or()
                .like(CoffeeProduct::getCategory, userPreference)
        );
        return productMapper.selectList(wrapper);
    }

    /**
     * 按商品分类查询商品列表
     * <p>
     * 返回指定分类下所有上架且有库存的商品，
     * 按创建时间倒序排列，最新添加的商品排在前面。
     * </p>
     *
     * @param category 商品分类名称，如"热饮"、"冷饮"
     * @return 该分类下的商品列表
     */
    @Override
    public List<CoffeeProduct> getProductsByCategory(String category) {
        LambdaQueryWrapper<CoffeeProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoffeeProduct::getIsActive, true);
        wrapper.gt(CoffeeProduct::getStock, 0);
        wrapper.eq(CoffeeProduct::getCategory, category);
        wrapper.orderByDesc(CoffeeProduct::getCreateTime);
        return productMapper.selectList(wrapper);
    }

    /**
     * 根据关键词搜索商品
     * <p>
     * 在商品名称和描述中进行关键词模糊搜索，
     * 只返回上架状态的商品。
     * </p>
     *
     * @param keyword 搜索关键词，如"拿铁"、"美式"
     * @return 匹配的商品列表
     */
    @Override
    public List<CoffeeProduct> searchProducts(String keyword) {
        LambdaQueryWrapper<CoffeeProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoffeeProduct::getIsActive, true);
        // 在商品名称、描述、别名中搜索
        wrapper.and(w -> w
                .like(CoffeeProduct::getName, keyword)
                .or()
                .like(CoffeeProduct::getDescription, keyword)
                .or()
                .like(CoffeeProduct::getAlias, keyword)
        );
        return productMapper.selectList(wrapper);
    }

    /**
     * 获取热门推荐商品
     */
    @Override
    public List<CoffeeProduct> getHotProducts() {
        LambdaQueryWrapper<CoffeeProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoffeeProduct::getIsActive, true);
        wrapper.gt(CoffeeProduct::getStock, 0);
        wrapper.orderByDesc(CoffeeProduct::getStock);
        wrapper.last("LIMIT 6");
        return productMapper.selectList(wrapper);
    }
}
