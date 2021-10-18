package com.xiaochen.cart.vo;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车VO
 */
@Data
public class Cart {
    private List<CartItem> items;
    private Integer count; //商品数量
    private Integer countType; //商品种类
    private BigDecimal totalAmount; //商品总价
    private BigDecimal reduce=new BigDecimal("0.00"); //减免价格

    public Integer getCount() {
        Integer count=0;
        if(this.items!=null&&this.items.size()!=0){
            for (CartItem item : this.items) {
                count+=item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        Integer countType=0;
        if(this.items!=null&&this.items.size()!=0){
            countType=items.size();
        }
        return countType;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal bigDecimal = new BigDecimal("0.00");
        if(this.items!=null&&this.items.size()!=0){
            for (CartItem item : this.items) {
                 if(item.getCheck())
                    bigDecimal=bigDecimal.add(item.getTotalPrice());
            }
        }
        bigDecimal = bigDecimal.subtract(getReduce());
        if(bigDecimal.compareTo(new BigDecimal("0.00"))<=0){
            bigDecimal=new BigDecimal("0.00");
        }

        return bigDecimal;
    }
}
