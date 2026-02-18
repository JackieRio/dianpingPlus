package com.hmdp.utils.ratelimiter;

import java.lang.annotation.*;

// 表明该注解用于方法上
@Target(ElementType.METHOD)
// 表明该注解在运行时存在，实现动态限流
@Retention(RetentionPolicy.RUNTIME)
// 表明该注解应该被javadoc记录
@Documented
public @interface RateLimiter {
    /**
     * 限流key前缀
     */
    String key() default "rate_limit:";

    /**
     * 时间窗口大小（秒）
     */
    int window() default 10;

    /**
     * 时间窗口内允许的请求数
     */
    int limit() default 20;

    /**
     * 限流提示信息
     */
    String message() default "系统繁忙，请稍后再试";

    /**
     * 限流维度（默认按方法限流）
     */
    LimitType type() default LimitType.METHOD;

    enum LimitType {
        /**
         * 按调用方IP限流
         */
        IP,
        /**
         * 按用户ID限流
         */
        USER,
        /**
         * 按方法限流/全局限流（默认）
         */
        METHOD
    }
}