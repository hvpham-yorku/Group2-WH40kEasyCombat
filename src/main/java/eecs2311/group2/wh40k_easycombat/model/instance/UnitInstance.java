package eecs2311.group2.wh40k_easycombat.model.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UnitInstance {
    private final String instanceId;
    private final String datasheetId;
    private final String unitName;

    private boolean battleShocked;

    private final List<UnitModelInstance> models = new ArrayList<>();
    private final List<WeaponProfile> rangedWeapons = new ArrayList<>();
    private final List<WeaponProfile> meleeWeapons = new ArrayList<>();

    public UnitInstance(String datasheetId, String unitName) {
        this.instanceId = UUID.randomUUID().toString();
        this.datasheetId = datasheetId == null ? "" : datasheetId;
        this.unitName = unitName == null ? "" : unitName;
        this.battleShocked = false;
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

    public List<UnitModelInstance> getModels() {
        return models;
    }

    public List<WeaponProfile> getRangedWeapons() {
        return rangedWeapons;
    }

    public List<WeaponProfile> getMeleeWeapons() {
        return meleeWeapons;
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

    public boolean isDestroyed() {
        return !models.isEmpty()
                && models.stream().allMatch(UnitModelInstance::isDestroyed);
    }

    public boolean isBelowHalfStrength() {
        int total = getModelCount();
        return total > 0 && getAliveModelCount() * 2 <= total;
    }

    public void removeDestroyedModels() {
        models.removeIf(UnitModelInstance::isDestroyed);
    }
}
