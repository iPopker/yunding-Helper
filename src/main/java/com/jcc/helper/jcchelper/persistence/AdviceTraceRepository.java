package com.jcc.helper.jcchelper.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AdviceTraceRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonCodec jsonCodec;

    public AdviceTraceRepository(JdbcTemplate jdbcTemplate, JsonCodec jsonCodec) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonCodec = jsonCodec;
    }

    public void save(String gameId, int turnIndex, Object adviceResult) {
        jdbcTemplate.update("""
                        INSERT INTO advice_trace(game_id, turn_index, advice_json)
                        VALUES (?, ?, ?)
                        """,
                gameId,
                turnIndex,
                jsonCodec.toJsonObject(adviceResult)
        );
    }

    public List<AdviceTraceRow> findByGameId(String gameId) {
        return jdbcTemplate.query("""
                        SELECT game_id, turn_index, advice_json
                        FROM advice_trace
                        WHERE game_id = ?
                        ORDER BY turn_index ASC
                        """,
                (rs, rowNum) -> new AdviceTraceRow(
                        rs.getString("game_id"),
                        rs.getInt("turn_index"),
                        rs.getString("advice_json")
                ),
                gameId
        );
    }

    public record AdviceTraceRow(
            String gameId,
            int turnIndex,
            String adviceJson
    ) {
    }
}
