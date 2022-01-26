package com.study.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.study.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000119602894";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCrXNY55+83JP+LfPkItMCofrfghRbJDx/HlTiRsPqQO0L8LUjpjRBjoSv7W111ZnZI2Rgv2SQUbFfYHLEy5ndZY08y/jX1cxrzPWRgDjHcjDzSyB4OFZS5MlZFGyO+9o3zUnSy57jyXGOYYVA6dDKreU5+XWVDJDUw8m4tcbPFjmS2eGr9GupW4NoWKNnNjRopyBcOUYrKOmk2P1RhWUtSr5jryaJTjfIyMqC5Ft/+Jk4+J3xU3xayGGpXhfHujNcYkUDDq+KR+22FE20u0E8VoK5BKXlT4hwJxNWFbBvdRVNZTrUOw9mimS3EaRBTphRotMadEHSRRIcemavj95qHAgMBAAECggEBAKQ4xfycbz2zsUqmK6rSPke7mNAL+3gXjGcidCH7L0UF5f3yEAvyaiBwlsu+FWSrGvRZSLqiJ9eCtPShGpgjEYGSkJXX1TIya0NsNENxlws7QhbmPRdWSz7oJJD5n1okwojLHpdNRorNS4z+xXofXWfDSxwCuUaM7oTH8BqQ+q15ZDVPlMGZGs/wfiTgySMbFo9EW18Ag8ghHQTFzSfNf3k6DMcKqwVG/uNwtUxJWP7QSX9U6LD0JS3rzRvbHYoj9rHoOcTJx8EquN7gPk3QCpLSUiUlpyiS3p9RxrgtQX90RSQDiIMJEJDCsyG/lKAAjbX5EEC+lENR9Ol4u3IXOsECgYEA4Xa5k6ba74K7gtrp3ZFwq7Kny+enb3TLMd8VbsLD9Ys928MxqxUnlPGQsw/BlFiZ1u2gNQmguvf+TRRTUb95NU5n46a9pE1II0eUyf7TJjMaO88oAWu8OmdHloAyJDxbMf1Pq430PHIOWuiDj6YNT4yR7PozBqlupHclE9WvxTcCgYEAwpJRnoUshONgdZelaoyewqJaWaG6lkwUkYTzWgQ+z9AYeTkHzdTUjBWdmLqc7DONhkqf6fxWDDgpcbYb0ZKAZuIb0PdiS0y4VgNygJkRYiqDOy87mYRXg7rlHJxTUlV+Dj/tBNRnextAhJCC4I53n68qgx5uT/BerSus9CmxfTECgYAucKD668RuzxPGeUbwj3OQuiTb2CRhzcHIzPWCMfXhDsQ4DvWdEqdrg3JVn6o03ycY4Ss9oXUM7eXghI5A6VqTA0pc1sx7GjwAoE7iaG2fkakWpq3L1SAZO4rAUjmBwzjZjB6r7HphNPEGApyOqxpUx1SLZmG6Po3+rFfjxQEGcwKBgQCai4vm5JKq7svUO2J5Uj5cDIbFQO+qp72CpF6CtnKJsBlwV/s543370sN952PzPTOBYk7IlNRoGkfY+TP5b2srzRcZERXzB3o3X7+YI3yqQ2g/H1Iwquy0RkPrSMPp1twsR1xlBK05HDlP0lUSNjcQEBstUIOAXUVmORXETkWbUQKBgHwrKQSESDLfBqZWQ/GpCfQgZ5nwKqAafLb8zhnMWvDchIEen//d6VzROWKah4P5uhSrzhxyTP0nKQlP6J8w6bPxCHkgEoZ5xoABDPDmD9sso8Et14qFEZlrBhvmiwKxVqnUjkXnR99oAJlZ4lqI2OUa2w61eVpAcT/9HuV57GNn";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkgfFo66/JbiX8GHFPwOJIYuvC4nzZrIjb82hGXikDN2YblpA53DNoOKFUXnvuyHeCicmWaMeI2p4lyfs4XPvDHiH8+U1kSRMBzL0fzQwBzZUhUO8l02KGQGNdDgzsS5Ho9RzWy78U4qY8BuMwIiSFPSRAGZV9Ftvkzw6ZxRj++A+sgaQBcxRupRupYKrEWLQS72YUtsM8TElVQQQYzlh6xQzTDh7/ihmz6XtkaSSRogMApjnP0daSxLKHi/PHZIkb6CgzHTE9+mPZmhRuyImpt0foQTd/8D5DnUcor8NIl/+u0EE378gkehvisop8FldsZaKnVxxa8EDdxivSNV2eQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url="http://4813il9347.zicp.vip/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url="http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    private String timeout="30m";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();


        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+ timeout+"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        //System.out.println("支付宝的响应："+result);

        return result;

    }
}
