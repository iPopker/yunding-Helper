package com.jcc.helper.jcchelper.persistence;

import com.jcc.helper.jcchelper.domain.StateDiff;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StateDiffRepository {

    private final JdbcTemplate jdbcTemplate;

    public StateDiffRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(StateDiff diff) {
        jdbcTemplate.update("""
                        INSERT INTO state_diff(
                            game_id, from_turn, to_turn, gold_delta, hp_delta, level_delta,
                            shop_changed, board_changed, bench_changed, item_changed, summary
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                diff.gameId(),
                diff.fromTurn(),
                diff.toTurn(),
                diff.goldDelta(),
                diff.hpDelta(),
                diff.levelDelta(),
                diff.shopChanged() ? 1 : 0,
                diff.boardChanged() ? 1 : 0,
                diff.benchChanged() ? 1 : 0,
                diff.itemChanged() ? 1 : 0,
                diff.summary()
        );
    }

    public List<StateDiff> findByGameId(String gameId) {
        return jdbcTemplate.query("""
                        SELECT game_id, from_turn, to_turn, gold_delta, hp_delta, level_delta,
                               shop_changed, board_changed, bench_changed, item_changed, summary
                        FROM state_diff
                        WHERE game_id = ?
                        ORDER BY to_turn ASC
                        """,
                (rs, rowNum) -> new StateDiff(
                        rs.getString("game_id"),
                        (Integer) rs.getObject("from_turn"),
                        rs.getInt("to_turn"),
                        (Integer) rs.getObject("gold_delta"),
                        (Integer) rs.getObject("hp_delta"),
                        (Integer) rs.getObject("level_delta"),
                        rs.getInt("shop_changed") == 1,
                        rs.getInt("board_changed") == 1,
                        rs.getInt("bench_changed") == 1,
                        rs.getInt("item_changed") == 1,
                        rs.getString("summary")
                ),
                gameId
        );
    }
}
