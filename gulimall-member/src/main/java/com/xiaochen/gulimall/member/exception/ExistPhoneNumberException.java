package com.xiaochen.gulimall.member.exception;

public class ExistPhoneNumberException extends RuntimeException{

    public ExistPhoneNumberException(){
        super("异常:手机号已存在");
    }
}
