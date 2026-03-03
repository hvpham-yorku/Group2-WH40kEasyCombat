-- Auto Generated Script --
PRAGMA foreign_keys = ON;

PRAGMA journal_mode = WAL;

PRAGMA synchronous = NORMAL;

CREATE TABLE IF NOT EXISTS Abilities (
    id TEXT NOT NULL,
    faction_id TEXT NOT NULL,
    name TEXT,
    legend TEXT,
    description TEXT,
    PRIMARY KEY (id, faction_id)
);

CREATE TABLE IF NOT EXISTS Datasheets (
    id TEXT PRIMARY KEY,
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
    datasheet_id TEXT NOT NULL,
    line TEXT NOT NULL,
    ability_id TEXT,
    model TEXT,
    name TEXT,
    description TEXT,
    type TEXT,
    parameter TEXT,
    PRIMARY KEY (datasheet_id, line)
);

CREATE TABLE IF NOT EXISTS Datasheets_detachment_abilities (
    datasheet_id TEXT NOT NULL,
    detachment_ability_id TEXT NOT NULL,
    PRIMARY KEY (datasheet_id, detachment_ability_id)
);

CREATE TABLE IF NOT EXISTS Datasheets_enhancements (
    datasheet_id TEXT NOT NULL,
    enhancement_id TEXT NOT NULL,
    PRIMARY KEY (datasheet_id, enhancement_id)
);

CREATE TABLE IF NOT EXISTS Datasheets_keywords (
    datasheet_id TEXT NOT NULL,
    keyword TEXT NOT NULL,
    model TEXT NOT NULL,
    is_faction_keyword TEXT,
    PRIMARY KEY (datasheet_id, keyword, model)
);

CREATE TABLE IF NOT EXISTS Datasheets_leader (
    leader_id TEXT NOT NULL,
    attached_id TEXT NOT NULL,
    PRIMARY KEY (leader_id, attached_id)
);

CREATE TABLE IF NOT EXISTS Datasheets_models (
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
    base_size_descr TEXT,
    PRIMARY KEY (datasheet_id, line)
);

CREATE TABLE IF NOT EXISTS Datasheets_models_cost (
    datasheet_id TEXT NOT NULL,
    line TEXT NOT NULL,
    description TEXT,
    cost TEXT,
    PRIMARY KEY (datasheet_id, line)
);

CREATE TABLE IF NOT EXISTS Datasheets_options (
    datasheet_id TEXT NOT NULL,
    line TEXT NOT NULL,
    button TEXT,
    description TEXT,
    PRIMARY KEY (datasheet_id, line)
);

CREATE TABLE IF NOT EXISTS Datasheets_stratagems (
    datasheet_id TEXT NOT NULL,
    stratagem_id TEXT NOT NULL,
    PRIMARY KEY (datasheet_id, stratagem_id)
);

CREATE TABLE IF NOT EXISTS Datasheets_unit_composition (
    datasheet_id TEXT NOT NULL,
    line TEXT NOT NULL,
    description TEXT,
    PRIMARY KEY (datasheet_id, line)
);

CREATE TABLE IF NOT EXISTS Datasheets_wargear (
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
    D TEXT,
    PRIMARY KEY (datasheet_id, line, line_in_wargear)
);

CREATE TABLE IF NOT EXISTS Detachments (
    id TEXT NOT NULL,
    faction_id TEXT NOT NULL,
    name TEXT,
    legend TEXT,
    type TEXT,
    PRIMARY KEY (id, faction_id)
);

CREATE TABLE IF NOT EXISTS Detachment_abilities (
    id TEXT NOT NULL,
    detachment_id TEXT NOT NULL,
    faction_id TEXT,
    name TEXT,
    legend TEXT,
    description TEXT,
    detachment TEXT,
    PRIMARY KEY (id, detachment_id)
);

CREATE TABLE IF NOT EXISTS Enhancements (
    id TEXT PRIMARY KEY,
    faction_id TEXT,
    name TEXT,
    legend TEXT,
    description TEXT,
    cost TEXT,
    detachment TEXT,
    detachment_id TEXT
);

CREATE TABLE IF NOT EXISTS Factions (
    id TEXT PRIMARY KEY,
    name TEXT,
    link TEXT
);

CREATE TABLE IF NOT EXISTS Last_update (
    last_update TEXT
);

CREATE TABLE IF NOT EXISTS Source (
    id TEXT PRIMARY KEY,
    name TEXT,
    type TEXT,
    edition TEXT,
    version TEXT,
    errata_date TEXT,
    errata_link TEXT
);

CREATE TABLE IF NOT EXISTS Stratagems (
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
    detachment_id TEXT,
    PRIMARY KEY (id, faction_id)
);

