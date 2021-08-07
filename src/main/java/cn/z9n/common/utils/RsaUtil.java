package cn.z9n.common.utils;


import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RsaUtil {

    //限制大小
    private static final int MAX_ENCRYPT_BLOCK = 117;
    private static final int MAX_DECRYPT_BLOCK = 128;

    public static PrivateKey getPrivateKey(String privateKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] decodedKey = Base64.getDecoder().decode(privateKey.getBytes());
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        return keyFactory.generatePrivate(keySpec);
    }

    public static PublicKey getPublicKey(String publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] decodedKey = Base64.getDecoder().decode(publicKey.getBytes());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        return keyFactory.generatePublic(keySpec);
    }

    public static String encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        int inputLen = data.getBytes().length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offset > 0) {
            if (inputLen - offset > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data.getBytes(), offset, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data.getBytes(), offset, inputLen - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        // 加密后的字符串
        return Base64.getEncoder().encodeToString(encryptedData);
    }


    public static String decrypt(String data, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] dataBytes = Base64.getDecoder().decode(data);
        int inputLen = dataBytes.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offset > 0) {
            if (inputLen - offset > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(dataBytes, offset, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(dataBytes, offset, inputLen - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_DECRYPT_BLOCK;
        }
        out.close();
        // 解密后的内容
        return out.toString("UTF-8");
    }

    public static void main(String[] args) {
        String encrypted = "PcNfk0++FGtKxwEC/nxq1vtmb9UaAzzxjxruO78ImTvvjWJJOyqdKbpbz+r1O5l2Ub9RFDrehfi6pQKJDmTlB5ojG06cXvZ4B1fjxOVcGzd5q38FsqMbnLvN3/AsEKzwW/Ebji+Nz5MNrRmhHRu9OB+anJq4QleOn2DjGYGdSNp44Ha3N9/VIPGWzgurowGc+jUx+GlZCSjURjn/kfUo7+P4Ff/C3V2JVrV61qqZn3kZYGwtzQgjHK2YpAMMgBVVj6JSwQq6TWjbtDT0KKLYYF21t21nN0pfvC5BYdNKbJ/n/GtXlYt+SmQzAzStRpr2YDBZ5N+47mYeEckwt3WkCENrUNMrL+nN5J3g+vhFebETdZUvZEx97cVsZ9HDqNKonNCiZG2QTQ7cv8Bn/1O5u/XHUxG3rxZ+OjMHU8hTGwWT1RHlU+80MuKiUZBpIATM2lyQmv/feu139pDSi4gFL5pLjIKGMc83/2cgpLw4GE6N5fIi2ujZciXT9FiYkZpF5neppHTTc7YhCicJ1XwsRoXquvrBW3mjLBacwGUacq/oMFrZv+1ZrgdQB4KjfAexVjZqrqyowWujezWeixxXICHXPde7f5NP6NgQrKcpNrZpYnKmZ4/QoWF0cJCE7gogzho/rcY1wPvr1pJN1ZZ1fwuxQDh0wdIYr3vXL3Alb0ErkFQiNRtinx8uyrOaXh9X48I4Kci9Eq+AFNjhE+wEwLCbCVsLF2UWqgvWwr9s+LlZLpu9AfmztDb2mOLSGww8csAqpOKgCZkVNvdjd7hah9c4HXJX2yuCXVPwcisCteifvKdH05lkhXiUWksYNO7Maj7zKpjkGjaBRUyoKry75A==";
        String prikey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAOk9x+ME3Sz1nxuViLiWgUPTqogRgjLiIC306Af/iK+oZ9ebY5Oi7lzXo2EjuZZ3Gy3x6pvW2UI7y140sT7fTnc2HnYZ4tkSBOXGrHBoUSVzotqrIjlCPzRqJlGw8T6OSBGi1MqbVduDhdQ1FgcpDAAwMo/mbPWrq9WO7ciRmTnFAgMBAAECgYBGa6PNNCu/Jk2qm2RTLwXKP78OJBMmX8WNepMPzyQqvPLVUxImghvrpiOq8YlYF4/6OD84XA7ug3UsdUZKkEt0+RMDvPoVE20kq9fN3OM80j86/IarjwJdvhGfncPMZqt9LCRTmSmeNyzBe7n3fuOskSbGAT283XnKbz8+hNdKJQJBAP5MoKkaM4WDqGiVdUPdIOMVt+yTB7BW4oIts3f+GNcdp/26Z2gRsgTHnWRrdduFHslpmduJkfc8/NIpaAMvDRMCQQDqzRnXvrkDfaeZklIdO4xT2Lca6emMp936WdzPeb/6x6xr7/5lR+wOZEMZifzEFika3+XUz8Y/ylS07lMS87DHAkEA7PViT7CGicbgynNaTd5jora+J8fpxjtv3XXQYeN4KhsMSrXDXYn/o8yNsOEhdWhaA7xPDe7nAMbnnr3Omgjh2QJBALUfBYJREvLIaRKjtkfqeXLsR0KxkYD4tNi7nQNGjXrrr+uLM5f5BPSNDt2SfbLczVR3xhiFNg5bC0COXvLNnF0CQQDO3DVdviW94Wc3U7z/hJujcmfUaftz+CRJLHlJio3xfTOUn58HVRC4lxFiStW2/QkyZB6576htIVePwpB/IqFh";
        String pub = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDpPcfjBN0s9Z8blYi4loFD06qIEYIy4iAt9OgH/4ivqGfXm2OTou5c16NhI7mWdxst8eqb1tlCO8teNLE+3053Nh52GeLZEgTlxqxwaFElc6LaqyI5Qj80aiZRsPE+jkgRotTKm1Xbg4XUNRYHKQwAMDKP5mz1q6vVju3IkZk5xQIDAQAB";

        String data =  "/test/body";

        try {
//            String decrypt = RSAUtil.decrypt(encrypted, RSAUtil.getPrivateKey(prikey));
            String encrypt = RsaUtil.encrypt(data, RsaUtil.getPublicKey(pub));
            System.out.println(encrypt);
            String decrypt = RsaUtil.decrypt(encrypt, RsaUtil.getPrivateKey(prikey));
            System.out.println(decrypt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
