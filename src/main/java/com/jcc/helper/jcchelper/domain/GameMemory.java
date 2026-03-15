package com.jcc.helper.jcchelper.domain;

import java.util.List;

public record GameMemory(
        String gameId,
        int turnIndex,
        String summary,
        List<String> keyRisks,
        String continuityNotes
) {
}
