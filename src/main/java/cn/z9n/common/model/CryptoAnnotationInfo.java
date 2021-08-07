package cn.z9n.common.model;

import cn.z9n.common.enums.CryptoTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : z9n
 * @date :  2021/8/6 10:32 下午
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CryptoAnnotationInfo {
    private String uri;
    private CryptoTypeEnum cryptoType;

}
