package com.lenyan.lenaiagent.common;

import lombok.Data;

import java.util.UUID;

/**
 * 统一响应结果封装类
 * <p>
 * 对所有 API 接口的返回值进行统一格式封装，包含状态码、数据、消息和追踪ID。
 * 前端可根据 code 判断请求是否成功，通过 message 获取提示信息，
 * 通过 traceId 进行问题追踪和日志关联。
 * </p>
 * <p>
 * 响应格式示例：
 * <pre>
 * {
 *   "code": 200,
 *   "data": { ... },
 *   "message": "success",
 *   "traceId": "a1b2c3d4"
 * }
 * </pre>
 * </p>
 *
 * @param <T> 响应数据的泛型类型
 * @author 曾家乐
 */
@Data
public class Result<T> {

    /**
     * 状态码，200 表示成功，其他值表示各类错误
     * 常见错误码：
     * - 400: 请求参数错误
     * - 401: 未授权
     * - 403: 禁止访问
     * - 500: 服务器内部错误
     */
    private Integer code;

    /**
     * 响应数据，泛型类型，成功时携带业务数据，失败时为 null
     */
    private T data;

    /**
     * 响应消息，成功时为 "success"，失败时为错误描述
     */
    private String message;

    /**
     * 追踪ID，每次请求生成唯一的8位标识
     * 用于将前端错误反馈与后端日志关联，便于问题定位
     */
    private String traceId;

    /**
     * 私有构造函数，强制使用静态工厂方法创建实例
     */
    private Result() {
    }

    /**
     * 创建成功响应（携带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功的响应结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.data = data;
        result.message = "success";
        result.traceId = UUID.randomUUID().toString().substring(0, 8);
        return result;
    }

    /**
     * 创建成功响应（携带数据和自定义消息）
     *
     * @param data    响应数据
     * @param message 自定义成功消息
     * @param <T>     数据类型
     * @return 成功的响应结果
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = success(data);
        result.message = message;
        return result;
    }

    /**
     * 创建无数据的成功响应
     * <p>
     * 适用于不需要返回数据的操作，如删除、更新等
     * </p>
     *
     * @param <T> 数据类型
     * @return 无数据的成功响应
     */
    public static <T> Result<T> ok() {
        return success(null);
    }

    /**
     * 创建错误响应
     *
     * @param code    错误状态码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误的响应结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.data = null;
        result.message = message;
        result.traceId = UUID.randomUUID().toString().substring(0, 8);
        return result;
    }

    /**
     * 创建带指定追踪ID的错误响应
     * <p>
     * 适用于异常处理中需要保留原始追踪ID的场景，
     * 如在全局异常处理器中捕获异常时传入已有的追踪ID。
     * </p>
     *
     * @param code    错误状态码
     * @param message 错误消息
     * @param traceId 追踪ID
     * @param <T>     数据类型
     * @return 错误的响应结果
     */
    public static <T> Result<T> error(Integer code, String message, String traceId) {
        Result<T> result = error(code, message);
        result.traceId = traceId;
        return result;
    }
}
