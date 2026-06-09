package com.appgo.katago.config;

import com.appgo.katago.adapter.FakeKataGoAdapter;
import com.appgo.katago.port.KataGoSuggestionPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Spring pour KataGo.
 */
@Configuration
public class KataGoConfiguration {

    @Bean
    public KataGoSuggestionPort kataGoSuggestionPort() {
        return new FakeKataGoAdapter();
    }
}
