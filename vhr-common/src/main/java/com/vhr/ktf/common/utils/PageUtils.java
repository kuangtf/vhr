package com.vhr.ktf.common.utils;

import com.github.pagehelper.PageHelper;
import com.vhr.ktf.common.core.page.PageDomain;
import com.vhr.ktf.common.core.page.TableSupport;
import com.vhr.ktf.common.utils.sql.SqlUtil;

/**
 * 分页工具类
 * 
 * @author vhr.ktf
 */
public class PageUtils extends PageHelper {

    /**
     * 设置请求分页数据
     */
    public static void startPage() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        if (StringUtils.isNotNull(pageNum) && StringUtils.isNotNull(pageSize)) {
            // 可以完成排序操作
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            Boolean reasonable = pageDomain.getReasonable();
            PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
        }
    }
}
