package com.boraydata.exception;

/** 用来处理异常的 类
 * @author bufan
 * @data 2021/7/28
 */
public class TaildirException extends RuntimeException{
    private static final long seriaVersionUID = 1L;

    public TaildirException(String msg,Throwable th){
        super(msg,th);
    }
    public TaildirException(String msg){
        super(msg);
    }
}
