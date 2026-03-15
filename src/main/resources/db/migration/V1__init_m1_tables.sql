CREATE TABLE IF NOT EXISTS game_session (
    game_id TEXT PRIMARY KEY,
    last_turn_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS turn_state (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_id TEXT NOT NULL,
    turn_index INTEGER NOT NULL,
    stage TEXT,
    gold INTEGER,
    level INTEGER,
    hp INTEGER,
    shop_units_json TEXT NOT NULL,
    items_json TEXT NOT NULL,
    board_units_json TEXT NOT NULL,
    bench_units_json TEXT NOT NULL,
    augments_json TEXT NOT NULL,
    observation_confidence REAL NOT NULL,
    state_confidence REAL NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (game_id, turn_index)
);

CREATE TABLE IF NOT EXISTS state_diff (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_id TEXT NOT NULL,
    from_turn INTEGER,
    to_turn INTEGER NOT NULL,
    gold_delta INTEGER,
    hp_delta INTEGER,
    level_delta INTEGER,
    shop_changed INTEGER NOT NULL,
    board_changed INTEGER NOT NULL,
    bench_changed INTEGER NOT NULL,
    item_changed INTEGER NOT NULL,
    summary TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS memory_summary (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_id TEXT NOT NULL,
    turn_index INTEGER NOT NULL,
    summary TEXT NOT NULL,
    key_risks_json TEXT NOT NULL,
    continuity_notes TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS retrieval_trace (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_id TEXT NOT NULL,
    turn_index INTEGER NOT NULL,
    query_text TEXT NOT NULL,
    hit_chunks_json TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS advice_trace (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_id TEXT NOT NULL,
    turn_index INTEGER NOT NULL,
    advice_json TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
