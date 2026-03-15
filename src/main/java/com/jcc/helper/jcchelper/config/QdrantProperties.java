package com.jcc.helper.jcchelper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "qdrant")
public record QdrantProperties(
        String host,
        int port,
        String collection
) {
}
