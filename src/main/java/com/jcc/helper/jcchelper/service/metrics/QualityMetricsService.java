package com.jcc.helper.jcchelper.service.metrics;

import com.jcc.helper.jcchelper.api.dto.QualityMetricsResponse;
import com.jcc.helper.jcchelper.domain.GameState;
import com.jcc.helper.jcchelper.service.advice.StructuredAdvice;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class QualityMetricsService {

    private final AtomicLong totalAnalyzeCount = new AtomicLong(0);
    private final AtomicLong totalFields = new AtomicLong(0);
    private final AtomicLong recognizedFields = new AtomicLong(0);
    private final AtomicLong lowConfidenceCount = new AtomicLong(0);
    private final AtomicLong stableSuggestionCount = new AtomicLong(0);
    private final AtomicLong comparableSuggestionCount = new AtomicLong(0);
    private final List<Long> latenciesMs = java.util.Collections.synchronizedList(new ArrayList<>());
    private final Map<String, String> lastSuggestionFingerprintByGame = new ConcurrentHashMap<>();

    public void recordAnalyze(String gameId, GameState state, StructuredAdvice advice, long latencyMs, boolean lowConfidence) {
        totalAnalyzeCount.incrementAndGet();
        recordFieldRecognition(state);
        if (lowConfidence) {
            lowConfidenceCount.incrementAndGet();
        }
        recordStability(gameId, advice);
        latenciesMs.add(latencyMs);
    }

    public QualityMetricsResponse snapshot() {
        long total = totalAnalyzeCount.get();
        double recognitionRate = rate(recognizedFields.get(), totalFields.get());
        double lowConfidenceRatio = rate(lowConfidenceCount.get(), total);
        double stability = rate(stableSuggestionCount.get(), comparableSuggestionCount.get());

        List<Long> sorted;
        synchronized (latenciesMs) {
            sorted = new ArrayList<>(latenciesMs);
        }
        sorted.sort(Comparator.naturalOrder());

        return new QualityMetricsResponse(
                total,
                recognitionRate,
                lowConfidenceRatio,
                stability,
                percentile(sorted, 50),
                percentile(sorted, 95)
        );
    }

    private void recordFieldRecognition(GameState state) {
        int fields = 9;
        int recognized = 0;
        if (state.stage() != null && !state.stage().isBlank()) {
            recognized++;
        }
        if (state.gold() >= 0) {
            recognized++;
        }
        if (state.level() > 0) {
            recognized++;
        }
        if (state.hp() >= 0) {
            recognized++;
        }
        if (state.shopUnits() != null && !state.shopUnits().isEmpty()) {
            recognized++;
        }
        if (state.items() != null && !state.items().isEmpty()) {
            recognized++;
        }
        if (state.boardUnits() != null && !state.boardUnits().isEmpty()) {
            recognized++;
        }
        if (state.benchUnits() != null && !state.benchUnits().isEmpty()) {
            recognized++;
        }
        if (state.augments() != null && !state.augments().isEmpty()) {
            recognized++;
        }
        totalFields.addAndGet(fields);
        recognizedFields.addAndGet(recognized);
    }

    private void recordStability(String gameId, StructuredAdvice advice) {
        String fingerprint = advice.compDirection() + "|" + String.join(";", advice.actions());
        String previous = lastSuggestionFingerprintByGame.put(gameId, fingerprint);
        if (previous == null) {
            return;
        }
        comparableSuggestionCount.incrementAndGet();
        if (previous.equals(fingerprint)) {
            stableSuggestionCount.incrementAndGet();
        }
    }

    private double percentile(List<Long> sorted, int percentile) {
        if (sorted.isEmpty()) {
            return 0.0;
        }
        int index = (int) Math.ceil((percentile / 100.0) * sorted.size()) - 1;
        int bounded = Math.max(0, Math.min(index, sorted.size() - 1));
        return sorted.get(bounded);
    }

    private double rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0.0;
        }
        return (double) numerator / (double) denominator;
    }
}
