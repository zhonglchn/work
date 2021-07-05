package com.leyou.common.auth.pojo;

public class UserHolder {

    //让对象与线程绑定的对象叫线程的局部变量ThreadLocal，可以保证多线程操作时，存储的对象是互不影响的
    private static final ThreadLocal<Long> TL = new ThreadLocal<>();

    public static void setUserId(Long userId){
        TL.set(userId);
    }

    public static Long getUserId(){
        return TL.get();
    }

    public static void reMoveUserId(){
        TL.remove();
    }

}