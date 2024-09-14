package com.storage073.aspect;


import com.storage073.annotation.GlobalInterceptor;
import com.storage073.annotation.VerifyParam;
import com.storage073.entity.Constants;
import com.storage073.entity.dto.SessionWebUserDto;
import com.storage073.entity.enums.ResponseCodeEnum;
import com.storage073.entity.enums.VerifyEnum;
import com.storage073.exception.BusinessException;
import com.storage073.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;


@Aspect
@Component
@Slf4j
public class OperationAspect {


    /*
    * 定义切入点
    * */
    @Pointcut("@annotation(com.storage073.annotation.GlobalInterceptor)")
    private void requestInterceptor(){

    }

    /*
    * 引用切入点，增强逻辑
    * */
    @Around("requestInterceptor()")
    public Object interceptorDo(ProceedingJoinPoint point) throws Throwable {
        try {
            Object target = point.getTarget();
            Object[] arguments = point.getArgs();
            String methodName = point.getSignature().getName();
            Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();
            Method method = target.getClass().getMethod(methodName, parameterTypes);

            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);

            if (interceptor == null) {
                return point.proceed(); // 如果没有GlobalInterceptor注解，直接执行方法
            }

            // 校验登录
            if (interceptor.checkLogin() || interceptor.checkAdmin()) {
                checkLogin(interceptor.checkAdmin());
            }

            // 校验参数
            if (interceptor.checkParams()) {
                validateParams(method, arguments);
            }

            // 继续执行被拦截的方法
            return point.proceed();

        } catch (Exception e) {
            log.error("Interceptor error", e);
            throw e; // 抛出异常，让全局异常处理类去处理
        }
    }

    //校验登录
    private void checkLogin(Boolean checkAdmin) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        SessionWebUserDto sessionUser = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
//        if (sessionUser == null && appConfig.getDev() != null && appConfig.getDev()) {
//            //TODO
//        }
        if (null == sessionUser) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        // 检查是否需要管理员身份
        if (checkAdmin && !sessionUser.getAdmin()) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
    }

    private void validateParams(Method method, Object[] arguments) {

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object value = arguments[i];

            VerifyParam verifyParam = parameter.getAnnotation(VerifyParam.class);
            if (verifyParam != null) {
                // 校验required
                if (verifyParam.required() && (value == null || value.toString().trim().isEmpty())) {
                    throw new IllegalArgumentException("Parameter " + parameter.getName() + " is required");
                }


                // 校验min
                if (verifyParam.min() != -1 && value != null && value.toString().length() < verifyParam.min()) {
                    throw new IllegalArgumentException("Parameter length" + parameter.getName() + " must be >= " + verifyParam.min());
                }

                // 校验max
                if (verifyParam.max() != -1 && value != null && value.toString().length() > verifyParam.max()) {
                    throw new IllegalArgumentException("Parameter length" + parameter.getName() + " must be <= " + verifyParam.max());
                }

                // 校验正则表达式
                if (verifyParam.regex() != VerifyEnum.NO) {
                    // 这里假设有一些正则表达式的预定义
                    String regex = getRegexPattern(verifyParam.regex());
                    if (value != null && !value.toString().matches(regex)) {
                        throw new IllegalArgumentException("Parameter " + parameter.getName() + " does not match the required pattern");
                    }
                }
            }
        }
    }

    private String getRegexPattern(VerifyEnum regexEnum) {
        switch (regexEnum) {
            case NO:
                return ""; // 不校验
            case POSITIVE_INTEGER:
                return regexEnum.getRegex(); // 正整数
            case EMAIL:
                return regexEnum.getRegex(); // 邮箱
            case PHONE:
                return regexEnum.getRegex(); // 手机号
            case PASSWORD:
                return regexEnum.getRegex(); // 复杂密码校验
            case ACCOUNT:
                return regexEnum.getRegex(); // 账号（字母开头，字母数字下划线组合）
            default:
                throw new IllegalArgumentException("Unknown regex type: " + regexEnum);
        }
    }


}
