-- Auto Generated Script --
PRAGMA foreign_keys = ON;

PRAGMA journal_mode = WAL;

PRAGMA synchronous = NORMAL;

CREATE TABLE IF NOT EXISTS Abilities (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    id TEXT NOT NULL,
    faction_id TEXT NOT NULL,
    name TEXT,
    legend TEXT,
    description TEXT
);

CREATE TABLE IF NOT EXISTS Datasheets (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    id TEXT NOT NULL,
    name TEXT,
    faction_id TEXT,
    source_id TEXT,
    legend TEXT,
    role TEXT,
    loadout TEXT,
    transport TEXT,
    virtual INTEGER CHECK(virtual IN (0,1)),
    leader_head TEXT,
    leader_footer TEXT,
    damaged_w TEXT,
    damaged_description TEXT,
    link TEXT
);

CREATE TABLE IF NOT EXISTS Datasheets_abilities (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    datasheet_id TEXT NOT NULL,
    line TEXT NOT NULL,
    ability_id TEXT,
    model TEXT,
    name TEXT,
    description TEXT,
    type TEXT,
    parameter TEXT
);

CREATE TABLE IF NOT EXISTS Datasheets_detachment_abilities (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    datasheet_id TEXT NOT NULL,
    detachment_ability_id TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS Datasheets_enhancements (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    datasheet_id TEXT NOT NULL,
    enhancement_id TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS Datasheets_keywords (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    datasheet_id TEXT NOT NULL,
    keyword TEXT NOT NULL,
    model TEXT NOT NULL,
    is_faction_keyword TEXT
);

CREATE TABLE IF NOT EXISTS Datasheets_leader (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    leader_id TEXT NOT NULL,
    attached_id TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS Datasheets_models (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    datasheet_id TEXT NOT NULL,
    line TEXT NOT NULL,
    name TEXT,
    M TEXT,
    T TEXT,
    Sv TEXT,
    inv_sv TEXT,
    inv_sv_descr TEXT,
    W TEXT,
    Ld TEXT,
    OC TEXT,
    base_size TEXT,
    base_size_descr TEXT
);

CREATE TABLE IF NOT EXISTS Datasheets_models_cost (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    datasheet_id TEXT NOT NULL,
    line TEXT NOT NULL,
    description TEXT,
    cost TEXT
);

CREATE TABLE IF NOT EXISTS Datasheets_options (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    datasheet_id TEXT NOT NULL,
    line TEXT NOT NULL,
    button TEXT,
    description TEXT
);

CREATE TABLE IF NOT EXISTS Datasheets_stratagems (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    datasheet_id TEXT NOT NULL,
    stratagem_id TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS Datasheets_unit_composition (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    datasheet_id TEXT NOT NULL,
    line TEXT NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS Datasheets_wargear (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    datasheet_id TEXT NOT NULL,
    line TEXT NOT NULL,
    line_in_wargear TEXT NOT NULL,
    dice TEXT,
    name TEXT,
    description TEXT,
    range TEXT,
    type TEXT,
    A TEXT,
    BS_WS TEXT,
    S TEXT,
    AP TEXT,
    D TEXT
);

CREATE TABLE IF NOT EXISTS Detachments (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    id TEXT NOT NULL,
    faction_id TEXT NOT NULL,
    name TEXT,
    legend TEXT,
    type TEXT
);

CREATE TABLE IF NOT EXISTS Detachment_abilities (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    id TEXT NOT NULL,
    detachment_id TEXT NOT NULL,
    faction_id TEXT,
    name TEXT,
    legend TEXT,
    description TEXT,
    detachment TEXT
);

CREATE TABLE IF NOT EXISTS Enhancements (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    id TEXT NOT NULL,
    faction_id TEXT,
    name TEXT,
    legend TEXT,
    description TEXT,
    cost TEXT,
    detachment TEXT,
    detachment_id TEXT
);

CREATE TABLE IF NOT EXISTS Factions (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    id TEXT NOT NULL,
    name TEXT,
    link TEXT
);

CREATE TABLE IF NOT EXISTS Last_update (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    last_update TEXT
);

CREATE TABLE IF NOT EXISTS Source (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    id TEXT NOT NULL,
    name TEXT,
    type TEXT,
    edition TEXT,
    version TEXT,
    errata_date TEXT,
    errata_link TEXT
);

CREATE TABLE IF NOT EXISTS Stratagems (
    auto_id INTEGER PRIMARY KEY AUTOINCREMENT,
    id TEXT NOT NULL,
    faction_id TEXT NOT NULL,
    name TEXT,
    type TEXT,
    cp_cost TEXT,
    legend TEXT,
    turn TEXT,
    phase TEXT,
    description TEXT,
    detachment TEXT,
    detachment_id TEXT
);

