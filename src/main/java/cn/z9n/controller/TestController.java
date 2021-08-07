package cn.z9n.controller;

import cn.z9n.common.anno.Crypto;
import cn.z9n.common.enums.CryptoTypeEnum;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author : z9n
 * @date :  2021/8/6 10:19 下午
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Crypto(CryptoTypeEnum.BOTH_REQUEST_RESPONSE)
    @RequestMapping("/body")
    private JSONObject body(@RequestBody JSONObject requestBody) {
        JSONObject resp = new JSONObject();
        resp.put("originalRequest", requestBody);
        resp.put("message", "请求处理成功");
        return resp;
    }

    @RequestMapping("/param")
    @Crypto(CryptoTypeEnum.DECRYPT_REQUEST)
    private JSONObject param(@RequestParam JSONObject requestParam) {
        JSONObject resp = new JSONObject();
        resp.put("originalParam", requestParam);
        resp.put("message", "请求处理成功");
        return resp;
    }

    @RequestMapping("/formData")
    @Crypto(CryptoTypeEnum.BOTH_REQUEST_RESPONSE)
    private JSONObject formData(FormData requestFormData) throws IOException {
        JSONObject resp = new JSONObject();
        JSONObject resData = new JSONObject();
        resData.put("key1", requestFormData.key1);
        resData.put("key2", StreamUtils.copyToString(requestFormData.key2.getInputStream(), StandardCharsets.UTF_8));
        resp.put("originalFormData", resData);
        resp.put("message", "请求处理成功");
        return resp;
    }

    @Data
    public static class FormData {
        private String key1;
        private MultipartFile key2;
    }
}
