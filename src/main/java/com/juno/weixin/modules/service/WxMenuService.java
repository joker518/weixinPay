package com.juno.weixin.modules.service;

/**
 * 微信菜单业务类
 * @author dongxj
 * @date   2018/03/01
 */
public interface WxMenuService {


    /**
     * 生成支付二维码URL
     * @param totalFee 标价金额(分单位)
     * @param outTradeNo 商户订单号
     * @param signType 签名类型
     * @throws Exception
     */
    String wxPayUrl(Double totalFee, String outTradeNo, String signType) throws Exception;

    /**
     * 查询订单交易状态
     * @param outTradeNo
     * @param signType
     * @return
     * @throws Exception
     */
    String wxPayStatus(String outTradeNo, String signType) throws Exception;


    /**
     * 退款
     * @param totalFee 标价金额(分单位)
     * @param outTradeNo 商户订单号
     * @param signType 签名类型
     * @throws Exception
     */
    String wxRefundOrder(Double totalFee, String outTradeNo, String signType) throws Exception;


}
