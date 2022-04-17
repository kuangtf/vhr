package com.vhr.ktf.framework.manager;

import com.vhr.ktf.common.utils.Threads;
import com.vhr.ktf.common.utils.spring.SpringUtils;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 异步任务管理器
 * 
 * @author vhr.ktf
 */
public class AsyncManager {

    /**
     * 操作延迟10毫秒
     */
    private static final int OPERATE_DELAY_TIME = 10;

    /**
     * 异步操作任务调度线程池
     */
    private final ScheduledExecutorService executor = SpringUtils.getBean("scheduledExecutorService");

    /**
     * 单例模式：饿汉式
     */
    private AsyncManager(){}

    private static final AsyncManager ME = new AsyncManager();

    public static AsyncManager me() {
        return ME;
    }

    /**
     * 执行任务
     * 
     * @param task 任务，TimerTask 实现了 Runnable 接口，其实就是一个任务
     */
    public void execute(TimerTask task) {
        // 实质是调用定时线程池中的方法,在指定的时间之后执行该任务, 参数：执行的任务  延迟时间  时间单位
        executor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 停止任务线程池
     */
    public void shutdown() {
        Threads.shutdownAndAwaitTermination(executor);
    }
}
