package com.xiaochen.cart.to;

import lombok.Data;
import lombok.ToString;

/**
 * 用户状态信息
 */
@ToString
@Data
public class UserInfoTo {
    private Long userId;
    private String userKey;
    private String username;
    /**
     * 判断是否是临时用户
     */
    private boolean tempUser = false;


}
