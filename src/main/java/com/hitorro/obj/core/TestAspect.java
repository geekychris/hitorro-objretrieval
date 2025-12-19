package com.hitorro.obj.core;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class TestAspect {
    @Around("execution(* com.hitorro.obj.core.Test.hello1(java.lang.Integer)) && args(hi)")
    public String hello1(ProceedingJoinPoint thisJoinPoint, Integer hi) throws Throwable {
        Object o = thisJoinPoint.proceed(new Object[]{hi});
        return "helloooooow from aspect";
    }

    /*
    @Pointcut("execution(* *.actionPerformed(java.awt.event.ActionEvent)) " +
        "&& args(actionEvent) && if()")
     */
    @Around("execution(* com.hitorro.obj.core.Test.hello())")
    public String hello(ProceedingJoinPoint thisJoinPoint) throws Throwable {
        Object o = thisJoinPoint.proceed(new Object[]{});
        return "hellooo from aspect";
    }
}
