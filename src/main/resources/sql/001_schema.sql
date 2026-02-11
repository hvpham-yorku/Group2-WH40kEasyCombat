-- Auto Generated Script --
PRAGMA foreign_keys = ON;

PRAGMA journal_mode = WAL;

PRAGMA synchronous = NORMAL;

CREATE TABLE IF NOT EXISTS abilities (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS factions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS units (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    factionId INTEGER NOT NULL,
    name TEXT NOT NULL,
    points INTEGER NOT NULL CHECK(points >= 0),
    M INTEGER NOT NULL,
    T INTEGER NOT NULL,
    SV INTEGER NOT NULL,
    W INTEGER NOT NULL,
    LD INTEGER NOT NULL,
    OC INTEGER NOT NULL,
    invulnerableSave INTEGER NOT NULL,
    category INTEGER NOT NULL,
    composition TEXT NOT NULL,
    keywordIdList TEXT NOT NULL,
    rangedWeaponIdList TEXT NOT NULL,
    meleeWeaponIdList TEXT NOT NULL,
    FOREIGN KEY(factionId) REFERENCES factions(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS weapons (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    weaponType TEXT NOT NULL CHECK(weaponType IN ('ranged','melee')),
    attacks INTEGER NOT NULL,
    skill INTEGER NOT NULL,
    strength INTEGER NOT NULL,
    ap INTEGER NOT NULL,
    damage INTEGER NOT NULL,
    range INTEGER
);

