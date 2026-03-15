package com.jcc.helper.jcchelper.api.dto;

import java.util.List;

public record AnalyzeResponse(
        String gameId,
        int turnIndex,
        String summary,
        String compDirection,
        List<String> actions,
        List<String> reasons,
        List<String> risks,
        List<String> uncertainties,
        double confidence
) {
}
