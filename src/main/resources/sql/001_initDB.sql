PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;
PRAGMA synchronous = NORMAL;

CREATE TABLE IF NOT EXISTS Unit (
    id INTEGER PRIMARY KEY,
    factionId INTEGER,
    name TEXT,
    points INTEGER,
    M INTEGER,
    T INTEGER,
    SV INTEGER,
    W INTEGER,
    LD INTEGER,
    OC INTEGER,
    category INTEGER,
    composition TEXT,
    keywordIdList TEXT,
    rangedWeaponIdList TEXT,
    meleeWeaponIdList TEXT
);
