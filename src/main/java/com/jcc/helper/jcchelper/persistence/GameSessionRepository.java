package com.jcc.helper.jcchelper.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class GameSessionRepository {

    private final JdbcTemplate jdbcTemplate;

    public GameSessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public int nextTurnIndex(String gameId) {
        Integer lastTurn = jdbcTemplate.query(
                "SELECT last_turn_index FROM game_session WHERE game_id = ?",
                rs -> rs.next() ? rs.getInt("last_turn_index") : null,
                gameId
        );
        if (lastTurn == null) {
            jdbcTemplate.update("""
                            INSERT INTO game_session(game_id, last_turn_index, created_at, updated_at)
                            VALUES (?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                            """,
                    gameId
            );
            return 1;
        }

        int nextTurn = lastTurn + 1;
        jdbcTemplate.update("""
                        UPDATE game_session
                        SET last_turn_index = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE game_id = ?
                        """,
                nextTurn, gameId
        );
        return nextTurn;
    }
}
