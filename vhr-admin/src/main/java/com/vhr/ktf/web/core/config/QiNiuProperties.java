package com.vhr.ktf.web.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author github.com/kuangtf
 * @date 2022/2/15 14:37
 */
@Configuration
public class QiNiuProperties {

    private static QiNiuProperties properties;

    public QiNiuProperties() {
        properties = this;
    }

    @Value("${qiniu.accessKey}")
    private String accessKey;

    @Value("${qiniu.secretKey}")
    private String secretKey;

    @Value("${qiniu.bucketName}")
    private String bucketName;

    @Value("${qiniu.domain}")
    private String doMain;

    public static QiNiuProperties getInstance() {
        return properties;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getDoMain() {
        return doMain;
    }
}
