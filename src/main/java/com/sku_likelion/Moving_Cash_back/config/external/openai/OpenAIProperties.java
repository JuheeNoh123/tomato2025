package com.sku_likelion.Moving_Cash_back.config.external.openai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "openai")
@Getter
@Setter
public class OpenAIProperties {
    private String apiKey;
    private String model;
    private int timeoutSeconds;
}
