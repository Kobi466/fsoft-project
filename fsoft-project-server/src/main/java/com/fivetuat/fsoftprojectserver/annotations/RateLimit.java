package com.fivetuat.fsoftprojectserver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fivetuat.fsoftprojectserver.enums.RateLimitKeyType;

@Target(
    {ElementType.METHOD, ElementType.TYPE}
)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    
    String name() default "";

    
    long limit() default 60;

    
    long period() default 60;

    
    RateLimitKeyType keyType() default RateLimitKeyType.USER_OR_IP;
}
