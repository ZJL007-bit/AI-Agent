package com.lenyan.lenaiagent.agent.coffee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 会话列表 VO
 *
 * @author 曾家乐
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionListVO {

    private Long id;

    private String sessionId;

    private String userId;

    private String title;

    private String intent;

    private String status;

    private String statusText;

    private Integer satisfactionScore;

    private Date createTime;

    private Date updateTime;
}
