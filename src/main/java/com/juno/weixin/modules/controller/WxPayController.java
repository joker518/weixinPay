package com.juno.weixin.modules.controller;

import com.alibaba.fastjson.JSONObject;
import com.juno.weixin.modules.common.wx.WxConfig;
import com.juno.weixin.modules.common.wx.WxConstants;
import com.juno.weixin.modules.common.wx.WxUtil;
import com.juno.weixin.modules.service.WxMenuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 微信菜单控制类
 * @author dongxj
 * @date   2018/03/01
 */
@Controller
public class WxPayController {

    @Autowired
    private WxMenuService wxMenuService;

    /**
     * 二维码首页
     */
    @RequestMapping(value = {"/prepay"}, method = RequestMethod.GET)
    public String wxPayList(Model model){
        //商户订单号
        model.addAttribute("outTradeNo",WxUtil.mchOrderNo());
        return "/wxPayList";
    }

    /**
     * 获取流水号
     */
    @RequestMapping(value = {"/wxPay/outTradeNo"})
    @ResponseBody
    public String getOutTradeNo(Model model){
        //商户订单号
        return WxUtil.mchOrderNo();
    }

    final private String signType = WxConstants.SING_MD5;
    /**
     * 统一下单-生成二维码
     */
    @RequestMapping(value = {"/wxPay/payUrl"})
    public void payUrl(HttpServletRequest request, HttpServletResponse response,
                       @RequestParam(value = "totalFee")Double totalFee,
                       @RequestParam(value = "outTradeNo")String outTradeNo) throws Exception{
        WxUtil.writerPayImage(response,wxMenuService.wxPayUrl(totalFee,outTradeNo,signType));
    }

    /**
     * 统一下单-通知链接
     */
    @RequestMapping(value = {"/wxPay/unifiedorderNotify"})
    public void unifiedorderNotify(HttpServletRequest request, HttpServletResponse response) throws Exception{

        //商户订单号
        String outTradeNo = null;
        String xmlContent = "<xml>" +
                "<return_code><![CDATA[FAIL]]></return_code>" +
                "<return_msg><![CDATA[签名失败]]></return_msg>" +
                "</xml>";

        try{
            String requstXml = WxUtil.getStreamString(request.getInputStream());
            System.out.println("requstXml : " + requstXml);
            Map<String,String> map = WxUtil.xmlToMap(requstXml);
            String returnCode= map.get(WxConstants.RETURN_CODE);
            //校验一下 ，判断是否已经支付成功
            if(StringUtils.isNotBlank(returnCode) && StringUtils.equals(returnCode,"SUCCESS")  &&  WxUtil.isSignatureValid(map, WxConfig.key,signType)){
                //商户订单号
                outTradeNo = map.get("out_trade_no");
                System.out.println("outTradeNo : "+ outTradeNo);
                //微信支付订单号
                String transactionId = map.get("transaction_id");
                System.out.println("transactionId : "+ transactionId);
                //支付完成时间
                SimpleDateFormat payFormat= new SimpleDateFormat("yyyyMMddHHmmss");
                Date payDate = payFormat.parse(map.get("time_end"));

                SimpleDateFormat systemFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.out.println("支付时间：" + systemFormat.format(payDate));
                //临时缓存
                WxConfig.setPayMap(outTradeNo,"SUCCESS");
                xmlContent = "<xml>" +
                        "<return_code><![CDATA[SUCCESS]]></return_code>" +
                        "<return_msg><![CDATA[OK]]></return_msg>" +
                        "</xml>";
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        WxUtil.responsePrint(response,xmlContent);
    }

    /**
     * 定时器查询是否已支付
     */
    @RequestMapping(value = {"/wxPay/payStatus"})
    @ResponseBody
    public String payStatus(@RequestParam(value = "outTradeNo")String outTradeNo) throws Exception {
        JSONObject responseObject = new JSONObject();
        //暂时不缓存处理，直接从request获取商户交易订单号
        //String outTradeNoValue = WxConfig.getPayMap(outTradeNo);

        //使用商户交易订单号查询微信支付状态
        String payStatus = wxMenuService.wxPayStatus(outTradeNo, signType);
        String status="";
        //判断是否已经支付成功
        if(StringUtils.isNotBlank(payStatus) && StringUtils.equals(payStatus,"SUCCESS")){
            status = "0";
        }
        responseObject.put("status",status);
        return responseObject.toJSONString();
    }

    /**
     * 退款
     */
    @RequestMapping(value = {"/wxPay/refundOrder"})
    @ResponseBody
    public String refundOrder(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String outTradeNo = request.getParameter("outTradeNo");
        Double totalFee = Double.parseDouble(request.getParameter("totalFee"));
        System.out.println("退款订单号"+outTradeNo+"    退款金额："+totalFee);
        JSONObject responseObject = new JSONObject();
        //使用商户交易订单号进行退款
        String refundStatus = wxMenuService.wxRefundOrder(totalFee,outTradeNo,signType);
        String status="";
        //判断是否已经退款成功
        if(StringUtils.isNotBlank(refundStatus) && StringUtils.equals(refundStatus,"SUCCESS")){
            status = "0";
        }
        responseObject.put("status",status);
        return responseObject.toJSONString();
    }




}
