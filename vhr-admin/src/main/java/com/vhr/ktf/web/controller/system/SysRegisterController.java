package com.vhr.ktf.web.controller.system;

import com.vhr.ktf.common.core.controller.BaseController;
import com.vhr.ktf.common.core.domain.AjaxResult;
import com.vhr.ktf.common.core.domain.model.RegisterBody;
import com.vhr.ktf.common.utils.StringUtils;
import com.vhr.ktf.framework.web.service.SysRegisterService;
import com.vhr.ktf.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册验证
 * 
 * @author vhr.ktf
 */
@RestController
public class SysRegisterController extends BaseController {

    private static final String REGISTER = "true";

    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private ISysConfigService configService;

    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user) {
        // 判断系统是否开启注册功能
        if (!(REGISTER.equals(configService.selectConfigByKey("sys.account.registerUser")))) {
            return error("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }
}
