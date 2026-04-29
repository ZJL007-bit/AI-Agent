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
 * 咖啡智能助手 - 咖啡商品实体类
 * <p>
 * 用于管理咖啡店提供的所有商品信息，
 * 包括商品名称、描述、价格、库存及分类等。
 * 管理员可通过后台增删改查商品，用户可通过智能助手浏览和下单。
 * </p>
 *
 * @author 曾家乐
 * @TableName coffee_product
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "coffee_product")
public class CoffeeProduct implements Serializable {

    /**
     * 商品主键ID，自增生成
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称，如"经典拿铁"、"冰美式"、"焦糖玛奇朵"等
     * 需要简洁明了，便于智能助手识别和推荐
     */
    @TableField(value = "name")
    private String name;

    /**
     * 商品详细描述，包括口感特点、成分、适用场景等
     * 用于智能助手向用户介绍商品时使用，也可作为 RAG 检索的文本来源
     */
    @TableField(value = "description")
    private String description;

    /**
     * 商品价格，单位为元
     * 管理员可在后台随时调整价格
     */
    @TableField(value = "price")
    private BigDecimal price;

    /**
     * 商品图片URL，用于前端展示商品图片
     * 可以为空，空时前端展示默认占位图
     */
    @TableField(value = "image_url")
    private String imageUrl;

    /**
     * 商品别名，分号分隔，如 "拿铁;latte;牛奶咖啡"
     * 用于搜索匹配，用户输入任意别名均可命中
     */
    @TableField(value = "alias")
    private String alias;

    /**
     * 成分描述，如 "意式浓缩咖啡、鲜牛奶"
     * 用于智能助手介绍商品成分
     */
    @TableField(value = "ingredients")
    private String ingredients;

    /**
     * 商品分类，如"热饮"、"冷饮"、"轻食"等
     * 用于智能助手分类推荐和菜单筛选
     */
    @TableField(value = "category")
    private String category;

    /**
     * 库存数量，用于判断商品是否可售
     * 当库存为0时，智能助手会告知用户该商品暂时缺货
     */
    @TableField(value = "stock")
    private Integer stock;

    /**
     * 上架状态：true 表示在售，false 表示下架
     * 下架商品不会出现在智能助手的推荐和搜索结果中
     */
    @TableField(value = "is_active")
    private Boolean isActive;

    /**
     * 商品创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 商品信息最后更新时间，如价格调整、库存变更等
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
     * 序列化版本号
     */
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
