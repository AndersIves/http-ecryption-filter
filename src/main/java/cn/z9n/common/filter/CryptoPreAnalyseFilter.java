package cn.z9n.common.filter;

import cn.z9n.common.anno.Crypto;
import cn.z9n.common.config.CryptoConfig;
import cn.z9n.common.enums.CryptoTypeEnum;
import cn.z9n.common.filter.wrapper.CustomRequestWrapper;
import cn.z9n.common.filter.wrapper.CustomResponseWrapper;
import cn.z9n.common.utils.RsaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;

/**
 * @author : z9n
 * @date :  2021/8/6 10:19 下午
 */
@Slf4j
public class CryptoPreAnalyseFilter implements Filter {

    private final DispatcherServlet dispatcherServlet;


    private final CryptoConfig cryptoConfig;

    private static final String CRYPTO_PREFIX = "/crypto/";

    public CryptoPreAnalyseFilter(DispatcherServlet dispatcherServlet, CryptoConfig cryptoConfig) {
        this.dispatcherServlet = dispatcherServlet;
        this.cryptoConfig = cryptoConfig;
    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            // 判断是否为加密路径 匹配/crypto/xxxxxxx
            String requestUri = httpServletRequest.getRequestURI();
            if (!requestUri.startsWith(CRYPTO_PREFIX)) {
                // 查方法，判断是否为需要响应加密的方法
                dealWithNoneCryptoStart(httpServletRequest,servletResponse,filterChain);
            }else {
                dealWithCryptoStart(httpServletRequest, servletResponse, filterChain);
            }
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
    private void dealWithNoneCryptoStart(HttpServletRequest httpServletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        Method method;
        try {
            method = getHandlerMethod(httpServletRequest);
        } catch (Exception e) {
            log.error("[http请求加密传输] 获取映射的方法异常, 移交spring管理", e);
            filterChain.doFilter(httpServletRequest, servletResponse);
            return;
        }
        CryptoTypeEnum cryptoTypeEnum = Optional.ofNullable(method.getAnnotation(Crypto.class)).map(Crypto::value).orElse(CryptoTypeEnum.NONE);
        //noinspection AlibabaSwitchStatement
        switch (cryptoTypeEnum) {
            case NONE:
                filterChain.doFilter(httpServletRequest, servletResponse);
                return;
            case DECRYPT_REQUEST: {
                if (!checkUriCrypto(servletResponse)) {
                    return;
                }
                CustomRequestWrapper requestWrapper = new CustomRequestWrapper(httpServletRequest);
                try {
                    String requestBody = requestWrapper.getRequestBody();
                    String decrypt = RsaUtil.decrypt(requestBody, cryptoConfig.getPrivateKey());
                    requestWrapper.setRequestBody(decrypt);
                } catch (Exception e) {
                    log.error("[http请求加密传输] 请求体解密异常", e);
                    // TODO 回写解密失败响应体
                    return;
                }
                filterChain.doFilter(requestWrapper, servletResponse);
                return;
            }
            case ENCRYPT_RESPONSE: {
                CustomResponseWrapper responseWrapper = new CustomResponseWrapper((HttpServletResponse) servletResponse);
                filterChain.doFilter(httpServletRequest, responseWrapper);
                try {
                    String responseBody = responseWrapper.getResponseBody();
                    String encrypt = RsaUtil.encrypt(responseBody, cryptoConfig.getPublicKey());
                    responseWrapper.setResponseBody(encrypt);
                } catch (Exception e) {
                    log.error("[http请求加密传输] 响应体加密异常", e);
                    // TODO 回写加密失败响应体
                    return;
                }
                return;
            }
            case BOTH_REQUEST_RESPONSE: {
                if (!checkUriCrypto(servletResponse)) {
                    return;
                }
                CustomRequestWrapper requestWrapper = new CustomRequestWrapper(httpServletRequest);
                try {
                    String requestBody = requestWrapper.getRequestBody();
                    String decrypt = RsaUtil.decrypt(requestBody, cryptoConfig.getPrivateKey());
                    requestWrapper.setRequestBody(decrypt);
                } catch (Exception e) {
                    log.error("[http请求加密传输] 请求体解密异常", e);
                    // TODO 回写解密失败响应体
                    return;
                }
                CustomResponseWrapper responseWrapper = new CustomResponseWrapper((HttpServletResponse) servletResponse);
                filterChain.doFilter(requestWrapper, responseWrapper);
                try {
                    String responseBody = responseWrapper.getResponseBody();
                    String encrypt = RsaUtil.encrypt(responseBody, cryptoConfig.getPublicKey());
                    responseWrapper.setResponseBody(encrypt);
                } catch (Exception e) {
                    log.error("[http请求加密传输] 响应体加密异常", e);
                    // TODO 回写加密失败响应体
                }
            }
        }
    }
    private void dealWithCryptoStart(HttpServletRequest httpServletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = httpServletRequest.getRequestURI();
        // 解密uri
        String secUri = requestUri.substring(CRYPTO_PREFIX.length());
        String orgUri;
        try {
            orgUri = RsaUtil.decrypt(secUri, cryptoConfig.getPrivateKey());
        } catch (Exception e) {
            log.error("[http请求加密传输] url解密异常", e);
            // TODO 回写解密失败响应体
            return;
        }
        httpServletRequest.getParameterMap();
        CustomRequestWrapper requestWrapper = new CustomRequestWrapper(httpServletRequest);
        // *** 注意 此时 httpServletRequest.getInputStream() 已被读完 后续必须使用 requestWrapper
        requestWrapper.setUri(orgUri);
        Method method;
        try {
            method = getHandlerMethod(requestWrapper);
        } catch (Exception e) {
            log.error("[http请求加密传输] 获取映射的方法异常, 移交spring管理", e);
            requestWrapper.setUri(secUri);
            filterChain.doFilter(requestWrapper, servletResponse);
            return;
        }
        CryptoTypeEnum cryptoTypeEnum = Optional.ofNullable(method.getAnnotation(Crypto.class)).map(Crypto::value).orElse(CryptoTypeEnum.NONE);
        //noinspection AlibabaSwitchStatement
        switch (cryptoTypeEnum) {
            case NONE:
                if (!checkUriCrypto(servletResponse)) {
                    return;
                }
                filterChain.doFilter(requestWrapper, servletResponse);
                return;
            case DECRYPT_REQUEST: {
                try {
                    String requestBody = requestWrapper.getRequestBody();
                    String decrypt = RsaUtil.decrypt(requestBody, cryptoConfig.getPrivateKey());
                    requestWrapper.setRequestBody(decrypt);
                } catch (Exception e) {
                    log.error("[http请求加密传输] 请求体解密异常", e);
                    // TODO 回写解密失败响应体
                    return;
                }
                filterChain.doFilter(requestWrapper, servletResponse);
                return;
            }
            case ENCRYPT_RESPONSE: {
                if (!checkUriCrypto(servletResponse)) {
                    return;
                }
                CustomResponseWrapper responseWrapper = new CustomResponseWrapper((HttpServletResponse) servletResponse);
                filterChain.doFilter(requestWrapper, responseWrapper);
                try {
                    String responseBody = responseWrapper.getResponseBody();
                    String encrypt = RsaUtil.encrypt(responseBody, cryptoConfig.getPublicKey());
                    responseWrapper.setResponseBody(encrypt);
                } catch (Exception e) {
                    log.error("[http请求加密传输] 响应体加密异常", e);
                    // TODO 回写加密失败响应体
                    return;
                }
                return;
            }
            case BOTH_REQUEST_RESPONSE: {
                try {
                    String requestBody = requestWrapper.getRequestBody();
                    String decrypt = RsaUtil.decrypt(requestBody, cryptoConfig.getPrivateKey());
                    requestWrapper.setRequestBody(decrypt);
                } catch (Exception e) {
                    log.error("[http请求加密传输] 请求体解密异常", e);
                    // TODO 回写解密失败响应体
                    return;
                }
                CustomResponseWrapper responseWrapper = new CustomResponseWrapper((HttpServletResponse) servletResponse);
                filterChain.doFilter(requestWrapper, responseWrapper);
                try {
                    String responseBody = responseWrapper.getResponseBody();
                    String encrypt = RsaUtil.encrypt(responseBody, cryptoConfig.getPublicKey());
                    responseWrapper.setResponseBody(encrypt);
                } catch (Exception e) {
                    log.error("[http请求加密传输] 响应体加密异常", e);
                    // TODO 回写加密失败响应体
                }
            }
        }
    }

    private boolean checkUriCrypto(ServletResponse servletResponse) {
        if (!cryptoConfig.isEnableDebugMod()) {
            // TODO 回写400响应体 非调试模式url也要遵从加解密规则
            return false;
        }
        return true;
    }

    private Method getHandlerMethod(HttpServletRequest request) throws Exception {
        for (HandlerMapping handlerMapping : Optional.ofNullable(dispatcherServlet.getHandlerMappings()).orElse(Collections.emptyList())) {
            HandlerExecutionChain handler = handlerMapping.getHandler(request);
            if (handler != null) {
                return ((HandlerMethod)handler.getHandler()).getMethod();
            }
        }
        throw new RuntimeException("未获取到映射的方法");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {

    }

}
