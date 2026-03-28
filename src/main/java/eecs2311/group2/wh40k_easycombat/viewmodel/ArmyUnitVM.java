package eecs2311.group2.wh40k_easycombat.viewmodel;

import eecs2311.group2.wh40k_easycombat.util.CostParser;
import eecs2311.group2.wh40k_easycombat.util.CostTier;

import javafx.beans.property.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArmyUnitVM {

    public static class WargearEntry {
        private final int autoId;
        private final String name;
        private final IntegerProperty count = new SimpleIntegerProperty(0);

        public WargearEntry(int autoId, String name) {
            this.autoId = autoId;
            this.name = (name == null || name.isBlank()) ? "Wargear" : name;
        }

        public int getAutoId() {
            return autoId;
        }

        public String getName() {
            return name;
        }

        public IntegerProperty countProperty() {
            return count;
        }

        public int getCount() {
            return count.get();
        }

        public void setCount(int value) {
            count.set(Math.max(0, value));
        }

        public void inc() {
            count.set(count.get() + 1);
        }

        public void dec() {
            if (count.get() > 0) {
                count.set(count.get() - 1);
            }
        }
    }

    public static class EnhancementEntry {
        private final String id;
        private final String name;
        private final int cost;
        private final String detachmentId;
        private final String factionId;

        public EnhancementEntry(String id, String name, int cost) {
            this(id, name, cost, "", "");
        }

        public EnhancementEntry(String id, String name, int cost, String detachmentId, String factionId) {
            this.id = id == null ? "" : id;
            this.name = (name == null || name.isBlank()) ? this.id : name;
            this.cost = cost;
            this.detachmentId = detachmentId == null ? "" : detachmentId;
            this.factionId = factionId == null ? "" : factionId;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getCost() {
            return cost;
        }

        public String getDetachmentId() {
            return detachmentId;
        }

        public String getFactionId() {
            return factionId;
        }

        @Override
        public String toString() {
            if (id.isBlank()) return "No Enhancement";
            return cost > 0 ? name + " (+" + cost + " pts)" : name;
        }
    }

    private final String datasheetId;
    private final String unitName;
    private final String statLine;
    private final String role;
    private final boolean character;

    private final IntegerProperty modelCount = new SimpleIntegerProperty();
    private final IntegerProperty points = new SimpleIntegerProperty();

    private final BooleanProperty expanded = new SimpleBooleanProperty(false);
    private final BooleanProperty warlord = new SimpleBooleanProperty(false);

    private final StringProperty enhancementId = new SimpleStringProperty("");
    private final IntegerProperty enhancementCost = new SimpleIntegerProperty(0);

    private final List<CostTier> tiers;
    private final List<WargearEntry> wargears = new ArrayList<>();
    private final List<EnhancementEntry> enhancements = new ArrayList<>();

    private final int minModels;
    private final int maxModels;

    public ArmyUnitVM(String datasheetId,
                      String unitName,
                      String statLine,
                      String role,
                      boolean character,
                      List<CostTier> tiers) {

        this.datasheetId = datasheetId;
        this.unitName = unitName;
        this.statLine = statLine;
        this.role = (role == null || role.isBlank()) ? "Other" : role;
        this.character = character;
        this.tiers = tiers == null ? List.of() : tiers;

        if (this.tiers.isEmpty()) {
            minModels = 1;
            maxModels = 1;
        } else {
            minModels = this.tiers.get(0).models();
            maxModels = this.tiers.get(this.tiers.size() - 1).models();
        }

        modelCount.set(minModels);
        recalculatePoints();
    }

    public void recalculatePoints() {
        points.set(CostParser.pointsForModels(modelCount.get(), tiers) + enhancementCost.get());
    }

    public void incModels() {
        if (modelCount.get() < maxModels) {
            modelCount.set(modelCount.get() + 1);
            clampWargearCounts();
            recalculatePoints();
        }
    }

    public void decModels() {
        if (modelCount.get() > minModels) {
            modelCount.set(modelCount.get() - 1);
            clampWargearCounts();
            recalculatePoints();
        }
    }

    public void setModelCount(int count) {
        int clamped = Math.max(minModels, Math.min(maxModels, count));
        modelCount.set(clamped);
        clampWargearCounts();
        recalculatePoints();
    }

    public void addWargear(int autoId, String wargearName) {
        wargears.add(new WargearEntry(autoId, wargearName));
    }

    public void setWargearCount(int wargearId, int count) {
        for (WargearEntry wg : wargears) {
            if (wg.getAutoId() == wargearId) {
                wg.setCount(Math.max(0, count));
                return;
            }
        }
    }

    public void addEnhancement(String id, String name, int cost) {
        enhancements.add(new EnhancementEntry(id, name, cost));
    }

    public void addEnhancement(String id, String name, int cost, String detachmentId, String factionId) {
        enhancements.add(new EnhancementEntry(id, name, cost, detachmentId, factionId));
    }

    public void setEnhancement(EnhancementEntry entry) {
        if (entry == null || entry.getId().isBlank()) {
            enhancementId.set("");
            enhancementCost.set(0);
        } else {
            enhancementId.set(entry.getId());
            enhancementCost.set(entry.getCost());
        }
        recalculatePoints();
    }

    public EnhancementEntry getSelectedEnhancement() {
        for (EnhancementEntry e : enhancements) {
            if (Objects.equals(e.getId(), enhancementId.get())) {
                return e;
            }
        }
        return new EnhancementEntry("", "No Enhancement", 0);
    }

    private void clampWargearCounts() {
        for (WargearEntry wg : wargears) {
            if (wg.getCount() > modelCount.get()) {
                wg.setCount(modelCount.get());
            }
        }
    }

    public String getDatasheetId() {
        return datasheetId;
    }

    public String getUnitName() {
        return unitName;
    }

    public String getStatLine() {
        return statLine;
    }

    public String getRole() {
        return role;
    }

    public boolean isCharacter() {
        return character;
    }

    public IntegerProperty modelCountProperty() {
        return modelCount;
    }

    public IntegerProperty pointsProperty() {
        return points;
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }

    public BooleanProperty warlordProperty() {
        return warlord;
    }

    public StringProperty enhancementIdProperty() {
        return enhancementId;
    }

    public String getEnhancementId() {
        return enhancementId.get();
    }

    public int getEnhancementCost() {
        return enhancementCost.get();
    }

    public List<WargearEntry> getWargears() {
        return wargears;
    }

    public List<EnhancementEntry> getEnhancements() {
        return enhancements;
    }

    public int getMinModels() {
        return minModels;
    }

    public int getMaxModels() {
        return maxModels;
    }
}
