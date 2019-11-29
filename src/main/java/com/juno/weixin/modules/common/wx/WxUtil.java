package com.juno.weixin.modules.common.wx;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.juno.weixin.modules.common.util.SHA1;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 微信公众号接口工具类
 * @author dongxj
 * @date   2018/03/01
 */
public class WxUtil {

	/**
	 * 加密/校验流程如下：
	 * 1. 将token、timestamp、nonce三个参数进行字典序排序<br>
	 * 2. 将三个参数字符串拼接成一个字符串进行sha1加密<br>
	 * 3. 开发者获得加密后的字符串可与signature对比，标识该请求来源于微信<br>
	 *
	 * @param token Token验证密钥
	 * @param signature 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数，nonce参数
	 * @param timestamp 时间戳
	 * @param nonce 随机数
	 * @return 验证成功返回：true,失败返回：false
	 */
	public static boolean checkSignature(String token, String signature, String timestamp, String nonce) {
		List<String> params = new ArrayList<String>();
		params.add(token);
		params.add(timestamp);
		params.add(nonce);
		//1. 将token、timestamp、nonce三个参数进行字典序排序
		Collections.sort(params, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		//2. 将三个参数字符串拼接成一个字符串进行sha1加密
		String temp = SHA1.encode(params.get(0) + params.get(1) + params.get(2));
		//3. 开发者获得加密后的字符串可与signature对比，标识该请求来源于微信
		return temp.equals(signature);
	}

	/**
	 * 输入流转化为字符串
	 * @param inputStream 流
	 * @return String 字符串
	 * @throws Exception
	 */
	public static String getStreamString(InputStream inputStream) throws Exception{
		StringBuffer buffer=new StringBuffer();
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		try{
			inputStreamReader=new InputStreamReader(inputStream, WxConstants.DEFAULT_CHARSET);
			bufferedReader=new BufferedReader(inputStreamReader);
			String line;
			while((line=bufferedReader.readLine())!=null){
				buffer.append(line);
			}
		}catch(Exception e){
			throw new Exception();
		}finally {
			if(bufferedReader != null){
				bufferedReader.close();
			}
			if(inputStreamReader != null){
				inputStreamReader.close();
			}
			if(inputStream != null){
				inputStream.close();
			}
		}
		return buffer.toString();
	}

	/**
	 * 获取随机字符串 Nonce Str
	 * @return String 随机字符串
	 */
	public static String getNonceStr() {
		return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
	}

	/**
	 * 生成签名. 注意，若含有sign_type字段，必须和signType参数保持一致。
	 * @param data 待签名数据
	 * @param key API密钥
	 * @return 签名
	 */
	public static String getSignature(final Map<String, String> data, String key,String signType) throws Exception {
		Set<String> keySet = data.keySet();
		String[] keyArray = keySet.toArray(new String[keySet.size()]);
		Arrays.sort(keyArray);
		StringBuilder sb = new StringBuilder();
		for (String k : keyArray) {
			if (k.equals("sign")) {
				continue;
			}
			//参数值为空，则不参与签名
			if (data.get(k).trim().length() > 0)
				sb.append(k).append("=").append(data.get(k).trim()).append("&");
		}
		sb.append("key=").append(key);
		if (signType.equals(WxConstants.SING_MD5)) {
			return MD5(sb.toString()).toUpperCase();
		}
		else if (signType.equals(WxConstants.SING_HMACSHA256)) {
			return HMACSHA256(sb.toString(), key);
		}
		else {
			throw new Exception(String.format("Invalid sign_type: %s", signType));
		}
	}

	/**
	 * 生成 MD5
	 * @param data 待处理数据
	 * @return MD5结果
	 */
	public static String MD5(String data) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] array = md.digest(data.getBytes("UTF-8"));
		StringBuilder sb = new StringBuilder();
		for (byte item : array) {
			sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
		}
		return sb.toString().toUpperCase();
	}

	/**
	 * 生成 HMACSHA256
	 * @param data 待处理数据
	 * @param key 密钥
	 * @return 加密结果
	 * @throws Exception
	 */
	public static String HMACSHA256(String data, String key) throws Exception {
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
		sha256_HMAC.init(secret_key);
		byte[] array = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
		StringBuilder sb = new StringBuilder();
		for (byte item : array) {
			sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
		}
		return sb.toString().toUpperCase();
	}

	/**
	 * @param data Map类型数据
	 * @return XML格式的字符串
	 * @throws Exception
	 */
	public static String mapToXml(Map<String, String> data) throws Exception {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder= documentBuilderFactory.newDocumentBuilder();
		org.w3c.dom.Document document = documentBuilder.newDocument();
		org.w3c.dom.Element root = document.createElement("xml");
		document.appendChild(root);
		for (String key: data.keySet()) {
			String value = data.get(key);
			if (value == null) {
				value = "";
			}
			value = value.trim();
			org.w3c.dom.Element filed = document.createElement(key);
			filed.appendChild(document.createTextNode(value));
			root.appendChild(filed);
		}
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		DOMSource source = new DOMSource(document);
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		transformer.transform(source, result);
		String output = writer.getBuffer().toString(); //.replaceAll("\n|\r", "");
		try {
			writer.close();
		}
		catch (Exception ex) {
		}
		return output;
	}

	/**
	 * 处理 HTTPS API返回数据，转换成Map对象。return_code为SUCCESS时，验证签名。
	 * @param xmlStr API返回的XML格式数据
	 * @return Map类型数据
	 * @throws Exception
	 */
	public static Map<String, String> processResponseXml(String xmlStr,String signType) throws Exception {
		String RETURN_CODE = WxConstants.RETURN_CODE;
		String return_code;
		Map<String, String> respData = xmlToMap(xmlStr);
		if (respData.containsKey(RETURN_CODE)) {
			return_code = respData.get(RETURN_CODE);
		}
		else {
			throw new Exception(String.format("No `return_code` in XML: %s", xmlStr));
		}

		if (return_code.equals("FAIL")) {
			return respData;
		}
		else if (return_code.equals("SUCCESS")) {
			if (isResponseSignatureValid(respData,signType)) {
				return respData;
			}
			else {
				throw new Exception(String.format("Invalid sign value in XML: %s", xmlStr));
			}
		}
		else {
			throw new Exception(String.format("return_code value %s is invalid in XML: %s", return_code, xmlStr));
		}
	}


	/**
	 * XML格式字符串转换为Map
	 * @param strXML XML字符串
	 * @return XML数据转换后的Map
	 * @throws Exception
	 */
	public static Map<String, String> xmlToMap(String strXML) throws Exception {
		try {
			Map<String, String> data = new HashMap<String, String>();
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

			String FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
			documentBuilderFactory.setFeature(FEATURE, true);

			FEATURE = "http://xml.org/sax/features/external-general-entities";
			documentBuilderFactory.setFeature(FEATURE, false);

			FEATURE = "http://xml.org/sax/features/external-parameter-entities";
			documentBuilderFactory.setFeature(FEATURE, false);

			FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
			documentBuilderFactory.setFeature(FEATURE, false);

			documentBuilderFactory.setXIncludeAware(false);
			documentBuilderFactory.setExpandEntityReferences(false);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputStream stream = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
			org.w3c.dom.Document doc = documentBuilder.parse(stream);
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getDocumentElement().getChildNodes();
			for (int idx = 0; idx < nodeList.getLength(); ++idx) {
				Node node = nodeList.item(idx);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					org.w3c.dom.Element element = (org.w3c.dom.Element) node;
					data.put(element.getNodeName(), element.getTextContent());
				}
			}
			try {
				stream.close();
			} catch (Exception ex) {
				// do nothing
			}
			return data;
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	 * 判断xml数据的sign是否有效，必须包含sign字段，否则返回false。
	 * @param reqData 向wxpay post的请求数据
	 * @return 签名是否有效
	 * @throws Exception
	 */
	private static boolean isResponseSignatureValid(final Map<String, String> reqData,String signType) throws Exception {
		// 返回数据的签名方式和请求中给定的签名方式是一致的
		return isSignatureValid(reqData,WxConfig.key,signType);
	}

	/**
	 * 判断签名是否正确，必须包含sign字段，否则返回false。
	 * @param data Map类型数据
	 * @param key API密钥
	 * @param signType 签名方式
	 * @return 签名是否正确
	 * @throws Exception
	 */
	public static boolean isSignatureValid(Map<String, String> data, String key, String signType) throws Exception {
		if (!data.containsKey("sign")) {
			return false;
		}
		String sign = data.get("sign");
		return getSignature(data, key, signType).equals(sign);
	}

	/**
	 * 生成支付二维码
	 * @param response 响应
	 * @param contents url链接
	 * @throws Exception
	 */
	public static void writerPayImage(HttpServletResponse response, String contents) throws Exception{
		ServletOutputStream out = response.getOutputStream();
		try {
			Map<EncodeHintType,Object> hints = new HashMap<EncodeHintType,Object>();
			hints.put(EncodeHintType.CHARACTER_SET,"UTF-8");
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
			hints.put(EncodeHintType.MARGIN, 0);
			BitMatrix bitMatrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE,300,300,hints);
			MatrixToImageWriter.writeToStream(bitMatrix,"jpg",out);
		}catch (Exception e){
			throw new Exception("生成二维码失败！");
		}finally {
			if(out != null){
				out.flush();
				out.close();
			}
		}
	}

	/**
	 * 生成商户订单号
	 * @return String
	 */
	public static String mchOrderNo(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String date = sdf.format(new Date());

		Random random = new Random();
		String fourRandom = String.valueOf(random.nextInt(10000));
		int randLength = fourRandom.length();
		//不足4位继续补充
		if(randLength<4){
			for(int remain = 1; remain <= 4 - randLength; remain ++ ){
				fourRandom += random.nextInt(10)  ;
			}
		}
		return date+fourRandom;
	}

	/**
	 * 返回信息给微信
	 * @param response
	 * @param content 内容
	 * @throws Exception
	 */
	public static void responsePrint(HttpServletResponse response, String content) throws Exception{
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/xml");
		response.getWriter().print(content);
		response.getWriter().flush();
		response.getWriter().close();
	}


}
