package com.vhr.ktf.web.controller.tool;

import com.vhr.ktf.common.core.controller.BaseController;
import com.vhr.ktf.common.core.domain.AjaxResult;
import com.vhr.ktf.common.utils.StringUtils;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * swagger 用户测试方法
 * 
 * @author vhr.ktf
 */
@Api("用户信息管理")
@RestController
@RequestMapping("/test/user")
public class TestUserController extends BaseController {

    private static final Map<Integer, UserEntity> USERS = new LinkedHashMap<Integer, UserEntity>();

    static {
        USERS.put(1, new UserEntity(1, "kuangtf", "122", "12312312313"));
    }


    @ApiOperation("获取用户列表")
    @GetMapping("/list")
    public AjaxResult userList() {
        List<UserEntity> userList = new ArrayList<>(USERS.values());
        return AjaxResult.success(userList);
    }

    @ApiOperation("获取用户详细")
    @ApiImplicitParam(name = "userId", value = "用户ID", required = true, dataType = "int", paramType = "path", dataTypeClass = Integer.class)
    @GetMapping("/{userId}")
    public AjaxResult getUser(@PathVariable Integer userId)
    {
        if (!USERS.isEmpty() && USERS.containsKey(userId))
        {
            return AjaxResult.success(USERS.get(userId));
        }
        else
        {
            return error("用户不存在");
        }
    }

    @ApiOperation("新增用户")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "userId", value = "用户id", dataType = "Integer", dataTypeClass = Integer.class),
        @ApiImplicitParam(name = "username", value = "用户名称", dataType = "String", dataTypeClass = String.class),
        @ApiImplicitParam(name = "password", value = "用户密码", dataType = "String", dataTypeClass = String.class),
        @ApiImplicitParam(name = "mobile", value = "用户手机", dataType = "String", dataTypeClass = String.class)
    })
    @PostMapping("/save")
    public AjaxResult save(UserEntity user)
    {
        if (StringUtils.isNull(user) || StringUtils.isNull(user.getUserId()))
        {
            return error("用户ID不能为空");
        }
        return AjaxResult.success(USERS.put(user.getUserId(), user));
    }

    @ApiOperation("更新用户")
    @PutMapping("/update")
    public AjaxResult update(@RequestBody UserEntity user)
    {
        if (StringUtils.isNull(user) || StringUtils.isNull(user.getUserId()))
        {
            return error("用户ID不能为空");
        }
        if (USERS.isEmpty() || !USERS.containsKey(user.getUserId()))
        {
            return error("用户不存在");
        }
        USERS.remove(user.getUserId());
        return AjaxResult.success(USERS.put(user.getUserId(), user));
    }

    @ApiOperation("删除用户信息")
    @ApiImplicitParam(name = "userId", value = "用户ID", required = true, dataType = "int", paramType = "path", dataTypeClass = Integer.class)
    @DeleteMapping("/{userId}")
    public AjaxResult delete(@PathVariable Integer userId)
    {
        if (!USERS.isEmpty() && USERS.containsKey(userId))
        {
            USERS.remove(userId);
            return success();
        }
        else
        {
            return error("用户不存在");
        }
    }
}

@ApiModel(value = "UserEntity", description = "用户实体")
class UserEntity {
    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("用户名称")
    private String username;

    @ApiModelProperty("用户密码")
    private String password;

    @ApiModelProperty("用户手机")
    private String mobile;

    public UserEntity() {

    }

    public UserEntity(Integer userId, String username, String password, String mobile) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.mobile = mobile;
    }

    public Integer getUserId()
    {
        return userId;
    }

    public void setUserId(Integer userId)
    {
        this.userId = userId;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }
}
