package com.appgo.shared.observability;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import java.io.IOException;
import java.util.UUID;

/**
 * Filtre HTTP pour la propagation du correlation-ID.
 * 
 * Ce filtre :
 * - Extrait le correlation-ID du header X-Correlation-Id si présent
 * - Génère un nouveau UUID sinon
 * - Stocke l'ID en MDC pour propagation automatique aux logs
 * - Ajoute le correlation-ID à la réponse
 */
public class CorrelationIdFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Extraire ou générer le correlation-ID
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        // Stocker en MDC pour propagation aux logs
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        
        try {
            // Ajouter le correlation-ID à la réponse
            httpResponse.addHeader(CORRELATION_ID_HEADER, correlationId);
            
            // Continuer la chaîne de filtre
            chain.doFilter(request, response);
        } finally {
            // Nettoyer le MDC
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

}
