package eecs2311.group2.wh40k_easycombat.model.instance;

import eecs2311.group2.wh40k_easycombat.service.calculations.UnitStrengthCalculations;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class UnitInstance {
    private final String instanceId;
    private final String datasheetId;
    private final String unitName;

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
    private final List<String> removedWeaponKeysForDestroyedModels = new ArrayList<>();

    private final Set<String> usedRangedWeaponKeysThisPhase = new LinkedHashSet<>();
    private final Set<String> usedOneShotWeaponKeysThisBattle = new LinkedHashSet<>();

    public UnitInstance(String datasheetId, String unitName) {
        this.instanceId = UUID.randomUUID().toString();
        this.datasheetId = datasheetId == null ? "" : datasheetId;
        this.unitName = unitName == null ? "" : unitName;
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

    public Set<String> getUsedRangedWeaponKeysThisPhase() {
        return Set.copyOf(usedRangedWeaponKeysThisPhase);
    }

    public Set<String> getUsedOneShotWeaponKeysThisBattle() {
        return Set.copyOf(usedOneShotWeaponKeysThisBattle);
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
        usedRangedWeaponKeysThisPhase.clear();
    }

    public void resetForNewShootingPhase() {
        usedRangedWeaponKeysThisPhase.clear();
    }

    public void resetForNewFightPhase() {
        eligibleToFightThisPhase = false;
        foughtThisPhase = false;
    }

    public boolean hasUsedRangedWeaponThisPhase(WeaponProfile weapon) {
        return weapon != null && usedRangedWeaponKeysThisPhase.contains(buildWeaponKey(weapon));
    }

    public void markRangedWeaponUsedThisPhase(WeaponProfile weapon) {
        if (weapon != null) {
            usedRangedWeaponKeysThisPhase.add(buildWeaponKey(weapon));
        }
    }

    public void markRangedWeaponUsedThisPhaseByKey(String weaponKey) {
        if (weaponKey != null && !weaponKey.isBlank()) {
            usedRangedWeaponKeysThisPhase.add(weaponKey);
        }
    }

    public boolean hasUsedOneShotWeaponThisBattle(WeaponProfile weapon) {
        return weapon != null && usedOneShotWeaponKeysThisBattle.contains(buildWeaponKey(weapon));
    }

    public void markOneShotWeaponUsed(WeaponProfile weapon) {
        if (weapon != null) {
            usedOneShotWeaponKeysThisBattle.add(buildWeaponKey(weapon));
        }
    }

    public void markOneShotWeaponUsedByKey(String weaponKey) {
        if (weaponKey != null && !weaponKey.isBlank()) {
            usedOneShotWeaponKeysThisBattle.add(weaponKey);
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
