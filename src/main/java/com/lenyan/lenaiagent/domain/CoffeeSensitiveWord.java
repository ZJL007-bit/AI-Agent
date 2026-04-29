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
import java.util.Date;

/**
 * 咖啡智能助手 - 敏感词实体类
 * <p>
 * 用于管理内容过滤所需的敏感词库，支持从数据库动态管理敏感词。
 * 敏感词分为不同严重等级（severity），可根据等级采取不同的过滤策略。
 * 敏感词同时支持从配置文件和数据库两个来源加载，数据库优先级更高。
 * </p>
 *
 * @author 曾家乐
 * @TableName coffee_sensitive_word
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "coffee_sensitive_word")
public class CoffeeSensitiveWord implements Serializable {

    /**
     * 敏感词主键ID，自增生成
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 敏感词内容，如违禁词、不雅用语等
     * 当用户输入中包含该词时，会被替换为 "***" 进行过滤
     */
    @TableField(value = "word")
    private String word;

    /**
     * 严重等级，用于区分不同严重程度的敏感词，可选值：
     * - LOW: 低级别，自动过滤但无需特殊处理
     * - MEDIUM: 中级别，过滤并记录日志
     * - HIGH: 高级别，过滤并可能触发转人工或警告
     */
    @TableField(value = "severity")
    private String severity;

    /**
     * 敏感词创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 逻辑删除标志，true 表示已删除
     * 删除的敏感词不会参与内容过滤
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
