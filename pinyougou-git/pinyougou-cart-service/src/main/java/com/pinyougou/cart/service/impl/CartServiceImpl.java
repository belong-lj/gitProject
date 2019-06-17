package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品SKU(item) ID查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if(item==null){
            throw new RuntimeException("商品不存在");
        }
        if(!item.getStatus().equals("1")){
            throw new RuntimeException("商品状态无效");
        }
        //2.获取商家ID
        String sellerId = item.getSellerId();
        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);
        //4.如果购物车列表中不存在该商家的购物车
        if(cart==null){
            //4.1 新建购物车对象
            cart= new Cart();
            //商家信息
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            //商品列表
            TbOrderItem orderItem=createOrderItem(item,num);
            List<TbOrderItem> orderItemList=new ArrayList<>();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);
        }else{
            //5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            //5.1. 如果没有，新增购物车明细
            if(orderItem==null){
                orderItem= createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);
            }else{
            //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()).multiply(new BigDecimal(orderItem.getPrice().doubleValue())));
                //如果数量操作后小于等于0，则移除
                if(orderItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItem);//移除购物车明细
                }
                //如果移除后cart的明细数量为0，则将cart移除
                if(cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }
            }


        }



        return cartList;
    }



    // 将购物车保存到redis
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis存入购物车数据....."+username);
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    // 从redis中查询购物车
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车数据....."+username);
        List<Cart> cartList = (List<Cart>)redisTemplate.boundHashOps("cartList").get(username);
        if(cartList==null){
            cartList=new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        System.out.println("合并购物车");
        for(Cart cart:cartList1){
            for(TbOrderItem orderItem:cart.getOrderItemList()){
                cartList2=addGoodsToCartList(cartList2,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList2;
    }

    //根据商家ID查询购物车对象,如果存在，返回该购物车对象。
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId){
        for(Cart cart:cartList){
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;//如果不存在返回null
    }

    /*创建订单明细（cart包含两部分：
    第一部分：商家信息（商家ID，和商家名）
    第二部分：订单明细
    这里是创建订单明细orderitem（item和num）
    * */
    private TbOrderItem createOrderItem(TbItem item,Integer num){
        if(num<=0){
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem =new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()).multiply(new BigDecimal(num)));
        return orderItem;
    }

    // 根据商品明细ID查询
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList ,Long itemId ){
        for(TbOrderItem orderItem:orderItemList){
            if(orderItem.getItemId().longValue()==itemId.longValue()){
                //如果存在商品明细ID，返回这个明细列表
                return orderItem;
            }
        }
        //如果没有，返回空，需要添加时再新创建
        return null;
    }

}
