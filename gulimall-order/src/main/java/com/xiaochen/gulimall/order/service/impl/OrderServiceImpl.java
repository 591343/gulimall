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


        //同步
        CompletableFuture.allOf(memberCompletableFuture,itemsCompletableFuture).get();
        //防重令牌，防止重复提交订单使用Token机制
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
        //1.验证令牌对比和删除必须是原子性的，防止快速重复提交。因此使用Lua脚本保证原子性操作，返回0验证失败，1成功
        String script="if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        Long res = redis.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(OrderConstant.USER_TOKEN_PREFIX + memberInfoTo.getId()), orderToken);

        if(res==0){
            response.setCode(1);
            return response;
        }else {
            //1.创建订单
            OrderCreateTo order = createOrder();
            //2.验价
            BigDecimal payPrice = vo.getPayPrice();
            BigDecimal payAmount = order.getOrder().getPayAmount();
            if(payAmount.subtract(payPrice).abs().compareTo(new BigDecimal("0.01")) < 0){ // 差价绝对值小于0.01验价成功
                //3 保存订单
                saveOrder(order);
                //4 库存锁定，只要有异常回滚订单
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

                if(r.getCode()==0){  // 锁定成功
                    response.setOrderEntity(order.getOrder());
                    //向延迟队列发送消息
                    rabbitTemplate.convertAndSend(this.eventExchange, this.createOrder, order.getOrder());
                    return response;
                }else { // 锁定失败
                    response.setCode(3);
                    return response;
                }
            }else {
                //验价失败
                response.setCode(2);
            }

            //TODO 提交订单
        }
        return null;
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        log.info("\n收到过期的订单信息--准关闭订单:" + entity.getOrderSn());
        // 查询这个订单的最新状态
        OrderEntity orderEntity = this.getById(entity.getId());
        if(orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()){
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            // 发送给MQ告诉它有一个订单被自动关闭了
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            try {
                // 保证消息 100% 发出去 每一个消息在数据库保存详细信息,防订单因为网络延时发送消息过慢，库存未能成功解锁。
                // 定期扫描数据库 将失败的消息在发送一遍
                rabbitTemplate.convertAndSend(eventExchange, ReleaseOtherKey , orderTo);
            } catch (AmqpException e) {
                // 将没发送成功的消息进行重试发送.
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
        
        //保存订单项信息
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(secKillOrderTo.getOrderSn());
        orderItemEntity.setRealAmount(payAmount);
        orderItemEntity.setSkuQuantity(secKillOrderTo.getNum());
        orderItemService.save(orderItemEntity);
    }


    /**
     * 保存订单数据
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
        // 1. 生成订单号
        String timeId = IdWorker.getTimeId();
        timeId=timeId.substring(0,OrderConstant.LENGTH_ORDER_NUMBER);  //订单号为12位
        OrderEntity orderEntity = buildOrderSn(timeId);
        // 2. 获取所有订单项
        List<OrderItemEntity> items = buildOrderItems(timeId);
        // 3. 验价
        computePrice(orderEntity,items);
        order.setOrder(orderEntity);
        order.setOrderItems(items);

        return order;
    }


    /**
     * 验价
     * @param orderEntity
     * @param items
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> items) {
        BigDecimal totalPrice = new BigDecimal("0.0");
        // 叠加每一个订单项的金额
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");
        for (OrderItemEntity item : items) {
            // 优惠券的金额
            coupon = coupon.add(item.getCouponAmount());
            // 积分优惠的金额
            integration = integration.add(item.getIntegrationAmount());
            // 打折的金额
            promotion = promotion.add(item.getPromotionAmount());
            BigDecimal realAmount = item.getRealAmount();
            totalPrice = totalPrice.add(realAmount);

            // 购物获取的积分、成长值
            gift.add(new BigDecimal(item.getGiftIntegration().toString()));
            growth.add(new BigDecimal(item.getGiftGrowth().toString()));
        }
        // 1.订单价格相关 总额、应付总额
        orderEntity.setTotalAmount(totalPrice);
        orderEntity.setPayAmount(totalPrice.add(orderEntity.getFreightAmount()));

        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);

        // 设置积分、成长值
        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());

        // 设置订单的删除状态
        orderEntity.setDeleteStatus(OrderStatusEnum.CREATE_NEW.getCode());
    }

    /**
     * 构建一个订单
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
        // 2. 获取收获地址信息
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
        // 设置订单状态
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);
        return entity;
    }

    /**
     * 为 orderSn 订单构建订单数据
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        // 这里是最后一次来确认购物项的价格 这个远程方法还会查询一次数据库
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
     * 构建某一个订单项
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        // 1.订单信息： 订单号
        // 2.商品spu信息
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSkuInfoBySkuId(skuId);
        SpuInfoVo spuInfo = r.getData(new TypeReference<SpuInfoVo>() {});
        itemEntity.setSpuId(spuInfo.getId());
        itemEntity.setSpuBrand(spuInfo.getBrandId().toString());
        itemEntity.setSpuName(spuInfo.getSpuName());
        itemEntity.setCategoryId(spuInfo.getCatalogId());
        // 3.商品的sku信息
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        // 把一个集合按照指定的字符串进行分割得到一个字符串
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());
        // 4.积分信息 买的数量越多积分越多 成长值越多
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
        // 5.订单项的价格信息 优惠金额
        itemEntity.setPromotionAmount(new BigDecimal("0.0"));
        itemEntity.setCouponAmount(new BigDecimal("0.0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0.0"));
        // 当前订单项的实际金额
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        // 减去各种优惠的价格
        BigDecimal subtract = orign.subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getPromotionAmount()).subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);
        return itemEntity;
    }
}