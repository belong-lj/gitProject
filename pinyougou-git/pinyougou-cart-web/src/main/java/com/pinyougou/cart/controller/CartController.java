package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
/*
*
* 实现思路：
    （1）从cookie中取出购物车
    （2）向购物车添加商品
    （3）将购物车存入cookie

*
*/
@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;


    //购物车列表
    //从cookie中获取购物车列表
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        //当前登录人账号
        //当用户未登陆时，username的值为anonymousUser
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人："+username);

        String cartListString = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if(cartListString==null || cartListString.equals("")){
            cartListString="[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);

        if(username.equals("anonymousUser")){//如果未登录
            //从cookie中提取购物车
            System.out.println("从cookie中提取购物车");

            return cartList_cookie;

        }else {//如果已经登录
            List<Cart> cartList_redis =cartService.findCartListFromRedis(username);//从redis中提取
            if(cartList_cookie.size()>0){//判断当本地购物车中存在数据
                //得到合并后的购物车
                List<Cart> cartList = cartService.mergeCartList(cartList_cookie, cartList_redis);
                //将合并后的购物车存入redis
                cartService.saveCartListToRedis(username,cartList);
                //本地购物车清除
                util.CookieUtil.deleteCookie(request,response,"cartList");
                System.out.println("执行了合并购物车的逻辑");
                return cartList;

            }

            return cartList_redis;

        }
    }

    //添加商品到购物车列表
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
    public Result addGoodsToCartList(Long itemId, Integer num){

        //当前登录人账号
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人："+username);
        try{
            //从cookie中获取购物车
            List<Cart> cartList = findCartList();
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if(username.equals("anonymousUser")) {
                //将新的商品列表转换为JSON写回到cookie中
                util.CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
                System.out.println("向cookie存入数据");
            }else{
                //如果已经登录，保存到redis中
                cartService.saveCartListToRedis(username,cartList);
            }
            return  new Result(true,"添加成功");

        }  catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());


        }catch (Exception e){
            e.printStackTrace();
            return  new Result(false,"添加失败");
        }
    }

}
