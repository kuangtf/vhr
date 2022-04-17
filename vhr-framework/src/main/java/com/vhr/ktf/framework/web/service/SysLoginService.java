package com.vhr.ktf.framework.web.service;

import com.vhr.ktf.common.constant.Constants;
import com.vhr.ktf.common.core.domain.entity.SysUser;
import com.vhr.ktf.common.core.domain.model.LoginUser;
import com.vhr.ktf.common.core.redis.RedisCache;
import com.vhr.ktf.common.exception.ServiceException;
import com.vhr.ktf.common.exception.user.CaptchaException;
import com.vhr.ktf.common.exception.user.CaptchaExpireException;
import com.vhr.ktf.common.exception.user.UserPasswordNotMatchException;
import com.vhr.ktf.common.utils.DateUtils;
import com.vhr.ktf.common.utils.MessageUtils;
import com.vhr.ktf.common.utils.ServletUtils;
import com.vhr.ktf.common.utils.ip.IpUtils;
import com.vhr.ktf.framework.manager.AsyncManager;
import com.vhr.ktf.framework.manager.factory.AsyncFactory;
import com.vhr.ktf.system.service.ISysConfigService;
import com.vhr.ktf.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 登录校验方法
 * 
 * @author vhr.ktf
 */
@Service
public class SysLoginService
{
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    /**
     * 登录验证
     * 
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid) {
        // 验证码开关
        boolean captchaOnOff = configService.selectCaptchaOnOff();
        if (captchaOnOff) {
            // 校验验证码，验证码不符合报异常
            validateCaptcha(username, code, uuid);
        }
        // 用户验证，Authentication 就相当于一个认证令牌
        Authentication authentication;
        try {
            // 该方法会去调用 UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        }
        catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            }
            else {
                AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        // 身份信息，大部分情况下返回的是 UserDetails 接口的实现类，UserDetails 代表用户的详细
        //信息，那从 Authentication 中取出来的 UserDetails 就是当前登录用户信息
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        // 记录登记信息
        recordLoginInfo(loginUser.getUserId());
        // 生成 token
        return tokenService.createToken(loginUser);
    }

    /**
     * 校验验证码
     * 
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     */
    public void validateCaptcha(String username, String code, String uuid) {
        // 从 Redis 中查询出生成的验证码
        String verifyKey = Constants.CAPTCHA_CODE_KEY + uuid;
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        // 如果查出来的验证码为空说明验证码过期了
        if (captcha == null) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
            throw new CaptchaExpireException();
        }
        // 校验验证码
        if (!code.equalsIgnoreCase(captcha)) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
            throw new CaptchaException();
        }
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(IpUtils.getIpAddr(ServletUtils.getRequest()));
        sysUser.setLoginDate(DateUtils.getNowDate());
        userService.updateUserProfile(sysUser);
    }
}
