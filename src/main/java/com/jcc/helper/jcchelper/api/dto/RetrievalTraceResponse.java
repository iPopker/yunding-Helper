package com.jcc.helper.jcchelper.api.dto;

public record RetrievalTraceResponse(
        int turnIndex,
        String queryText,
        String hitChunksJson
) {
}
