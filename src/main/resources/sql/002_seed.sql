-- =========================================================
-- 002_seed.sql (FIXED)
-- Seeds ONLY ONCE using PRAGMA user_version (0 -> not seeded, 1 -> seeded)
-- Does NOT create armies.
-- =========================================================
BEGIN TRANSACTION;
PRAGMA defer_foreign_keys = ON;

-- ---------------------------------------------------------
-- factions (4)
-- ---------------------------------------------------------
INSERT INTO factions (id, name)
SELECT 1, 'Space Marines'
WHERE (SELECT user_version FROM pragma_user_version)=0;

INSERT INTO factions (id, name)
SELECT 2, 'Orks'
WHERE (SELECT user_version FROM pragma_user_version)=0;

INSERT INTO factions (id, name)
SELECT 3, 'Aeldari'
WHERE (SELECT user_version FROM pragma_user_version)=0;

INSERT INTO factions (id, name)
SELECT 4, 'Necrons'
WHERE (SELECT user_version FROM pragma_user_version)=0;

-- ---------------------------------------------------------
-- detachments (4)
-- ---------------------------------------------------------
INSERT INTO detachments (id, name, factionId, strategyId, detachmentRule)
SELECT 1, 'Gladius Task Force', 1, NULL, 'Adaptive doctrine and flexible tactics.'
WHERE (SELECT user_version FROM pragma_user_version)=0;

INSERT INTO detachments (id, name, factionId, strategyId, detachmentRule)
SELECT 2, 'Waaagh! Warband', 2, NULL, 'Charge harder when the Waaagh! is called.'
WHERE (SELECT user_version FROM pragma_user_version)=0;

INSERT INTO detachments (id, name, factionId, strategyId, detachmentRule)
SELECT 3, 'Battle Host', 3, NULL, 'Speed and precision strikes.'
WHERE (SELECT user_version FROM pragma_user_version)=0;

INSERT INTO detachments (id, name, factionId, strategyId, detachmentRule)
SELECT 4, 'Awakened Dynasty', 4, NULL, 'Relentless advance and reanimation protocols.'
WHERE (SELECT user_version FROM pragma_user_version)=0;

-- ---------------------------------------------------------
-- core_abilities (8)
-- ---------------------------------------------------------
INSERT INTO core_abilities (id, ability)
SELECT 1, 'Deep Strike' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO core_abilities (id, ability)
SELECT 2, 'Feel No Pain (6+)' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO core_abilities (id, ability)
SELECT 3, 'Infiltrators' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO core_abilities (id, ability)
SELECT 4, 'Lone Operative' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO core_abilities (id, ability)
SELECT 5, 'Stealth' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO core_abilities (id, ability)
SELECT 6, 'Sustained Hits 1' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO core_abilities (id, ability)
SELECT 7, 'Lethal Hits' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO core_abilities (id, ability)
SELECT 8, 'Devastating Wounds' WHERE (SELECT user_version FROM pragma_user_version)=0;

-- ---------------------------------------------------------
-- other_abilities (8)
-- ---------------------------------------------------------
INSERT INTO other_abilities (id, ability)
SELECT 1, 'Reroll 1s to Hit while on an objective.' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO other_abilities (id, ability)
SELECT 2, '+1 OC while within 6" of a friendly Character.' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO other_abilities (id, ability)
SELECT 3, 'Once per battle: gain 1 CP.' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO other_abilities (id, ability)
SELECT 4, 'Reactive Move: 6" after being targeted.' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO other_abilities (id, ability)
SELECT 5, 'Ignore cover against targets within 12".' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO other_abilities (id, ability)
SELECT 6, 'Critical hits on 5+ when charging.' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO other_abilities (id, ability)
SELECT 7, 'Enemy units suffer -1 to Battleshock tests within 9".' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO other_abilities (id, ability)
SELECT 8, 'After this unit destroys an enemy: regain D3 wounds.' WHERE (SELECT user_version FROM pragma_user_version)=0;

-- ---------------------------------------------------------
-- unit_keywords (12)
-- ---------------------------------------------------------
INSERT INTO unit_keywords (id, keyword)
SELECT 1,'INFANTRY' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO unit_keywords (id, keyword)
SELECT 2,'CHARACTER' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO unit_keywords (id, keyword)
SELECT 3,'VEHICLE' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO unit_keywords (id, keyword)
SELECT 4,'BATTLELINE' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO unit_keywords (id, keyword)
SELECT 5,'ELITE' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO unit_keywords (id, keyword)
SELECT 6,'SCOUT' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO unit_keywords (id, keyword)
SELECT 7,'PSYKER' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO unit_keywords (id, keyword)
SELECT 8,'FLY' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO unit_keywords (id, keyword)
SELECT 9,'TERMINATOR' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO unit_keywords (id, keyword)
SELECT 10,'MONSTER' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO unit_keywords (id, keyword)
SELECT 11,'STEALTH' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO unit_keywords (id, keyword)
SELECT 12,'ARTILLERY' WHERE (SELECT user_version FROM pragma_user_version)=0;

-- ---------------------------------------------------------
-- weapon_keywords (12)
-- ---------------------------------------------------------
INSERT INTO weapon_keywords (id, keyword)
SELECT 1,'ASSAULT' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO weapon_keywords (id, keyword)
SELECT 2,'HEAVY' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO weapon_keywords (id, keyword)
SELECT 3,'RAPID FIRE' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO weapon_keywords (id, keyword)
SELECT 4,'PISTOL' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO weapon_keywords (id, keyword)
SELECT 5,'MELTA' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO weapon_keywords (id, keyword)
SELECT 6,'BLAST' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO weapon_keywords (id, keyword)
SELECT 7,'SUSTAINED HITS 1' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO weapon_keywords (id, keyword)
SELECT 8,'LETHAL HITS' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO weapon_keywords (id, keyword)
SELECT 9,'DEVASTATING WOUNDS' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO weapon_keywords (id, keyword)
SELECT 10,'TWIN-LINKED' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO weapon_keywords (id, keyword)
SELECT 11,'IGNORES COVER' WHERE (SELECT user_version FROM pragma_user_version)=0;
INSERT INTO weapon_keywords (id, keyword)
SELECT 12,'ANTI-INFANTRY 4+' WHERE (SELECT user_version FROM pragma_user_version)=0;

-- ---------------------------------------------------------
-- melee_weapons (16) generated
-- ---------------------------------------------------------
WITH RECURSIVE n(i) AS (SELECT 1 UNION ALL SELECT i+1 FROM n WHERE i<16)
INSERT INTO melee_weapons (id, name, A, WS, S, AP, D, keywordIdList)
SELECT
  i,
  'Melee Weapon ' || i,
  '3',
  3 + (i % 3),
  4 + (i % 3),
  -1 * (i % 3),
  '1',
  CAST(((i % 12) + 1) AS TEXT) || ',' || CAST((((i + 3) % 12) + 1) AS TEXT)
FROM n
WHERE (SELECT user_version FROM pragma_user_version)=0;

-- ---------------------------------------------------------
-- ranged_weapons (24) generated
-- ---------------------------------------------------------
WITH RECURSIVE n(i) AS (SELECT 1 UNION ALL SELECT i+1 FROM n WHERE i<24)
INSERT INTO ranged_weapons (id, name, range, A, BS, S, AP, D, keywordIdList)
SELECT
  i,
  'Ranged Weapon ' || i,
  12 + (i % 5) * 6,
  '2',
  3 + (i % 3),
  4 + (i % 4),
  -1 * (i % 3),
  '1',
  CAST(((i % 12) + 1) AS TEXT) || ',' || CAST((((i + 5) % 12) + 1) AS TEXT)
FROM n
WHERE (SELECT user_version FROM pragma_user_version)=0;

-- ---------------------------------------------------------
-- units (40) generated
-- ---------------------------------------------------------
WITH RECURSIVE nums(n) AS (SELECT 1 UNION ALL SELECT n+1 FROM nums WHERE n<40)
INSERT INTO units (
  id, factionId, name, points, M, T, SV, W, LD, OC,
  invulnerableSave, category, composition,
  coreAbilityIdList, otherAbilityIdList, keywordIdList,
  rangedWeaponIdList, meleeWeaponIdList
)
SELECT
  n AS id,
  CAST(((n - 1) / 10) AS INTEGER) + 1 AS factionId,
  (SELECT name FROM factions f WHERE f.id = (CAST(((n - 1) / 10) AS INTEGER) + 1))
    || ' Unit ' || CAST((((n - 1) % 10) + 1) AS TEXT),
  50,
  6 + ((((n - 1) % 10) + 1) % 3) * 2,
  4 + ((((n - 1) % 10) + 1) % 3),
  3 + ((((n - 1) % 10) + 1) % 2),
  2 + ((((n - 1) % 10) + 1) % 4),
  6 + ((((n - 1) % 10) + 1) % 3),
  1 + ((((n - 1) % 10) + 1) % 2),

  CASE WHEN ((((n - 1) % 10) + 1) % 3) = 1 THEN 4 ELSE 0 END,
  CASE WHEN ((((n - 1) % 10) + 1) % 3) = 1 THEN 1
       WHEN ((((n - 1) % 10) + 1) % 3) = 2 THEN 2
       ELSE 3 END,
  CASE WHEN ((((n - 1) % 10) + 1) % 3) = 1 THEN '1 model'
       WHEN ((((n - 1) % 10) + 1) % 3) = 2 THEN '5 models'
       ELSE '1 vehicle' END,

  CAST(((n % 8) + 1) AS TEXT) || ',' || CAST((((n + 3) % 8) + 1) AS TEXT),
  CAST((((n + 1) % 8) + 1) AS TEXT),
  CAST((CASE WHEN ((((n - 1) % 10) + 1) % 3) = 1 THEN 2
             WHEN ((((n - 1) % 10) + 1) % 3) = 2 THEN 1
             ELSE 3 END) AS TEXT)
    || ',' || CAST((((n + 2) % 12) + 1) AS TEXT)
    || ',' || CAST((((n + 7) % 12) + 1) AS TEXT),

  CAST((((n - 1) % 24) + 1) AS TEXT) || ',' || CAST((((n + 5) % 24) + 1) AS TEXT),
  CAST((((n - 1) % 16) + 1) AS TEXT)
FROM nums
WHERE (SELECT user_version FROM pragma_user_version)=0;

-- mark seeded
PRAGMA user_version = 1;

COMMIT;
