package eecs2311.group2.wh40k_easycombat.model.instance;

import eecs2311.group2.wh40k_easycombat.service.calculations.UnitStrengthCalculations;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UnitInstance {
    private final String instanceId;
    private final String datasheetId;
    private final String unitName;
    private String factionId;
    private String factionName;
    private String detachmentId;
    private String detachmentName;
    private String enhancementId;
    private String enhancementName;

    private boolean battleShocked;
    private boolean eligibleToFightThisPhase;
    private boolean foughtThisPhase;
    private boolean chargedThisTurn;
    private boolean wasChargedThisTurn;

    private final List<UnitModelInstance> models = new ArrayList<>();
    private final List<WeaponProfile> rangedWeapons = new ArrayList<>();
    private final List<WeaponProfile> meleeWeapons = new ArrayList<>();
    private final List<String> keywords = new ArrayList<>();
    private final List<UnitAbilityProfile> abilities = new ArrayList<>();
    private final List<String> factionAbilityNames = new ArrayList<>();
    private final List<String> detachmentAbilityNames = new ArrayList<>();
    private final List<String> removedWeaponKeysForDestroyedModels = new ArrayList<>();

    private final Map<String, Integer> usedRangedWeaponCountsThisPhase = new LinkedHashMap<>();
    private final Map<String, Integer> usedOneShotWeaponCountsThisBattle = new LinkedHashMap<>();

    public UnitInstance(String datasheetId, String unitName) {
        this.instanceId = UUID.randomUUID().toString();
        this.datasheetId = datasheetId == null ? "" : datasheetId;
        this.unitName = unitName == null ? "" : unitName;
        this.factionId = "";
        this.factionName = "";
        this.detachmentId = "";
        this.detachmentName = "";
        this.enhancementId = "";
        this.enhancementName = "";
        this.battleShocked = false;
        this.eligibleToFightThisPhase = false;
        this.foughtThisPhase = false;
        this.chargedThisTurn = false;
        this.wasChargedThisTurn = false;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getDatasheetId() {
        return datasheetId;
    }

    public String getUnitName() {
        return unitName;
    }

    public String getFactionId() {
        return factionId;
    }

    public void setFactionId(String factionId) {
        this.factionId = factionId == null ? "" : factionId.trim();
    }

    public String getFactionName() {
        return factionName;
    }

    public void setFactionName(String factionName) {
        this.factionName = factionName == null ? "" : factionName.trim();
    }

    public String getDetachmentId() {
        return detachmentId;
    }

    public void setDetachmentId(String detachmentId) {
        this.detachmentId = detachmentId == null ? "" : detachmentId.trim();
    }

    public String getDetachmentName() {
        return detachmentName;
    }

    public void setDetachmentName(String detachmentName) {
        this.detachmentName = detachmentName == null ? "" : detachmentName.trim();
    }

    public String getEnhancementId() {
        return enhancementId;
    }

    public void setEnhancementId(String enhancementId) {
        this.enhancementId = enhancementId == null ? "" : enhancementId.trim();
    }

    public String getEnhancementName() {
        return enhancementName;
    }

    public void setEnhancementName(String enhancementName) {
        this.enhancementName = enhancementName == null ? "" : enhancementName.trim();
    }

    public boolean isBattleShocked() {
        return battleShocked;
    }

    public void setBattleShocked(boolean battleShocked) {
        this.battleShocked = battleShocked;
    }

    public boolean isEligibleToFightThisPhase() {
        return eligibleToFightThisPhase;
    }

    public void setEligibleToFightThisPhase(boolean eligibleToFightThisPhase) {
        this.eligibleToFightThisPhase = eligibleToFightThisPhase;
    }

    public boolean hasFoughtThisPhase() {
        return foughtThisPhase;
    }

    public void setFoughtThisPhase(boolean foughtThisPhase) {
        this.foughtThisPhase = foughtThisPhase;
    }

    public boolean hasChargedThisTurn() {
        return chargedThisTurn;
    }

    public void setChargedThisTurn(boolean chargedThisTurn) {
        this.chargedThisTurn = chargedThisTurn;
    }

    public boolean hasBeenChargedThisTurn() {
        return wasChargedThisTurn;
    }

    public void setWasChargedThisTurn(boolean wasChargedThisTurn) {
        this.wasChargedThisTurn = wasChargedThisTurn;
    }

    public List<UnitModelInstance> getModels() {
        return models;
    }

    public List<WeaponProfile> getRangedWeapons() {
        return rangedWeapons;
    }

    public List<WeaponProfile> getMeleeWeapons() {
        return meleeWeapons;
    }

    public List<String> getKeywords() {
        return List.copyOf(keywords);
    }

    public List<UnitAbilityProfile> getAbilities() {
        return List.copyOf(abilities);
    }

    public List<String> getFactionAbilityNames() {
        return List.copyOf(factionAbilityNames);
    }

    public List<String> getDetachmentAbilityNames() {
        return List.copyOf(detachmentAbilityNames);
    }

    public Set<String> getUsedRangedWeaponKeysThisPhase() {
        return Set.copyOf(usedRangedWeaponCountsThisPhase.keySet());
    }

    public Map<String, Integer> getUsedRangedWeaponCountsThisPhase() {
        return Map.copyOf(usedRangedWeaponCountsThisPhase);
    }

    public Set<String> getUsedOneShotWeaponKeysThisBattle() {
        return Set.copyOf(usedOneShotWeaponCountsThisBattle.keySet());
    }

    public Map<String, Integer> getUsedOneShotWeaponCountsThisBattle() {
        return Map.copyOf(usedOneShotWeaponCountsThisBattle);
    }

    public List<String> getRemovedWeaponKeysForDestroyedModels() {
        return List.copyOf(removedWeaponKeysForDestroyedModels);
    }

    public WeaponProfile getWeaponProfileByName(String name) {
        for (WeaponProfile profile : rangedWeapons) {
            if (profile.name().equals(name)) {
                return profile;
            }
        }
        for (WeaponProfile profile : meleeWeapons) {
            if (profile.name().equals(name)) {
                return profile;
            }
        }
        return null;
    }

    public void addModel(UnitModelInstance model) {
        if (model != null) {
            models.add(model);
        }
    }

    public void addRangedWeapon(WeaponProfile weapon) {
        if (weapon != null) {
            rangedWeapons.add(weapon);
        }
    }

    public void addMeleeWeapon(WeaponProfile weapon) {
        if (weapon != null) {
            meleeWeapons.add(weapon);
        }
    }

    public void addKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }

        if (!hasKeyword(keyword)) {
            keywords.add(keyword.trim());
        }
    }

    public void addAbility(UnitAbilityProfile ability) {
        if (ability != null) {
            abilities.add(ability);
        }
    }

    public void addFactionAbilityName(String abilityName) {
        if (abilityName == null || abilityName.isBlank()) {
            return;
        }

        if (!containsNormalizedValue(factionAbilityNames, abilityName)) {
            factionAbilityNames.add(abilityName.trim());
        }
    }

    public void addDetachmentAbilityName(String abilityName) {
        if (abilityName == null || abilityName.isBlank()) {
            return;
        }

        if (!containsNormalizedValue(detachmentAbilityNames, abilityName)) {
            detachmentAbilityNames.add(abilityName.trim());
        }
    }

    public boolean hasKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return false;
        }

        String expected = normalize(keyword);
        for (String existing : keywords) {
            if (normalize(existing).equals(expected)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAbilityNamed(String abilityName) {
        if (abilityName == null || abilityName.isBlank()) {
            return false;
        }

        String expected = normalize(abilityName);
        for (UnitAbilityProfile ability : abilities) {
            if (normalize(ability.name()).equals(expected)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAbilityTextContaining(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String expected = normalize(text);
        for (UnitAbilityProfile ability : abilities) {
            if (normalize(ability.description()).contains(expected)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAbilityNameContaining(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String expected = normalize(text);
        for (UnitAbilityProfile ability : abilities) {
            if (normalize(ability.name()).contains(expected)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFactionAbilityNameContaining(String text) {
        return containsListValue(factionAbilityNames, text);
    }

    public boolean hasDetachmentAbilityNameContaining(String text) {
        return containsListValue(detachmentAbilityNames, text);
    }

    public boolean hasEnhancementNameContaining(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        return normalize(enhancementName).contains(normalize(text));
    }

    public boolean hasFightsFirst() {
        return hasKeyword("Fights First")
                || hasAbilityNamed("Fights First")
                || hasAbilityTextContaining("fights first ability");
    }

    public int getModelCount() {
        return models.size();
    }

    public int getAliveModelCount() {
        return (int) models.stream()
                .filter(model -> !model.isDestroyed())
                .count();
    }

    public int getTotalCurrentHp() {
        return models.stream()
                .filter(model -> !model.isDestroyed())
                .mapToInt(UnitModelInstance::getCurrentHp)
                .sum();
    }

    public int getStartingOc() {
        return models.stream()
                .mapToInt(UnitModelInstance::getBaseOc)
                .sum();
    }

    public int getCurrentOc() {
        if (battleShocked) {
            return 0;
        }

        return models.stream()
                .filter(model -> !model.isDestroyed())
                .mapToInt(UnitModelInstance::getBaseOc)
                .sum();
    }

    public int getBestLeadership() {
        int best = Integer.MAX_VALUE;

        for (UnitModelInstance model : models) {
            if (model == null || model.isDestroyed()) {
                continue;
            }

            int leadership = parseCharacteristicSafe(model.getLd());
            if (leadership > 0) {
                best = Math.min(best, leadership);
            }
        }

        if (best != Integer.MAX_VALUE) {
            return best;
        }

        for (UnitModelInstance model : models) {
            if (model == null) {
                continue;
            }

            int leadership = parseCharacteristicSafe(model.getLd());
            if (leadership > 0) {
                best = Math.min(best, leadership);
            }
        }

        return best == Integer.MAX_VALUE ? 7 : best;
    }

    public boolean isDestroyed() {
        return !models.isEmpty()
                && models.stream().allMatch(UnitModelInstance::isDestroyed);
    }

    public boolean isBelowHalfStrength() {
        int total = getModelCount();
        if (total <= 0) {
            return false;
        }

        if (total == 1) {
            UnitModelInstance model = models.get(0);
            return UnitStrengthCalculations.isBelowHalfStrengthSingleModel(
                    model.getMaxHp(),
                    model.getCurrentHp()
            );
        }

        return UnitStrengthCalculations.isBelowHalfStrength(total, getAliveModelCount());
    }

    public void removeDestroyedModels() {
        // Destroyed models stay in the list so the UI can strike them through,
        // keep HP editable for healing or revival effects, and preserve the
        // unit's original starting strength for later rule checks.
    }

    public void resetForNewTurn() {
        chargedThisTurn = false;
        wasChargedThisTurn = false;
        eligibleToFightThisPhase = false;
        foughtThisPhase = false;
        usedRangedWeaponCountsThisPhase.clear();
    }

    public void resetForNewShootingPhase() {
        usedRangedWeaponCountsThisPhase.clear();
    }

    public void resetForNewFightPhase() {
        eligibleToFightThisPhase = false;
        foughtThisPhase = false;
    }

    public boolean hasUsedRangedWeaponThisPhase(WeaponProfile weapon) {
        return getRemainingRangedWeaponCountThisPhase(weapon) <= 0;
    }

    public int getUsedRangedWeaponCountThisPhase(WeaponProfile weapon) {
        if (weapon == null) {
            return 0;
        }
        return usedRangedWeaponCountsThisPhase.getOrDefault(buildWeaponKey(weapon), 0);
    }

    public int getRemainingRangedWeaponCountThisPhase(WeaponProfile weapon) {
        if (weapon == null) {
            return 0;
        }
        return Math.max(0, weapon.count() - getUsedRangedWeaponCountThisPhase(weapon));
    }

    public void markRangedWeaponUsedThisPhase(WeaponProfile weapon) {
        markRangedWeaponUsedThisPhase(weapon, 1);
    }

    public void markRangedWeaponUsedThisPhase(WeaponProfile weapon, int count) {
        if (weapon != null) {
            incrementUsage(usedRangedWeaponCountsThisPhase, buildWeaponKey(weapon), count, weapon.count());
        }
    }

    public void markRangedWeaponUsedThisPhaseByKey(String weaponKey) {
        markRangedWeaponUsedThisPhaseByKey(weaponKey, 1);
    }

    public void markRangedWeaponUsedThisPhaseByKey(String weaponKey, int count) {
        if (weaponKey != null && !weaponKey.isBlank()) {
            incrementUsage(usedRangedWeaponCountsThisPhase, weaponKey, count, Integer.MAX_VALUE);
        }
    }

    public boolean hasUsedOneShotWeaponThisBattle(WeaponProfile weapon) {
        return getRemainingOneShotWeaponCountThisBattle(weapon) <= 0;
    }

    public int getUsedOneShotWeaponCountThisBattle(WeaponProfile weapon) {
        if (weapon == null) {
            return 0;
        }
        return usedOneShotWeaponCountsThisBattle.getOrDefault(buildWeaponKey(weapon), 0);
    }

    public int getRemainingOneShotWeaponCountThisBattle(WeaponProfile weapon) {
        if (weapon == null) {
            return 0;
        }
        return Math.max(0, weapon.count() - getUsedOneShotWeaponCountThisBattle(weapon));
    }

    public void markOneShotWeaponUsed(WeaponProfile weapon) {
        markOneShotWeaponUsed(weapon, 1);
    }

    public void markOneShotWeaponUsed(WeaponProfile weapon, int count) {
        if (weapon != null) {
            incrementUsage(usedOneShotWeaponCountsThisBattle, buildWeaponKey(weapon), count, weapon.count());
        }
    }

    public void markOneShotWeaponUsedByKey(String weaponKey) {
        markOneShotWeaponUsedByKey(weaponKey, 1);
    }

    public void markOneShotWeaponUsedByKey(String weaponKey, int count) {
        if (weaponKey != null && !weaponKey.isBlank()) {
            incrementUsage(usedOneShotWeaponCountsThisBattle, weaponKey, count, Integer.MAX_VALUE);
        }
    }

    public static String buildWeaponKey(WeaponProfile weapon) {
        if (weapon == null) {
            return "";
        }
        return weapon.weaponID() + "::" + weapon.name();
    }

    public String decrementRepeatedWeaponForCasualty() {
        String ranged = decrementPreferredWeaponList(rangedWeapons);
        if (!ranged.isBlank()) {
            return ranged;
        }

        return decrementPreferredWeaponList(meleeWeapons);
    }

    private String decrementPreferredWeaponList(List<WeaponProfile> weapons) {
        int bestIndex = -1;
        int bestCount = 1;

        for (int i = 0; i < weapons.size(); i++) {
            WeaponProfile weapon = weapons.get(i);
            if (weapon == null || weapon.count() <= 1) {
                continue;
            }

            if (weapon.count() > bestCount) {
                bestCount = weapon.count();
                bestIndex = i;
            }
        }

        if (bestIndex < 0) {
            return "";
        }

        WeaponProfile existing = weapons.get(bestIndex);
        WeaponProfile updated = existing.withCount(existing.count() - 1);
        weapons.set(bestIndex, updated);
        removedWeaponKeysForDestroyedModels.add(buildWeaponKey(existing));
        return existing.name();
    }

    public String restoreWeaponForRevivedModel() {
        if (removedWeaponKeysForDestroyedModels.isEmpty()) {
            return "";
        }

        String weaponKey = removedWeaponKeysForDestroyedModels.remove(removedWeaponKeysForDestroyedModels.size() - 1);
        return incrementWeaponByKey(weaponKey);
    }

    public void markRemovedWeaponKey(String weaponKey) {
        if (weaponKey != null && !weaponKey.isBlank()) {
            removedWeaponKeysForDestroyedModels.add(weaponKey);
        }
    }

    private String incrementWeaponByKey(String weaponKey) {
        String restored = incrementWeaponByKey(rangedWeapons, weaponKey);
        if (!restored.isBlank()) {
            return restored;
        }

        return incrementWeaponByKey(meleeWeapons, weaponKey);
    }

    private String incrementWeaponByKey(List<WeaponProfile> weapons, String weaponKey) {
        for (int i = 0; i < weapons.size(); i++) {
            WeaponProfile weapon = weapons.get(i);
            if (weapon == null) {
                continue;
            }
            if (!buildWeaponKey(weapon).equals(weaponKey)) {
                continue;
            }

            weapons.set(i, weapon.withCount(weapon.count() + 1));
            return weapon.name();
        }

        return "";
    }

    private static String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean containsNormalizedValue(List<String> values, String expected) {
        String normalizedExpected = normalize(expected);
        for (String value : values) {
            if (normalize(value).equals(normalizedExpected)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsListValue(List<String> values, String expected) {
        if (expected == null || expected.isBlank()) {
            return false;
        }

        String normalizedExpected = normalize(expected);
        for (String value : values) {
            if (normalize(value).contains(normalizedExpected)) {
                return true;
            }
        }
        return false;
    }

    private static void incrementUsage(Map<String, Integer> usageMap, String weaponKey, int count, int maxAllowed) {
        if (usageMap == null || weaponKey == null || weaponKey.isBlank() || count <= 0) {
            return;
        }

        int current = usageMap.getOrDefault(weaponKey, 0);
        int next = current + count;
        if (maxAllowed > 0 && maxAllowed != Integer.MAX_VALUE) {
            next = Math.min(next, maxAllowed);
        }
        usageMap.put(weaponKey, next);
    }

    private static int parseCharacteristicSafe(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        String cleaned = text.replaceAll("[^0-9-]", "").trim();
        if (cleaned.isBlank()) {
            return 0;
        }

        try {
            return Integer.parseInt(cleaned);
        } catch (Exception e) {
            return 0;
        }
    }
}
