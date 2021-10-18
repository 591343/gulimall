package com.xiaochen.cart;

import com.xiaochen.cart.vo.CartItem;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class GulimallCartApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void test1(){
        CartItem cartItem = new CartItem();
        cartItem.setPrice(new BigDecimal("-1123.123"));
        cartItem.setCount(5);
        System.out.println(cartItem.getTotalPrice());

        System.out.println(cartItem.getPrice().compareTo(new BigDecimal("0")));
    }
}
