package com.vhr.ktf;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @author github.com/kuangtf
 * @date 2022/4/17 10:56
 */
public class VhrServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(VhrApplication.class);
    }
}
