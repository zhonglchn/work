package com.leyou.sms.utils;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.leyou.common.constant.SmsConstants;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 发送各种短信【验证码，通知】的工具类
 */
@Slf4j
@Component
public class SmsHelper {

    @Autowired
    private IAcsClient client;

    @Autowired
    private SmsProperties prop;

    /**
     * 发送验证码短信的工具类
     * @param phone 发送的手机号
     * @param code 发送的验证码
     */
    public void sendCheckCodeMsg(String phone, String code){

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain(prop.getDomain());
        request.setSysVersion(prop.getVersion());
        request.setSysAction(prop.getAction());
        request.putQueryParameter(SmsConstants.SMS_PARAM_REGION_ID, prop.getRegionID());
        request.putQueryParameter(SmsConstants.SMS_PARAM_KEY_PHONE, phone);
        request.putQueryParameter(SmsConstants.SMS_PARAM_KEY_SIGN_NAME, prop.getSignName());
        request.putQueryParameter(SmsConstants.SMS_PARAM_KEY_TEMPLATE_CODE, prop.getVerifyCodeTemplate());
        request.putQueryParameter(SmsConstants.SMS_PARAM_KEY_TEMPLATE_PARAM, "{\"checkcode\":\""+code+"\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            //将返回结果转成Json
            Map<String, String> respData = JsonUtils.toMap(response.getData(), String.class, String.class);
            //判断短信是否发送成功
            if(StringUtils.equals(respData.get(SmsConstants.SMS_RESPONSE_KEY_CODE), SmsConstants.OK)){
                log.info("【短信验证码发送成】手机号："+phone+"已经收到了验证码，验证码为："+code);
            }else {
                log.error("【短信验证码发失败】手机号："+phone+"没有收到验证码，异常信息为："+respData.get(SmsConstants.SMS_RESPONSE_KEY_MESSAGE));
                throw new LyException(501, "短信验证码发失败");
            }
        } catch (ServerException e) {
            log.error("【短信验证码发失败】阿里云服务器出了问题！");
            e.printStackTrace();
        } catch (ClientException e) {
            log.error("【短信验证码发失败】自己的服务器出了问题！");
            e.printStackTrace();
        }
    }

}