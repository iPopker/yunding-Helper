package com.jcc.helper.jcchelper.persistence;

import com.jcc.helper.jcchelper.domain.GameState;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TurnStateRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonCodec jsonCodec;

    public TurnStateRepository(JdbcTemplate jdbcTemplate, JsonCodec jsonCodec) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonCodec = jsonCodec;
    }

    public void save(GameState state) {
        jdbcTemplate.update("""
                        INSERT INTO turn_state(
                            game_id, turn_index, stage, gold, level, hp,
                            shop_units_json, items_json, board_units_json, bench_units_json, augments_json,
                            observation_confidence, state_confidence
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                state.gameId(),
                state.turnIndex(),
                state.stage(),
                state.gold(),
                state.level(),
                state.hp(),
                jsonCodec.toJson(state.shopUnits()),
                jsonCodec.toJson(state.items()),
                jsonCodec.toJson(state.boardUnits()),
                jsonCodec.toJson(state.benchUnits()),
                jsonCodec.toJson(state.augments()),
                state.observationConfidence(),
                state.stateConfidence()
        );
    }

    public Optional<GameState> findLatestByGameId(String gameId) {
        return jdbcTemplate.query("""
                        SELECT game_id, turn_index, stage, gold, level, hp,
                               shop_units_json, items_json, board_units_json, bench_units_json, augments_json,
                               observation_confidence, state_confidence
                        FROM turn_state
                        WHERE game_id = ?
                        ORDER BY turn_index DESC
                        LIMIT 1
                        """,
                rs -> {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(new GameState(
                            rs.getString("game_id"),
                            rs.getInt("turn_index"),
                            rs.getString("stage"),
                            rs.getInt("gold"),
                            rs.getInt("level"),
                            rs.getInt("hp"),
                            jsonCodec.toStringList(rs.getString("shop_units_json")),
                            jsonCodec.toStringList(rs.getString("items_json")),
                            jsonCodec.toStringList(rs.getString("board_units_json")),
                            jsonCodec.toStringList(rs.getString("bench_units_json")),
                            jsonCodec.toStringList(rs.getString("augments_json")),
                            rs.getDouble("observation_confidence"),
                            rs.getDouble("state_confidence")
                    ));
                },
                gameId
        );
    }
}
