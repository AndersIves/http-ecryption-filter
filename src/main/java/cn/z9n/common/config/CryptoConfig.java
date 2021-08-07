package cn.z9n.common.config;

import cn.z9n.common.utils.RsaUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author : z9n
 * @date :  2021/8/6 10:19 下午
 */
@Slf4j
@Data
@ConfigurationProperties(prefix = "http.crypto.config")
public class CryptoConfig {

    private static final String CLASSPATH_PREFIX = "classpath::";

    @PostConstruct
    private void init() {
        try {
            String privateKeyFileContent = new String(FileCopyUtils.copyToByteArray(getFile(privateKeyPath)), StandardCharsets.UTF_8);
            String publicKeyFileContent = new String(FileCopyUtils.copyToByteArray(getFile(publicKeyPath)), StandardCharsets.UTF_8);
            privateKey = RsaUtil.getPrivateKey(getKeyString(privateKeyFileContent));
            publicKey = RsaUtil.getPublicKey(getKeyString(publicKeyFileContent));
            log.info("[http请求加密传输] 公私钥加载完成");
        } catch (Exception e) {
            log.error("[http请求加密传输] 服务启动异常", e);
            throw new RuntimeException(e);
        }
    }

    private File getFile(String configPath) throws IOException {
        if (configPath.startsWith(CLASSPATH_PREFIX)) {
            return new ClassPathResource(configPath.substring(CLASSPATH_PREFIX.length())).getFile();
        }else {
            return new File(configPath);
        }
    }

    private String getKeyString(String orgKey) {
        String[] split = orgKey.split("\r\n?|\n");
        StringBuilder res = new StringBuilder();
        for (String s : split) {
            if (s.startsWith("-----BEGIN") || s.startsWith("-----END") || !StringUtils.hasText(s)) {
                continue;
            }
            res.append(s);
        }
        return res.toString();
    }

    private String publicKeyPath;
    private String privateKeyPath;
    private boolean enableDebugMod = true;



    private PrivateKey privateKey;
    private PublicKey publicKey;
}
