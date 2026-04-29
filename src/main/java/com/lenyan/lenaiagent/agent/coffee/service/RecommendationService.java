package com.lenyan.lenaiagent.agent.coffee.service;

import com.lenyan.lenaiagent.domain.CoffeeProduct;

import java.util.List;

/**
 * 商品推荐服务接口
 * <p>
 * 提供基于用户偏好和场景的智能推荐能力，
 * 根据用户的需求描述（如"适合夏天的咖啡"、"提神的"），
 * 从商品库中筛选出最匹配的商品推荐给用户。
 * </p>
 *
 * @author 曾家乐
 */
public interface RecommendationService {

    /**
     * 根据用户偏好推荐商品
     * <p>
     * 分析用户的描述关键词（如"冰的"、"提神"、"甜一点"等），
     * 结合商品分类、描述等属性进行匹配推荐。
     * </p>
     *
     * @param userPreference 用户偏好描述，如"适合夏天的冰饮"、"低咖啡因"
     * @return 推荐的商品列表，按匹配度从高到低排序
     */
    List<CoffeeProduct> recommend(String userPreference);

    /**
     * 按商品分类查询商品列表
     * <p>
     * 返回指定分类下所有在售且有库存的商品。
     * 常见分类如"热饮"、"冷饮"、"轻食"等。
     * </p>
     *
     * @param category 商品分类名称
     * @return 该分类下的商品列表
     */
    List<CoffeeProduct> getProductsByCategory(String category);

    /**
     * 根据关键词搜索商品
     * <p>
     * 在商品名称和描述中进行关键词匹配搜索，
     * 返回匹配的在售商品列表。
     * </p>
     *
     * @param keyword 搜索关键词，如"拿铁"、"美式"
     * @return 匹配的商品列表
     */
    List<CoffeeProduct> searchProducts(String keyword);

    /**
     * 获取热门推荐商品
     * <p>
     * 返回在售且有库存的商品，按库存量降序排列（热销品通常备货多），
     * 限制返回6条，用于首次询问时展示热门菜单。
     * </p>
     *
     * @return 热门商品列表，最多6条
     */
    List<CoffeeProduct> getHotProducts();
}
