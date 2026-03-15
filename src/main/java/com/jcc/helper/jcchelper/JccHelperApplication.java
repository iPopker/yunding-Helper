package com.jcc.helper.jcchelper;

import com.jcc.helper.jcchelper.config.QdrantProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(QdrantProperties.class)
public class JccHelperApplication {

    public static void main(String[] args) {
        SpringApplication.run(JccHelperApplication.class, args);
    }

}
