package com.xiaochen.gulimall.product.exception;

import com.xiaochen.common.exception.BizCodeEnum;
import com.xiaochen.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一异常处理
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.xiaochen.gulimall.product.controller")
public class GuliMallExceptionControllerAdvice {

    /**
     * 处理校验失败异常
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据校验出现异常{},异常类型{}",e.getMessage(),e.getClass());
        BindingResult res=e.getBindingResult();
        Map<String,String> errorMap=new HashMap<>();
        res.getFieldErrors().forEach((fieldError)->{
            errorMap.put(fieldError.getField(),fieldError.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), BizCodeEnum.VAILD_EXCEPTION.getMessage()).put("data",errorMap);
    }

}
