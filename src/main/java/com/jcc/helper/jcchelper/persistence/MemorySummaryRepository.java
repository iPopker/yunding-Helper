package com.jcc.helper.jcchelper.persistence;

import com.jcc.helper.jcchelper.domain.GameMemory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MemorySummaryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonCodec jsonCodec;

    public MemorySummaryRepository(JdbcTemplate jdbcTemplate, JsonCodec jsonCodec) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonCodec = jsonCodec;
    }

    public void save(GameMemory memory) {
        jdbcTemplate.update("""
                        INSERT INTO memory_summary(game_id, turn_index, summary, key_risks_json, continuity_notes)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                memory.gameId(),
                memory.turnIndex(),
                memory.summary(),
                jsonCodec.toJson(memory.keyRisks()),
                memory.continuityNotes()
        );
    }

    public Optional<GameMemory> findLatestByGameId(String gameId) {
        return jdbcTemplate.query("""
                        SELECT game_id, turn_index, summary, key_risks_json, continuity_notes
                        FROM memory_summary
                        WHERE game_id = ?
                        ORDER BY turn_index DESC
                        LIMIT 1
                        """,
                rs -> {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(new GameMemory(
                            rs.getString("game_id"),
                            rs.getInt("turn_index"),
                            rs.getString("summary"),
                            jsonCodec.toStringList(rs.getString("key_risks_json")),
                            rs.getString("continuity_notes")
                    ));
                },
                gameId
        );
    }
}
