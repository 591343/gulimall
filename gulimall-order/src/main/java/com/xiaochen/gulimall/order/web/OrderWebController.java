package com.xiaochen.gulimall.order.web;


import com.xiaochen.common.exception.NotStockException;
import com.xiaochen.gulimall.order.service.OrderService;
import com.xiaochen.gulimall.order.vo.OrderConfirmVo;
import com.xiaochen.gulimall.order.vo.request.OrderSubmitVo;
import com.xiaochen.gulimall.order.vo.response.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();

        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }

    /**
     * 提交下单
     * @param vo
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo,Model model, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {

        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
            System.out.println(vo);
            // 下单失败回到订单重新确认订单信息
            if(responseVo.getCode() == 0){
                // 下单成功取支付选项
                model.addAttribute("submitOrderResp", responseVo);
                return "pay";
            }else{
                String msg = "下单失败";
                switch (responseVo.getCode()){
                    case 1: msg += "订单信息过期,请刷新在提交";break;
                    case 2: msg += "订单商品价格发送变化,请确认后再次提交";break;
                    case 3: msg += "商品库存不足";break;
                }
                redirectAttributes.addFlashAttribute("msg", msg);
                return "redirect:http://order.gulimall.com/toTrade";
            }
        } catch (Exception e) {
            if (e instanceof NotStockException){
                String message = e.getMessage();
                redirectAttributes.addFlashAttribute("msg", message);
            }
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
