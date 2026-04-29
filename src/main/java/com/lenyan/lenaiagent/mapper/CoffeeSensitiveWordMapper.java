package com.lenyan.lenaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lenyan.lenaiagent.domain.CoffeeSensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 咖啡敏感词 Mapper 接口
 * <p>
 * 提供对 coffee_sensitive_word 表的数据库操作，包括基本的增删改查（继承自 BaseMapper）
 * 以及敏感词相关的自定义查询方法。敏感词用于内容过滤，防止不当内容出现在对话中。
 * </p>
 *
 * @author 曾家乐
 */
@Mapper
public interface CoffeeSensitiveWordMapper extends BaseMapper<CoffeeSensitiveWord> {

    /**
     * 查询所有未被逻辑删除的敏感词内容
     * <p>
     * 用于在应用启动时和敏感词热更新时加载全部敏感词到内存中，
     * 以实现高效的实时内容过滤。
     * </p>
     *
     * @return 敏感词字符串列表，仅包含 word 字段值
     */
    @Select("SELECT word FROM coffee_sensitive_word WHERE is_delete = 0")
    List<String> getAllWords();
}
