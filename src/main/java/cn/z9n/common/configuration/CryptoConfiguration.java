package cn.z9n.common.configuration;

import cn.z9n.common.config.CryptoConfig;
import cn.z9n.common.filter.CryptoPreAnalyseFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @author : z9n
 * @date :  2021/8/6 10:19 下午
 */
@Configuration
@ConditionalOnProperty(prefix = "http.crypto.config", name = "enable", havingValue = "true")
public class CryptoConfiguration {
    @Bean("cryptoConfig")
    public CryptoConfig cryptoConfig() {
        return new CryptoConfig();
    }

//    @Bean("cryptoPostProcessor")
//    public CryptoPostProcessor cryptoPostProcessor(@Autowired Environment environment) {
//        return new CryptoPostProcessor(environment);
//    }


    @Bean("cryptoPreAnalyseFilter")
    public FilterRegistrationBean<CryptoPreAnalyseFilter> cryptoPreAnalyseFilter(
            @Autowired DispatcherServlet dispatcherServlet,
            @Qualifier("cryptoConfig") CryptoConfig cryptoConfig
    ) {
        FilterRegistrationBean<CryptoPreAnalyseFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CryptoPreAnalyseFilter(dispatcherServlet, cryptoConfig));
        registration.addUrlPatterns("/*");
        registration.setName("CryptoFilter");
        registration.setOrder(0);
        return registration;
    }
}
