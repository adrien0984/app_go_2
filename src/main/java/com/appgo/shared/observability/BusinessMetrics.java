package com.appgo.shared.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Composant pour enregistrer les métriques métier personnalisées.
 * 
 * Exposed via /actuator/metrics pour le monitoring.
 */
@Component
public class BusinessMetrics {

    private final Counter gameCreatedCounter;
    private final Counter gameMoveCounter;
    private final Counter gamePassCounter;
    private final Timer moveExecutionTimer;
    private final Timer katagoBuildSuggestionsTimer;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.gameCreatedCounter = Counter.builder("game.created")
                .description("Nombre total de parties créées")
                .register(meterRegistry);
        
        this.gameMoveCounter = Counter.builder("game.move")
                .description("Nombre total de coups joués")
                .register(meterRegistry);
        
        this.gamePassCounter = Counter.builder("game.pass")
                .description("Nombre total de passes")
                .register(meterRegistry);
        
        this.moveExecutionTimer = Timer.builder("game.move.duration")
                .description("Durée d'exécution d'un coup")
                .register(meterRegistry);
        
        this.katagoBuildSuggestionsTimer = Timer.builder("katago.build.suggestions.duration")
                .description("Durée de construction des suggestions KataGo")
                .register(meterRegistry);
    }

    public void recordGameCreated() {
        gameCreatedCounter.increment();
    }

    public void recordGameMove() {
        gameMoveCounter.increment();
    }

    public void recordGamePass() {
        gamePassCounter.increment();
    }

    public Timer.Sample recordMoveExecutionStart() {
        return Timer.start();
    }

    public void recordMoveExecutionStop(Timer.Sample sample) {
        sample.stop(moveExecutionTimer);
    }

    public Timer.Sample recordKataGoBuildSuggestionsStart() {
        return Timer.start();
    }

    public void recordKataGoBuildSuggestionsStop(Timer.Sample sample) {
        sample.stop(katagoBuildSuggestionsTimer);
    }

}
