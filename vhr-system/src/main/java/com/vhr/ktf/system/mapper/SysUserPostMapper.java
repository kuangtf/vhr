package com.vhr.ktf.system.mapper;

import com.vhr.ktf.system.domain.SysUserPost;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户与岗位关联表 数据层
 * 
 * @author ruoyi
 */
@Repository
public interface SysUserPostMapper {

    /**
     * 通过用户ID删除用户和岗位关联
     * 
     * @param userId 用户ID
     */
    void deleteUserPostByUserId(Long userId);

    /**
     * 通过岗位ID查询岗位使用数量
     * 
     * @param postId 岗位ID
     * @return 结果
     */
    int countUserPostById(Long postId);

    /**
     * 批量删除用户和岗位关联
     * 
     * @param ids 需要删除的数据ID
     */
    void deleteUserPost(Long[] ids);

    /**
     * 批量新增用户岗位信息
     * 
     * @param userPostList 用户角色列表
     */
    void batchUserPost(List<SysUserPost> userPostList);
}
