package com.jcc.helper.jcchelper.domain;

import java.util.List;

public record Observation(
        String stage,
        int gold,
        int level,
        int hp,
        List<String> shopUnits,
        List<String> items,
        List<String> boardUnits,
        List<String> benchUnits,
        List<String> augments,
        double confidence
) {
}
