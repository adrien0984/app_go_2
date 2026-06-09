package com.appgo.katago.service;

import com.appgo.katago.config.KataGoProperties;
import com.appgo.katago.port.KataGoException;
import com.appgo.katago.port.KataGoSuggestionPort;
import com.appgo.katago.port.SuggestionRequest;
import com.appgo.katago.port.SuggestionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

/**
 * Service KataGo avec gestion du timeout et du retry.
 */
@Service
public class KataGoService {

    private static final Logger logger = LoggerFactory.getLogger(KataGoService.class);

    private final KataGoSuggestionPort adapter;
    private final KataGoProperties properties;
    private final ExecutorService executorService;

    private SuggestionResponse lastValidSuggestion;

    public KataGoService(KataGoSuggestionPort adapter, KataGoProperties properties) {
        this.adapter = adapter;
        this.properties = properties;
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "KataGo-Executor");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Obtient une suggestion de coup avec gestion du timeout et du retry.
     *
     * @param request La requête de suggestion
     * @return La réponse avec la suggestion, ou null si échec après tous les retry
     */
    public SuggestionResponse getSuggestion(SuggestionRequest request) {
        if (!properties.isEnabled()) {
            logger.warn("Service KataGo désactivé");
            return null;
        }

        for (int attempt = 0; attempt <= properties.getRetryCount(); attempt++) {
            try {
                logger.debug("Tentative {} de suggestion KataGo", attempt + 1);
                SuggestionResponse response = getWithTimeout(request);
                lastValidSuggestion = response;
                return response;
            } catch (TimeoutException e) {
                logger.warn("Timeout lors de la tentative {} de suggestion KataGo", attempt + 1);
                if (attempt < properties.getRetryCount()) {
                    long delayMs = properties.getRetryDelayMs() * (long) Math.pow(2, attempt);
                    logger.debug("Attente de {}ms avant retry", delayMs);
                    sleepOrInterrupt(delayMs);
                }
            } catch (KataGoException e) {
                logger.warn("Erreur KataGo lors de la tentative {}: {}", attempt + 1, e.getMessage());
                if (attempt < properties.getRetryCount()) {
                    long delayMs = properties.getRetryDelayMs() * (long) Math.pow(2, attempt);
                    logger.debug("Attente de {}ms avant retry", delayMs);
                    sleepOrInterrupt(delayMs);
                }
            } catch (Exception e) {
                logger.error("Erreur inattendue lors de la tentative {}", attempt + 1, e);
                if (attempt < properties.getRetryCount()) {
                    long delayMs = properties.getRetryDelayMs() * (long) Math.pow(2, attempt);
                    logger.debug("Attente de {}ms avant retry", delayMs);
                    sleepOrInterrupt(delayMs);
                }
            }
        }

        // Fallback gracieux: utiliser la dernière suggestion valide
        if (lastValidSuggestion != null) {
            logger.warn("Utilisation de la dernière suggestion valide en fallback");
            return lastValidSuggestion;
        }

        logger.error("Tous les retry de suggestion KataGo ont échoué, pas de fallback disponible");
        return null;
    }

    /**
     * Exécute l'appel à l'adaptateur avec timeout.
     */
    private SuggestionResponse getWithTimeout(SuggestionRequest request) throws TimeoutException, KataGoException {
        CompletableFuture<SuggestionResponse> future = CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return adapter.getSuggestion(request);
                    } catch (KataGoException e) {
                        throw new RuntimeException(e);
                    }
                },
                executorService
        );

        try {
            return future.get(properties.getTimeout(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException && e.getCause().getCause() instanceof KataGoException) {
                throw (KataGoException) e.getCause().getCause();
            }
            throw new KataGoException("Erreur lors de l'exécution de la suggestion", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KataGoException("Interruption lors de la suggestion", e);
        }
    }

    private void sleepOrInterrupt(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Interruption lors du delai de retry");
        }
    }

    /**
     * Arrête le service (à appeler lors de la fermeture).
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
