package com.xiaochen.gulimall.ware.vo.request;

import lombok.Data;
import java.util.List;

@Data
public class PurchaseVo {
    private Long purchaseId; //整单id
    private List<Long> items; //合并项集合
}
