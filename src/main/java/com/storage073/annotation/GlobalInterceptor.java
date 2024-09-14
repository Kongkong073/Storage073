package com.storage073.annotation;


import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GlobalInterceptor {

    /*
    * 是否要登陆
    **/
    boolean checkLogin() default true;

    /*
    * 是否要校验参数
    * */
    boolean checkParams() default  false;

    /*
     * 是否使管理员
     * */
    boolean checkAdmin() default false;

    /*
    * 校验频次
    * */

}
