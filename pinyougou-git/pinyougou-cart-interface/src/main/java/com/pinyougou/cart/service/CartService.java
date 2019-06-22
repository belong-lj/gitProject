package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

public interface CartService {

/*
 * 添加商品到购物车123
*/
    public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num );

    //将购物车保存到redis:username作为key
    public void saveCartListToRedis(String username,List<Cart> cartList);

    //从redis中查询购物车
    public List<Cart> findCartListFromRedis(String username);
    //购物车合并
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);

}
