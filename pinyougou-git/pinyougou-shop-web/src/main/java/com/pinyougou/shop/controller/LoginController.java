package com.pinyougou.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/*读取到当前用户名*/
@RestController
@RequestMapping("/login")
public class LoginController {
    @RequestMapping("/name")
    public Map name(){
        //获取到当前用户名字符串
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map =new HashMap();
        map.put("loginName",name);
        return map;
    }
}
