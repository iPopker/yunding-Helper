package com.jcc.helper.jcchelper.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
}
