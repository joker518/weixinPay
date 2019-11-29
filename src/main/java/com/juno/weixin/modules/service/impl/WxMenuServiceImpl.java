package com.juno.weixin.modules.service.impl;

import com.juno.weixin.modules.common.http.HttpsClient;
import com.juno.weixin.modules.common.wx.WxConfig;
import com.juno.weixin.modules.common.wx.WxConstants;
import com.juno.weixin.modules.common.wx.WxUtil;
import com.juno.weixin.modules.service.WxMenuService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信菜单实现类类
 * @author dongxj
 * @date   2018/02/16
 */
@Service("wxMenuService")
public class WxMenuServiceImpl implements WxMenuService {

    @Override
    public String wxPayUrl(Double totalFee,String outTradeNo,String signType) throws Exception {
        HashMap<String, String> data = new HashMap<String, String>();
        //公众账号ID
        data.put("appid", WxConfig.appID);
        //商户号
        data.put("mch_id", WxConfig.mchID);
        //随机字符串
        data.put("nonce_str", WxUtil.getNonceStr());
        //商品描述
        data.put("body","测试订单："+outTradeNo);
        //商户订单号
        data.put("out_trade_no",outTradeNo);
        //标价币种
        data.put("fee_type","CNY");
        //标价金额
        data.put("total_fee",String.valueOf(Math.round(totalFee * 100)));
        //用户的IP
        data.put("spbill_create_ip","12.12.12.12");
        //通知地址
        data.put("notify_url",WxConfig.unifiedorderNotifyUrl);
        //交易类型
        data.put("trade_type","NATIVE");
        //签名类型
        data.put("sign_type",signType);
        //签名
        data.put("sign",WxUtil.getSignature(data, WxConfig.key,signType));

        String requestXML = WxUtil.mapToXml(data);

        System.out.println("请求数据："+requestXML);
        String reponseString = HttpsClient.httpsRequestReturnString(WxConstants.PAY_UNIFIEDORDER,HttpsClient.METHOD_POST,requestXML);

        Map<String,String> resultMap = WxUtil.processResponseXml(reponseString,signType);
        System.out.println("响应数据："+resultMap);

        if(resultMap.get(WxConstants.RETURN_CODE).equals("SUCCESS")){
            return resultMap.get("code_url");
        }
        return null;
    }

    /**
     * 查询订单交易状态
     *
     * @param outTradeNo
     * @param signType
     * @return
     * @throws Exception
     */
    @Override
    public String wxPayStatus(String outTradeNo, String signType) throws Exception {
        HashMap<String, String> data = new HashMap<String, String>();
        //公众账号ID
        data.put("appid", WxConfig.appID);
        //商户号
        data.put("mch_id", WxConfig.mchID);
        //随机字符串
        data.put("nonce_str", WxUtil.getNonceStr());
        //商户订单号
        data.put("out_trade_no",outTradeNo);
        //签名类型
        data.put("sign_type",signType);
        //签名
        data.put("sign",WxUtil.getSignature(data, WxConfig.key,signType));

        String requestXML = WxUtil.mapToXml(data);
        System.out.println("请求数据："+requestXML);
        String reponseString = HttpsClient.httpsRequestReturnString(WxConstants.PAY_ORDERQUERY,HttpsClient.METHOD_POST,requestXML);

        Map<String,String> resultMap = WxUtil.processResponseXml(reponseString,signType);
        System.out.println("响应数据："+resultMap);

        if(resultMap.get(WxConstants.RETURN_CODE).equals("SUCCESS")){
            return "SUCCESS";
        }
        return null;
    }

    @Override
    public String wxRefundOrder(Double totalFee, String outTradeNo, String signType) throws Exception {
        HashMap<String, String> data = new HashMap<String, String>();
        //公众账号ID
        data.put("appid", WxConfig.appID);
        //商户号
        data.put("mch_id", WxConfig.mchID);
        //随机字符串
        data.put("nonce_str", WxUtil.getNonceStr());
        //商户订单号
        data.put("out_trade_no",outTradeNo);
        //标价金额
        data.put("total_fee",String.valueOf(Math.round(totalFee * 100)));
        //退款金额
        data.put("out_refund_no",String.valueOf(Math.round(totalFee * 100)));
        //签名类型
        data.put("sign_type",signType);
        //签名
        data.put("sign",WxUtil.getSignature(data, WxConfig.key,signType));

        String requestXML = WxUtil.mapToXml(data);

        System.out.println("请求数据："+requestXML);
        String reponseString = HttpsClient.httpsRequestReturnString(WxConstants.PAY_REFUND,HttpsClient.METHOD_POST,requestXML);

        Map<String,String> resultMap = WxUtil.processResponseXml(reponseString,signType);
        System.out.println("响应数据："+resultMap);

        if(resultMap.get(WxConstants.RETURN_CODE).equals("SUCCESS")){
            return resultMap.get("code_url");
        }
        return null;
    }


}
