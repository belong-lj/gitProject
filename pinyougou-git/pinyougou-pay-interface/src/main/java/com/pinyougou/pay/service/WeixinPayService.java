package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {

    /*
    * 根据订单号和金额生成微信支付二维码集合
    * 前端会根据这个集合生成响应的二维码图片
    *out_trade_no 订单号
    * total_fee 金额(分)
    * */

    public Map createNative(String out_trade_no,String total_fee);

    /**
     * 查询支付状态
     * @param out_trade_no
     */
    public Map queryPayStatus(String out_trade_no);

    //关闭支付
    public Map closePay(String out_trade_no);

}
