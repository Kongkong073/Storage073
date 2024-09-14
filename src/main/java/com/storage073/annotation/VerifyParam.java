package com.storage073.annotation;


import com.storage073.entity.enums.VerifyEnum;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VerifyParam {
    boolean required() default false;

    int max() default -1;
    int min() default -1;

    VerifyEnum regex() default VerifyEnum.NO;


}

