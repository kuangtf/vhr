package com.vhr.ktf.web.controller.common;

import com.google.code.kaptcha.Producer;
import com.vhr.ktf.common.config.VhrConfig;
import com.vhr.ktf.common.constant.Constants;
import com.vhr.ktf.common.core.domain.AjaxResult;
import com.vhr.ktf.common.core.redis.RedisCache;
import com.vhr.ktf.common.utils.sign.Base64;
import com.vhr.ktf.common.utils.uuid.IdUtils;
import com.vhr.ktf.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 验证码操作处理
 *
 * @author 11628
 */
@RestController
public class CaptchaController {

    // 在验证码配置类中已经将 Producer 的实现类对象注入进 IOC 容器中并且设置了名字
    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;

    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private ISysConfigService configService;

    /**
     * 生成验证码，每次进入登录或注册页面时，或点击验证码时都会访问此方法
     */
    @GetMapping("/captchaImage")
    public AjaxResult getCode() {
        AjaxResult ajax = AjaxResult.success();
        // 查询验证码是否开关
        boolean captchaOnOff = configService.selectCaptchaOnOff();
        // 前端需要看是否显示验证码
        ajax.put("captchaOnOff", captchaOnOff);
        if (!captchaOnOff) {
            return ajax;
        }
        // 生成一个 UUID, 后续与验证码一块存入 Redis
        String uuid = IdUtils.simpleUUID();
        String verifyKey = Constants.CAPTCHA_CODE_KEY + uuid;

        String capStr, code = null;
        BufferedImage image = null;

        // 生成验证码，从配置文件中查询验证码类型：数学型、字符型
        String captchaType = VhrConfig.getCaptchaType();
        if ("math".equals(captchaType)) {
            // Google 的验证码工具，数学型验证码
            String capText = captchaProducerMath.createText();
            capStr = capText.substring(0, capText.lastIndexOf("@"));
            code = capText.substring(capText.lastIndexOf("@") + 1);
            image = captchaProducerMath.createImage(capStr);
        } else if ("char".equals(captchaType)) {
            // Google 的验证码工具，字符型验证码
            capStr = code = captchaProducer.createText();
            image = captchaProducer.createImage(capStr);
        }

        // 将验证码（字符或数字）和其对应的 UUID 存入 Redis 中，并且设置过期时间为 2 分钟
        redisCache.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);

        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try {
           if (image != null) {
               ImageIO.write(image, "jpg", os);
           }
        } catch (IOException e) {
            return AjaxResult.error(e.getMessage());
        }

        // 这里将 UUID 和验证码返回给前端，在用户登录或注册的时候会将此 UUID
        // 和验证码封装到用户登录对象或用户注册对象中，用于后续验证码的校验
        ajax.put("uuid", uuid);
        ajax.put("img", Base64.encode(os.toByteArray()));

        return ajax;
    }
}
