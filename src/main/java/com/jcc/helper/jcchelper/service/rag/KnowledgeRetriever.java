package com.jcc.helper.jcchelper.service.rag;

import com.jcc.helper.jcchelper.domain.GameState;
import com.jcc.helper.jcchelper.domain.RetrievalChunk;

import java.util.List;

public interface KnowledgeRetriever {

    List<RetrievalChunk> retrieve(String query, GameState state, int topK);
}
