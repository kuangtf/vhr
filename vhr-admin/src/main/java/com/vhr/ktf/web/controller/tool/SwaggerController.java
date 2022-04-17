package com.vhr.ktf.web.controller.tool;

import com.vhr.ktf.common.core.controller.BaseController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * swagger 接口
 * 
 * @author vhr.ktf
 */
@Controller
@RequestMapping("/tool")
public class SwaggerController extends BaseController {

    @PreAuthorize("@ss.hasPermi('tool:swagger:view')")
    @GetMapping("/swagger")
    public String index() {
        return redirect("/swagger-ui.html");
    }
}
