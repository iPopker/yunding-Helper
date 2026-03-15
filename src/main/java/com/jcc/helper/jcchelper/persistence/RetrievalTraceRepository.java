package com.jcc.helper.jcchelper.persistence;

import com.jcc.helper.jcchelper.domain.RetrievalChunk;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RetrievalTraceRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonCodec jsonCodec;

    public RetrievalTraceRepository(JdbcTemplate jdbcTemplate, JsonCodec jsonCodec) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonCodec = jsonCodec;
    }

    public void save(String gameId, int turnIndex, String queryText, List<RetrievalChunk> hitChunks) {
        jdbcTemplate.update("""
                        INSERT INTO retrieval_trace(game_id, turn_index, query_text, hit_chunks_json)
                        VALUES (?, ?, ?, ?)
                        """,
                gameId,
                turnIndex,
                queryText,
                jsonCodec.toJsonObject(hitChunks)
        );
    }

    public List<RetrievalTraceRow> findByGameId(String gameId) {
        return jdbcTemplate.query("""
                        SELECT game_id, turn_index, query_text, hit_chunks_json
                        FROM retrieval_trace
                        WHERE game_id = ?
                        ORDER BY turn_index ASC
                        """,
                (rs, rowNum) -> new RetrievalTraceRow(
                        rs.getString("game_id"),
                        rs.getInt("turn_index"),
                        rs.getString("query_text"),
                        rs.getString("hit_chunks_json")
                ),
                gameId
        );
    }

    public record RetrievalTraceRow(
            String gameId,
            int turnIndex,
            String queryText,
            String hitChunksJson
    ) {
    }
}
