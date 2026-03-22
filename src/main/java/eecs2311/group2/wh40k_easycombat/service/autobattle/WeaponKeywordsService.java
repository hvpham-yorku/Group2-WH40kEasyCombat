package eecs2311.group2.wh40k_easycombat.service.autobattle;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record WeaponKeywordsService(
        boolean assault,
        boolean pistol,
        boolean torrent,
        boolean ignoresCover,
        boolean lethalHits,
        boolean twinLinked,
        boolean lance,
        boolean heavy,
        boolean blast,
        boolean devastatingWounds,
        boolean hazardous,
        boolean precision,
        boolean extraAttacks,
        boolean oneShot,
        boolean psychic,
        String rapidFireBonus,
        String sustainedHitsBonus,
        String meltaBonus,
        Integer antiInfantry,
        Integer antiVehicle,
        Integer antiMonster,
        Integer antiCharacter,
        Integer antiPsyker
) {
    private static final Pattern RAPID_FIRE_PATTERN = Pattern.compile("^rapid fire\\s+(.+)$");
    private static final Pattern SUSTAINED_HITS_PATTERN = Pattern.compile("^sustained hits\\s+(.+)$");
    private static final Pattern MELTA_PATTERN = Pattern.compile("^melta\\s+(.+)$");
    private static final Pattern ANTI_PATTERN = Pattern.compile("^anti[- ]([a-z-]+)\\s+(\\d)\\+$");

    public static WeaponKeywordsService parse(String rawText) {
        boolean assault = false;
        boolean pistol = false;
        boolean torrent = false;
        boolean ignoresCover = false;
        boolean lethalHits = false;
        boolean twinLinked = false;
        boolean lance = false;
        boolean heavy = false;
        boolean blast = false;
        boolean devastatingWounds = false;
        boolean hazardous = false;
        boolean precision = false;
        boolean extraAttacks = false;
        boolean oneShot = false;
        boolean psychic = false;

        String rapidFireBonus = "";
        String sustainedHitsBonus = "";
        String meltaBonus = "";

        Integer antiInfantry = null;
        Integer antiVehicle = null;
        Integer antiMonster = null;
        Integer antiCharacter = null;
        Integer antiPsyker = null;

        if (rawText != null && !rawText.isBlank()) {
            String[] tokens = rawText.toLowerCase(Locale.ROOT).split(",");

            for (String tokenRaw : tokens) {
                String token = tokenRaw.trim();
                if (token.isBlank()) {
                    continue;
                }

                switch (token) {
                    case "assault" -> assault = true;
                    case "pistol" -> pistol = true;
                    case "torrent" -> torrent = true;
                    case "ignores cover" -> ignoresCover = true;
                    case "lethal hits" -> lethalHits = true;
                    case "twin-linked" -> twinLinked = true;
                    case "lance" -> lance = true;
                    case "heavy" -> heavy = true;
                    case "blast" -> blast = true;
                    case "devastating wounds" -> devastatingWounds = true;
                    case "hazardous" -> hazardous = true;
                    case "precision" -> precision = true;
                    case "extra attacks" -> extraAttacks = true;
                    case "one shot" -> oneShot = true;
                    case "psychic" -> psychic = true;
                    default -> {
                        Matcher rapidFireMatcher = RAPID_FIRE_PATTERN.matcher(token);
                        if (rapidFireMatcher.matches()) {
                            rapidFireBonus = normalizeValue(rapidFireMatcher.group(1));
                            continue;
                        }

                        Matcher sustainedMatcher = SUSTAINED_HITS_PATTERN.matcher(token);
                        if (sustainedMatcher.matches()) {
                            sustainedHitsBonus = normalizeValue(sustainedMatcher.group(1));
                            continue;
                        }

                        Matcher meltaMatcher = MELTA_PATTERN.matcher(token);
                        if (meltaMatcher.matches()) {
                            meltaBonus = normalizeValue(meltaMatcher.group(1));
                            continue;
                        }

                        Matcher antiMatcher = ANTI_PATTERN.matcher(token);
                        if (antiMatcher.matches()) {
                            int threshold = Integer.parseInt(antiMatcher.group(2));
                            String antiType = antiMatcher.group(1)
                                    .replace("-", "")
                                    .replace(" ", "");

                            switch (antiType) {
                                case "infantry" -> antiInfantry = threshold;
                                case "vehicle" -> antiVehicle = threshold;
                                case "monster" -> antiMonster = threshold;
                                case "character" -> antiCharacter = threshold;
                                case "psyker" -> antiPsyker = threshold;
                                default -> {
                                }
                            }
                        }
                    }
                }
            }
        }

        return new WeaponKeywordsService(
                assault,
                pistol,
                torrent,
                ignoresCover,
                lethalHits,
                twinLinked,
                lance,
                heavy,
                blast,
                devastatingWounds,
                hazardous,
                precision,
                extraAttacks,
                oneShot,
                psychic,
                rapidFireBonus,
                sustainedHitsBonus,
                meltaBonus,
                antiInfantry,
                antiVehicle,
                antiMonster,
                antiCharacter,
                antiPsyker
        );
    }

    public boolean antiTriggers(AttackKeywordContext context, int unmodifiedWoundRoll) {
        if (context == null) {
            return false;
        }
        if (antiInfantry != null && context.targetIsInfantry() && unmodifiedWoundRoll >= antiInfantry) {
            return true;
        }
        if (antiVehicle != null && context.targetIsVehicle() && unmodifiedWoundRoll >= antiVehicle) {
            return true;
        }
        if (antiMonster != null && context.targetIsMonster() && unmodifiedWoundRoll >= antiMonster) {
            return true;
        }
        if (antiCharacter != null && context.targetIsCharacter() && unmodifiedWoundRoll >= antiCharacter) {
            return true;
        }
        if (antiPsyker != null && context.targetIsPsyker() && unmodifiedWoundRoll >= antiPsyker) {
            return true;
        }
        return false;
    }

    private static String normalizeValue(String value) {
        return value == null
                ? ""
                : value.trim().toUpperCase(Locale.ROOT).replace(" ", "");
    }
}
