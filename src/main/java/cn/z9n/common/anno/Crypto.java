package cn.z9n.common.anno;

import cn.z9n.common.enums.CryptoTypeEnum;

import java.lang.annotation.*;

/**
 * @author : z9n
 * @date :  2021/8/6 10:19 下午
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Crypto {
    CryptoTypeEnum value() default CryptoTypeEnum.BOTH_REQUEST_RESPONSE;
}
