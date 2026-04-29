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
 * 咖啡智能助手 - 聊天会话实体类
 * <p>
 * 用于记录用户与咖啡智能助手之间的每一次聊天会话信息，
 * 包括会话ID、用户标识、会话状态及起止时间等。
 * 每个会话可以包含多条聊天消息，通过 sessionId 关联。
 * </p>
 *
 * @author 曾家乐
 * @TableName coffee_chat_session
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "coffee_chat_session")
public class CoffeeChatSession implements Serializable {

    /**
     * 会话主键ID，自增生成
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话唯一标识，用于关联聊天消息和订单等信息
     * 在用户发起对话时自动生成，整个会话生命周期内保持不变
     */
    @TableField(value = "session_id")
    private String sessionId;

    /**
     * 用户标识，用于区分不同的用户
     * 可以是用户ID、设备标识或临时访客标识
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * 意图分类结果，如 QUERY_PRODUCT、ORDER、CHITCHAT 等
     */
    @TableField(value = "intent")
    private String intent;

    /**
     * 会话标题，通常取用户的第一条消息内容作为摘要
     * 用于在会话列表中展示，方便用户快速定位历史对话
     */
    @TableField(value = "title")
    private String title;

    /**
     * 满意度评分，1-5
     */
    @TableField(value = "satisfaction_score")
    private Integer satisfactionScore;

    /**
     * 会话状态，可选值：
     * - ACTIVE: 活跃中，用户正在对话
     * - CLOSED: 已关闭，用户主动结束或超时关闭
     * - TRANSFERRED: 已转人工，因复杂问题转交人工客服处理
     */
    @TableField(value = "status")
    private String status;

    /**
     * 会话创建时间，即用户首次发起对话的时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 会话最后更新时间，每次对话交互时刷新
     * 可用于判断会话是否超时、排序会话列表等
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 逻辑删除标志，true 表示已删除
     * 删除后不会从数据库物理删除，而是标记为删除状态
     */
    @TableField(value = "is_delete")
    @TableLogic
    private Boolean isDelete;

    /**
     * 序列化版本号，用于 Java 序列化机制保证兼容性
     */
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
