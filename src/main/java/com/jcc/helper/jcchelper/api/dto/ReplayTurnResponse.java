package com.jcc.helper.jcchelper.api.dto;

import java.util.List;

public record ReplayTurnResponse(
        int turnIndex,
        String stage,
        int gold,
        int level,
        int hp,
        double stateConfidence,
        String diffSummary,
        String memorySummary,
        String continuityNotes,
        String adviceJson,
        String retrievalQuery,
        String retrievalHitsJson,
        List<String> keyRisks
) {
}
