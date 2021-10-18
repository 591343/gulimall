package com.xiaochen.gulimall.product.vo.respone;

import com.baomidou.mybatisplus.annotation.TableId;
import com.xiaochen.gulimall.product.vo.AttrVo;
import lombok.Data;

@Data
public class AttrRespVo extends AttrVo {
    private static final long serialVersionUID = 1L;

    /**
     * 所属分组名字
     */
    private String groupName;

    /**
     * 所属分类
     */
    private String catelogName;

    private Long [] catelogPath;
}
