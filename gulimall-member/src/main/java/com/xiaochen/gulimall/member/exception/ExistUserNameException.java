package com.xiaochen.gulimall.member.exception;

public class ExistUserNameException extends RuntimeException{

    public ExistUserNameException(){
        super("异常:用户名已存在");
    }
}
