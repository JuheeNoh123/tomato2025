package com.sku_likelion.Moving_Cash_back.config.external.kakao;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao")
@Getter @Setter
public class KakaoProperties {
    private String restKey;
    private String baseUrl;
}
