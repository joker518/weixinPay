package com.juno.weixin.modules.common.wx;

/**
 * 微信公众号常量类
 * @author dongxj
 * @date   2018/06/28
 */
public class WxConstants {

    /**
     * 默认编码
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 统一下单-扫描支付
     */
    public static String PAY_UNIFIEDORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    /**
     * 查询订单
     */
    public static String PAY_ORDERQUERY = "https://api.mch.weixin.qq.com/pay/orderquery";

    /**
     * 退款
     */
    public static String PAY_REFUND = "https://api.mch.weixin.qq.com/secapi/pay/refund";

    /**
     * 请求成功返回码
     */
    public final static String ERRCODE_OK_CODE = "0";
    /**
     * 错误的返回码的Key
     */
    public final static String ERRCODE = "errcode";

    /**
     * 返回状态码
     */
    public final static String RETURN_CODE= "return_code";

    /**
     * access_token 字符串
     */
    public final static String ACCESS_TOKEN = "access_token";

    /**
     * 签名类型 MD5
     */
    public final static String SING_MD5 = "MD5";

    /**
     * 签名类型 HMAC-SHA256
     */
    public final static String SING_HMACSHA256 = "HMAC-SHA256";

}
