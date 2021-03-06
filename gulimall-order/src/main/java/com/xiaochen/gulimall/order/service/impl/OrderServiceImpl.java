package com.xiaochen.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.xiaochen.common.constant.AuthServerConstant;
import com.xiaochen.common.enume.OrderStatusEnum;
import com.xiaochen.common.to.HasStockTo;
import com.xiaochen.common.to.MemberInfoTo;
import com.xiaochen.common.to.mq.OrderTo;
import com.xiaochen.common.to.mq.SecKillOrderTo;
import com.xiaochen.common.utils.R;
import com.xiaochen.gulimall.order.config.LoginUserInterceptor;
import com.xiaochen.gulimall.order.constant.OrderConstant;
import com.xiaochen.gulimall.order.dao.OrderItemDao;
import com.xiaochen.gulimall.order.entity.OrderItemEntity;
import com.xiaochen.gulimall.order.feign.CartFeignService;
import com.xiaochen.gulimall.order.feign.MemberFeignService;
import com.xiaochen.gulimall.order.feign.ProductFeignService;
import com.xiaochen.gulimall.order.feign.WmsFeignService;
import com.xiaochen.gulimall.order.service.OrderItemService;
import com.xiaochen.gulimall.order.to.OrderCreateTo;
import com.xiaochen.gulimall.order.vo.MemberAddressVo;
import com.xiaochen.gulimall.order.vo.OrderConfirmVo;
import com.xiaochen.gulimall.order.vo.OrderItemVo;
import com.xiaochen.gulimall.order.vo.request.FareVo;
import com.xiaochen.gulimall.order.vo.request.OrderSubmitVo;
import com.xiaochen.gulimall.order.vo.request.SpuInfoVo;
import com.xiaochen.gulimall.order.vo.request.WareSkuLockVo;
import com.xiaochen.gulimall.order.vo.response.SubmitOrderResponseVo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.Query;

import com.xiaochen.gulimall.order.dao.OrderDao;
import com.xiaochen.gulimall.order.entity.OrderEntity;
import com.xiaochen.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    WmsFeignService wmsFeignService;
    @Autowired
    StringRedisTemplate redis;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Value("${myRabbitmq.MQConfig.eventExchange}")
    private String eventExchange;

    @Value("${myRabbitmq.MQConfig.createOrder}")
    private String createOrder;

    @Value("${myRabbitmq.MQConfig.ReleaseOtherKey}")
    private String ReleaseOtherKey;

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {

        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberInfoTo memberInfoTo = LoginUserInterceptor.threadlocal.get();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> memberCompletableFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            List<MemberAddressVo> memberAddressVos = memberFeignService.memberAddress(memberInfoTo.getId());
            orderConfirmVo.setAddress(memberAddressVos);
        }, executor);

        CompletableFuture<Void> itemsCompletableFuture = CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            orderConfirmVo.setItems(items);
            return items;
        }, executor).thenAcceptAsync((res) -> {
            RequestContextHolder.setRequestAttributes(attributes);
            List<Long> collect = res.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R r = wmsFeignService.hasStock(collect);
            List<HasStockTo> data = r.getData(new TypeReference<List<HasStockTo>>(){});
            if(data!=null){
                Map<Long, Boolean> stocks= data.stream().collect(Collectors.toMap(HasStockTo::getSkuId, HasStockTo::getHasStock));
                orderConfirmVo.setStocks(stocks);
            }
        }, executor);


        //??????
        CompletableFuture.allOf(memberCompletableFuture,itemsCompletableFuture).get();
        //?????????????????????????????????????????????Token??????
        UUID uuid = UUID.randomUUID();
        String token = uuid.toString().replaceAll("-", "");
        redis.opsForValue().set(OrderConstant.USER_TOKEN_PREFIX+memberInfoTo.getId(),token,30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);

        orderConfirmVo.setIntegration(memberInfoTo.getIntegration());
        return orderConfirmVo;
    }


    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderResponseVo response = new SubmitOrderResponseVo();
        MemberInfoTo memberInfoTo = LoginUserInterceptor.threadlocal.get();
        String orderToken= vo.getOrderToken();
        //1.??????????????????????????????????????????????????????????????????????????????????????????Lua????????????????????????????????????0???????????????1??????
        String script="if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        Long res = redis.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(OrderConstant.USER_TOKEN_PREFIX + memberInfoTo.getId()), orderToken);

        if(res==0){
            response.setCode(1);
            return response;
        }else {
            //1.????????????
            OrderCreateTo order = createOrder();
            //2.??????
            BigDecimal payPrice = vo.getPayPrice();
            BigDecimal payAmount = order.getOrder().getPayAmount();
            if(payAmount.subtract(payPrice).abs().compareTo(new BigDecimal("0.01")) < 0){ // ?????????????????????0.01????????????
                //3 ????????????
                saveOrder(order);
                //4 ??????????????????????????????????????????
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> collect = order.getOrderItems().stream().map((item) -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());

                wareSkuLockVo.setLocks(collect);
                R r = wmsFeignService.lockOrderSku(wareSkuLockVo);

                if(r.getCode()==0){  // ????????????
                    response.setOrderEntity(order.getOrder());
                    //???????????????????????????
                    rabbitTemplate.convertAndSend(this.eventExchange, this.createOrder, order.getOrder());
                    return response;
                }else { // ????????????
                    response.setCode(3);
                    return response;
                }
            }else {
                //????????????
                response.setCode(2);
            }

            //TODO ????????????
        }
        return null;
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        log.info("\n???????????????????????????--???????????????:" + entity.getOrderSn());
        // ?????????????????????????????????
        OrderEntity orderEntity = this.getById(entity.getId());
        if(orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()){
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            // ?????????MQ??????????????????????????????????????????
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            try {
                // ???????????? 100% ????????? ?????????????????????????????????????????????,???????????????????????????????????????????????????????????????????????????
                // ????????????????????? ?????????????????????????????????
                rabbitTemplate.convertAndSend(eventExchange, ReleaseOtherKey , orderTo);
            } catch (AmqpException e) {
                // ?????????????????????????????????????????????.
            }
        }
    }

    @Transactional
    @Override
    public void createSeckillOrder(SecKillOrderTo secKillOrderTo) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(secKillOrderTo.getOrderSn());
        orderEntity.setMemberId(secKillOrderTo.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal payAmount = secKillOrderTo.getSeckillPrice().multiply(new BigDecimal(secKillOrderTo.getNum() + ""));
        orderEntity.setPayAmount(payAmount);
        this.save(orderEntity);
        
        //?????????????????????
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(secKillOrderTo.getOrderSn());
        orderItemEntity.setRealAmount(payAmount);
        orderItemEntity.setSkuQuantity(secKillOrderTo.getNum());
        orderItemService.save(orderItemEntity);
    }


    /**
     * ??????????????????
     * @param order
     */

    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity=order.getOrder();
        orderEntity.setModifyTime(new Date(new java.util.Date().getTime()));
        this.baseMapper.insert(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItems=orderItems.stream().map(item->{
            item.setOrderId(orderEntity.getId());
            item.setOrderSn(orderEntity.getOrderSn());
            return item;
        }).collect(Collectors.toList());
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder(){
        OrderCreateTo order = new OrderCreateTo();
        // 1. ???????????????
        String timeId = IdWorker.getTimeId();
        timeId=timeId.substring(0,OrderConstant.LENGTH_ORDER_NUMBER);  //????????????12???
        OrderEntity orderEntity = buildOrderSn(timeId);
        // 2. ?????????????????????
        List<OrderItemEntity> items = buildOrderItems(timeId);
        // 3. ??????
        computePrice(orderEntity,items);
        order.setOrder(orderEntity);
        order.setOrderItems(items);

        return order;
    }


    /**
     * ??????
     * @param orderEntity
     * @param items
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> items) {
        BigDecimal totalPrice = new BigDecimal("0.0");
        // ?????????????????????????????????
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");
        for (OrderItemEntity item : items) {
            // ??????????????????
            coupon = coupon.add(item.getCouponAmount());
            // ?????????????????????
            integration = integration.add(item.getIntegrationAmount());
            // ???????????????
            promotion = promotion.add(item.getPromotionAmount());
            BigDecimal realAmount = item.getRealAmount();
            totalPrice = totalPrice.add(realAmount);

            // ?????????????????????????????????
            gift.add(new BigDecimal(item.getGiftIntegration().toString()));
            growth.add(new BigDecimal(item.getGiftGrowth().toString()));
        }
        // 1.?????????????????? ?????????????????????
        orderEntity.setTotalAmount(totalPrice);
        orderEntity.setPayAmount(totalPrice.add(orderEntity.getFreightAmount()));

        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);

        // ????????????????????????
        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());

        // ???????????????????????????
        orderEntity.setDeleteStatus(OrderStatusEnum.CREATE_NEW.getCode());
    }

    /**
     * ??????????????????
     */
    private OrderEntity buildOrderSn(String orderSn) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setCreateTime(new Date(new java.util.Date().getTime()));
        entity.setCommentTime(new Date(new java.util.Date().getTime()));
        entity.setReceiveTime(new Date(new java.util.Date().getTime()));
        entity.setDeliveryTime(new Date(new java.util.Date().getTime()));
        MemberInfoTo rsepVo = LoginUserInterceptor.threadlocal.get();
        entity.setMemberId(rsepVo.getId());
        entity.setMemberUsername(rsepVo.getUsername());
        entity.setBillReceiverEmail(rsepVo.getEmail());
        // 2. ????????????????????????
        OrderSubmitVo submitVo = confirmVoThreadLocal.get();
        R fare = wmsFeignService.getFare(submitVo.getAddrId());
        FareVo resp = fare.getData(new TypeReference<FareVo>() {});
        entity.setFreightAmount(resp.getFare());
        entity.setReceiverCity(resp.getMemberAddressVo().getCity());
        entity.setReceiverDetailAddress(resp.getMemberAddressVo().getDetailAddress());
        entity.setDeleteStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setReceiverPhone(resp.getMemberAddressVo().getPhone());
        entity.setReceiverName(resp.getMemberAddressVo().getName());
        entity.setReceiverPostCode(resp.getMemberAddressVo().getPostCode());
        entity.setReceiverProvince(resp.getMemberAddressVo().getProvince());
        entity.setReceiverRegion(resp.getMemberAddressVo().getRegion());
        // ??????????????????
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);
        return entity;
    }

    /**
     * ??? orderSn ????????????????????????
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        // ???????????????????????????????????????????????? ?????????????????????????????????????????????
        List<OrderItemVo> cartItems = cartFeignService.getCurrentUserCartItems();
        List<OrderItemEntity> itemEntities = null;
        if(cartItems != null && cartItems.size() > 0){
            itemEntities = cartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
        }
        return itemEntities;
    }

    /**
     * ????????????????????????
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        // 1.??????????????? ?????????
        // 2.??????spu??????
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSkuInfoBySkuId(skuId);
        SpuInfoVo spuInfo = r.getData(new TypeReference<SpuInfoVo>() {});
        itemEntity.setSpuId(spuInfo.getId());
        itemEntity.setSpuBrand(spuInfo.getBrandId().toString());
        itemEntity.setSpuName(spuInfo.getSpuName());
        itemEntity.setCategoryId(spuInfo.getCatalogId());
        // 3.?????????sku??????
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        // ????????????????????????????????????????????????????????????????????????
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());
        // 4.???????????? ?????????????????????????????? ???????????????
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
        // 5.???????????????????????? ????????????
        itemEntity.setPromotionAmount(new BigDecimal("0.0"));
        itemEntity.setCouponAmount(new BigDecimal("0.0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0.0"));
        // ??????????????????????????????
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        // ???????????????????????????
        BigDecimal subtract = orign.subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getPromotionAmount()).subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);
        return itemEntity;
    }
}