package com.jcc.helper.jcchelper.domain;

public record StateDiff(
        String gameId,
        Integer fromTurn,
        int toTurn,
        Integer goldDelta,
        Integer hpDelta,
        Integer levelDelta,
        boolean shopChanged,
        boolean boardChanged,
        boolean benchChanged,
        boolean itemChanged,
        String summary
) {
}
