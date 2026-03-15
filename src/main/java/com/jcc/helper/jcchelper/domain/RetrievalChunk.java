package com.jcc.helper.jcchelper.domain;

import java.util.Map;

public record RetrievalChunk(
        String id,
        String text,
        double score,
        Map<String, Object> metadata
) {
}
