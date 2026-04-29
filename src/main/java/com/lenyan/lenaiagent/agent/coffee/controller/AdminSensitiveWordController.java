package com.lenyan.lenaiagent.agent.coffee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lenyan.lenaiagent.agent.coffee.service.SensitiveFilter;
import com.lenyan.lenaiagent.common.Result;
import com.lenyan.lenaiagent.domain.CoffeeSensitiveWord;
import com.lenyan.lenaiagent.mapper.CoffeeSensitiveWordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 敏感词管理控制器
 * <p>
 * 提供敏感词的后台管理接口，包括敏感词的增删查及热更新。
 * 管理员可通过此接口动态维护敏感词库，修改后可触发即时热更新，
 * 无需重启服务即可生效。
 * </p>
 *
 * @author 曾家乐
 */
@RestController
@RequestMapping("/agent/admin/sensitive-words")
@RequiredArgsConstructor
public class AdminSensitiveWordController {

    /**
     * 敏感词 Mapper，操作数据库中的敏感词数据
     */
    private final CoffeeSensitiveWordMapper sensitiveWordMapper;

    /**
     * 敏感词过滤器，支持热更新敏感词列表
     */
    private final SensitiveFilter sensitiveFilter;

    /**
     * 分页查询敏感词列表
     * <p>
     * 按创建时间倒序排列，最新的敏感词排在前面。
     * </p>
     *
     * @param page 当前页码，默认1
     * @param size 每页条数，默认10
     * @return 分页敏感词数据
     */
    @GetMapping
    public Result<Page<CoffeeSensitiveWord>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<CoffeeSensitiveWord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CoffeeSensitiveWord> wrapper = new LambdaQueryWrapper<>();
        // 按创建时间倒序排列
        wrapper.orderByDesc(CoffeeSensitiveWord::getCreateTime);
        return Result.success(sensitiveWordMapper.selectPage(pageParam, wrapper));
    }

    /**
     * 新增敏感词（添加后自动热更新）
     */
    @PostMapping
    public Result<Void> add(@RequestBody CoffeeSensitiveWord word) {
        sensitiveWordMapper.insert(word);
        sensitiveFilter.reloadWordList();
        return Result.ok();
    }

    /**
     * 更新敏感词（修改后自动热更新）
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CoffeeSensitiveWord word) {
        word.setId(id);
        sensitiveWordMapper.updateById(word);
        sensitiveFilter.reloadWordList();
        return Result.ok();
    }

    /**
     * 删除敏感词，逻辑删除后自动热更新
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sensitiveWordMapper.deleteById(id);
        sensitiveFilter.reloadWordList();
        return Result.ok();
    }

    /**
     * 手动触发敏感词热更新
     */
    @PostMapping("/reload")
    public Result<String> reload() {
        sensitiveFilter.reloadWordList();
        return Result.success("敏感词已重新加载");
    }
}
