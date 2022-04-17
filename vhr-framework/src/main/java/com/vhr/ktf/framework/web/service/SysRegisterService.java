package com.vhr.ktf.framework.web.service;

import com.vhr.ktf.common.constant.Constants;
import com.vhr.ktf.common.constant.UserConstants;
import com.vhr.ktf.common.core.domain.entity.SysUser;
import com.vhr.ktf.common.core.domain.model.RegisterBody;
import com.vhr.ktf.common.core.redis.RedisCache;
import com.vhr.ktf.common.exception.user.CaptchaException;
import com.vhr.ktf.common.exception.user.CaptchaExpireException;
import com.vhr.ktf.common.utils.MessageUtils;
import com.vhr.ktf.common.utils.SecurityUtils;
import com.vhr.ktf.common.utils.StringUtils;
import com.vhr.ktf.framework.manager.AsyncManager;
import com.vhr.ktf.framework.manager.factory.AsyncFactory;
import com.vhr.ktf.system.service.ISysConfigService;
import com.vhr.ktf.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 注册校验方法
 * 
 * @author vhr.ktf
 */
@Component
public class SysRegisterService {

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    /**
     * 注册
     */
    public String register(RegisterBody registerBody) {

        String msg = "", username = registerBody.getUsername(), password = registerBody.getPassword();
        // 查询是否开启验证码
        boolean captchaOnOff = configService.selectCaptchaOnOff();
        // 验证码开关
        if (captchaOnOff) {
            validateCaptcha(registerBody.getCode(), registerBody.getUuid());
        }

        if (StringUtils.isEmpty(username)) {
            msg = "用户名不能为空";
        } else if (StringUtils.isEmpty(password)) {
            msg = "用户密码不能为空";
        }
        // 限制用户名长度
        else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            msg = "账户长度必须在2到20个字符之间";
        }
        // 限制密码长度
        else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            msg = "密码长度必须在5到20个字符之间";
        }
        // 判断用户是否已存在
        else if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(username))) {
            msg = "保存用户'" + username + "'失败，注册账号已存在";
        }
        else {
            SysUser sysUser = new SysUser();
            sysUser.setUserName(username);
            // 昵称初始化和用户名一样
            sysUser.setNickName(username);
            // 将密码加密
            sysUser.setPassword(SecurityUtils.encryptPassword(registerBody.getPassword()));
            boolean regFlag = userService.registerUser(sysUser);
            if (!regFlag) {
                msg = "注册失败,请联系系统管理人员";
            } else {
                AsyncManager.me().execute(
                        AsyncFactory.recordLoginInfo(username, Constants.REGISTER, MessageUtils.message("user.register.success")));
            }
        }
        return msg;
    }

    /**
     * 校验验证码
     *
     * @param code 验证码
     * @param uuid 唯一标识
     */
    public void validateCaptcha(String code, String uuid) {
        // 从 redis 中查询出对应的验证码
        String verifyKey = Constants.CAPTCHA_CODE_KEY + uuid;
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null) {
            // 如果验证码为空说明验证码超时了
            throw new CaptchaExpireException();
        }
        // 将用户输入的验证码和生成的验证码比较
        if (!code.equalsIgnoreCase(captcha)) {
            throw new CaptchaException();
        }
    }
}
