package cn.z9n.test;

import cn.z9n.common.config.CryptoConfig;
import cn.z9n.common.utils.RsaUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @author : z9n
 * @date :  2021/8/6 10:19 下午
 */
@Component
@ConditionalOnProperty(prefix = "http.crypto.config", name = "enable", havingValue = "true")
public class Runner implements ApplicationRunner {
    @Autowired
    private CryptoConfig cryptoConfig;
    @Override
    public void run(ApplicationArguments args) throws Exception {

        System.out.println(RsaUtil.encrypt("/test/body", cryptoConfig.getPublicKey()));
    }
}
