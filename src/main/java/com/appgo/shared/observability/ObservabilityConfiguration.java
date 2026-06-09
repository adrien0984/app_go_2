package com.appgo.shared.observability;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration pour l'observabilité : correlation-ID, métriques, etc.
 */
@Configuration
public class ObservabilityConfiguration {

    /**
     * Enregistre le CorrelationIdFilter avec une haute priorité
     * pour que tous les autres filtres aient accès au correlation-ID.
     */
    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
        FilterRegistrationBean<CorrelationIdFilter> bean = 
            new FilterRegistrationBean<>(new CorrelationIdFilter());
        
        // Ordre élevé = exécution en premier dans la chaîne de filtre
        bean.setOrder(-100);
        
        return bean;
    }

}
