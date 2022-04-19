package com.uio.monitor.aop;

import com.alibaba.fastjson.JSON;
import com.uio.monitor.common.BackEnum;
import com.uio.monitor.common.BackMessage;
import com.uio.monitor.common.CustomException;
import com.uio.monitor.controller.base.UserToken;
import com.uio.monitor.utils.ThreadLocalUtils;
import com.uio.monitor.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;

/**
 * @author han xun
 * Date 2021-10-08 14:42
 * Description HTTP AOP
 */
@Aspect
@Slf4j
@Component
public class WebInterceptorAop {

    public final static String TOKEN = "token";

    @Value("${token.secret}")
    private String tokenSecret;

    @Pointcut("execution (* com.uio.*.controller.*.*(..))")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object aroundAdvice(ProceedingJoinPoint proceedingJoinPoint) {

        // 进入请求，打印入惨
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)requestAttributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String method = request.getMethod();
        StringBuilder params = new StringBuilder();
        if (HttpMethod.GET.toString().equals(method)) {
            // GET 请求
            String queryString = request.getQueryString();
            if (!StringUtils.isEmpty(queryString)) {
                params.append(queryString);
            }
        } else {
            // POST 等 请求
            Object[] paramArray = proceedingJoinPoint.getArgs();
            for (Object o : paramArray) {
                params.append(JSON.toJSONString(o)).append(",");
            }
        }
        log.info("start execute url:{}, method:{}, params:{}", request.getRequestURI(), method, params);

        // 执行业务逻辑
        Object o = null;
        try {
            // 登陆态操作
            String token = request.getHeader(TOKEN);
            if (token != null) {
                UserToken userToken = new UserToken();
                userToken.setId(TokenUtils.getIdAndVerify(token, tokenSecret));
                ThreadLocalUtils.addCurrentUser(userToken);
            }
            o = proceedingJoinPoint.proceed();
            return o;
        } catch (CustomException e) {
            log.warn("自定义异常捕获:{} ", e.getErrorMsg(), e);
            return new BackMessage<Void>(e.getErrorCode(), e.getErrorMsg());
        } catch (HttpRequestMethodNotSupportedException e) {
            log.warn("捕捉浏览器错误请求异常,", e);
            return new BackMessage<Void>(BackEnum.REQUEST_METHOD_ERROR);
        } catch (MissingServletRequestParameterException e) {
            log.warn("捕捉错误参数请求异常, ", e);
            return new BackMessage<Void>(BackEnum.PARAM_ERROR);
        } catch (HttpMessageNotReadableException e) {
            log.warn("捕捉错误JSON请求有数据异常, ", e);
            return new BackMessage<Void>(BackEnum.DATA_ERROR);
        } catch (DuplicateKeyException e) {
            log.warn("主键重复添加, ", e);
            return new BackMessage<Void>(BackEnum.REPETITION);
        } catch (HttpMediaTypeNotSupportedException e) {
            log.warn("数据类型错误,json<->form-urlencoded, ", e);
            return new BackMessage<Void>(BackEnum.MEDIA_TYPE_ERROR);
        } catch (Throwable t) {
            log.error("系统异常", t);
            return new BackMessage<Void>(BackEnum.UNKNOWN_ERROR);
        } finally {
            // 执行完成，打印出参
            log.info("finish execute url:{}, method:{}, return:{}", request.getRequestURI(), method, JSON.toJSONString(o));
            ThreadLocalUtils.removeCurrentUser();
        }
    }

    @Before("pointcut()")
    public void beforeAdvice() {
    }

    @After("pointcut()")
    public void afterAdvice() {
    }
}
