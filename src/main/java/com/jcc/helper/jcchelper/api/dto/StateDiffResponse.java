package com.jcc.helper.jcchelper.api.dto;

public record StateDiffResponse(
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
