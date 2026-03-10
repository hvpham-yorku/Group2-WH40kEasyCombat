package eecs2311.group2.wh40k_easycombat.assembler;

import eecs2311.group2.wh40k_easycombat.aggregate.ArmyAggregate;
import eecs2311.group2.wh40k_easycombat.aggregate.DatasheetAggregate;
import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.model.Army_units;
import eecs2311.group2.wh40k_easycombat.repository.FactionLookupRepository;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameSubUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameWeaponVM;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static eecs2311.group2.wh40k_easycombat.util.FxReflectionHelper.getAny;
import static eecs2311.group2.wh40k_easycombat.util.FxReflectionHelper.s;

public final class SavedArmyGameAssembler {

    private SavedArmyGameAssembler() {
    }

    public static List<SavedArmyOption> loadSavedArmies() throws Exception {
        List<SavedArmyOption> out = new ArrayList<>();

        for (Army a : StaticDataService.getAllArmies()) {
            out.add(new SavedArmyOption(
                    a.auto_id(),
                    a.name(),
                    a.faction_id(),
                    a.total_points(),
                    a.isMarked()
            ));
        }

        out.sort((a, b) -> {
            if (a.marked() != b.marked()) {
                return Boolean.compare(b.marked(), a.marked());
            }
            return a.name().compareToIgnoreCase(b.name());
        });

        return out;
    }

    public static ImportedArmyData importArmy(int armyId) throws Exception {
    	ArmyAggregate bundle = StaticDataService.getArmyBundle(armyId);
        if (bundle == null || bundle.army == null) {
            return null;
        }

        List<GameArmyUnitVM> importedUnits = new ArrayList<>();

        for (Army_units savedUnit : bundle.units) {
        	DatasheetAggregate datasheetBundle =
                    StaticDataService.getDatasheetBundle(savedUnit.datasheet_id());

            if (datasheetBundle == null || datasheetBundle.datasheet == null) {
                continue;
            }

            String unitName = s(getAny(datasheetBundle.datasheet, "name", "datasheet_name", "title"));
            if (unitName.isBlank()) {
                unitName = savedUnit.datasheet_id();
            }

            GameArmyUnitVM vm = new GameArmyUnitVM(unitName);

            buildSubUnits(vm, datasheetBundle, savedUnit.model_count());
            buildWeapons(vm, datasheetBundle);

            importedUnits.add(vm);
        }

        String factionId = bundle.army.faction_id();
        String factionName = resolveFactionName(factionId);

        return new ImportedArmyData(
                bundle.army.auto_id(),
                bundle.army.name(),
                factionId,
                factionName,
                bundle.army.total_points(),
                importedUnits
        );
    }

    private static void buildSubUnits(
            GameArmyUnitVM vm,
            DatasheetAggregate bundle,
            int totalModelCount
    ) {
        if (bundle.models == null || bundle.models.isEmpty()) {
            return;
        }

        List<Object> models = new ArrayList<>(bundle.models);
        Map<String, Integer> countsByName = parseCountsFromComposition(models, bundle.compositions, totalModelCount);

        if (countsByName.isEmpty()) {
            if (models.size() == 1) {
                String modelName = s(getAny(models.get(0), "name", "model", "unit_name"));
                countsByName.put(modelName, totalModelCount);
            } else {
                int remaining = totalModelCount;
                for (int i = 0; i < models.size(); i++) {
                    String modelName = s(getAny(models.get(i), "name", "model", "unit_name"));
                    int count = (i == models.size() - 1) ? remaining : Math.min(1, remaining);
                    countsByName.put(modelName, count);
                    remaining -= count;
                }
            }
        }

        for (Object model : models) {
            String modelName = s(getAny(model, "name", "model", "unit_name"));
            int count = countsByName.getOrDefault(modelName, 0);

            for (int i = 0; i < count; i++) {
                String w = s(getAny(model, "W", "w", "wounds"));
                vm.getSubUnits().add(new GameSubUnitVM(
                        modelName,
                        s(getAny(model, "M", "m", "move", "movement")),
                        s(getAny(model, "T", "t", "toughness")),
                        s(getAny(model, "Sv", "sv", "save")),
                        w,
                        s(getAny(model, "Ld", "ld", "leadership")),
                        s(getAny(model, "OC", "oc", "objective_control")),
                        s(getAny(model, "inv_sv", "inv", "invSave", "invulnerable_save")),
                        w
                ));
            }
        }
    }

    private static Map<String, Integer> parseCountsFromComposition(List<Object> models, List<?> compositions, int totalCount) {
        Map<String, Integer> result = new LinkedHashMap<>();

        if (compositions == null || compositions.isEmpty()) {
            return result;
        }

        for (Object model : models) {
            String modelName = s(getAny(model, "name", "model", "unit_name"));
            if (modelName.isBlank()) continue;

            for (Object composition : compositions) {
                String text = s(getAny(composition, "description", "text", "composition", "line_text"));
                if (text.isBlank()) continue;

                String lowerText = text.toLowerCase();
                String lowerName = modelName.toLowerCase();

                int index = lowerText.indexOf(lowerName);
                if (index > 0) {
                    int start = index - 1;
                    while (start >= 0 && Character.isWhitespace(lowerText.charAt(start))) start--;
                    int end = start;
                    while (start >= 0 && Character.isDigit(lowerText.charAt(start))) start--;
                    if (end >= 0 && start < end) {
                        try {
                            int count = Integer.parseInt(lowerText.substring(start + 1, end + 1));
                            result.put(modelName, count);
                            break;
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }

        int sum = result.values().stream().mapToInt(Integer::intValue).sum();
        if (sum < totalCount && models.size() == 1) {
            String modelName = s(getAny(models.get(0), "name", "model", "unit_name"));
            result.put(modelName, totalCount);
        }

        return result;
    }

    private static void buildWeapons(GameArmyUnitVM vm, DatasheetAggregate bundle) {
        if (bundle.wargear == null) {
            return;
        }

        for (Object w : bundle.wargear) {
            String name = s(getAny(w, "wargear", "weapon", "name"));
            String range = s(getAny(w, "range", "weapon_range"));
            String attacks = s(getAny(w, "a", "attacks", "A"));
            String bs = s(getAny(w, "bs", "BS", "BS_WS"));
            String ws = s(getAny(w, "ws", "WS", "BS_WS"));
            String strength = s(getAny(w, "s", "strength", "S"));
            String ap = s(getAny(w, "ap", "AP"));
            String dmg = s(getAny(w, "d", "damage", "D"));
            String type = s(getAny(w, "type", "category", "weapon_type", "profile_type")).toLowerCase();

            int count = parseWeaponCount(w);

            boolean isMelee = type.contains("melee")
                    || range.toLowerCase().contains("melee")
                    || (range.isBlank() && !ws.isBlank());

            GameWeaponVM row = new GameWeaponVM(
                    name,
                    count,
                    isMelee ? "Melee" : range,
                    attacks,
                    isMelee ? ws : bs,
                    strength,
                    ap,
                    dmg,
                    isMelee
            );

            if (row.isMelee()) {
                vm.getMeleeWeapons().add(row);
            } else {
                vm.getRangedWeapons().add(row);
            }
        }
    }

    private static int parseWeaponCount(Object w) {
        String[] fields = {"count", "quantity", "number", "amount"};
        for (String f : fields) {
            String value = s(getAny(w, f));
            if (!value.isBlank()) {
                try {
                    return Math.max(1, Integer.parseInt(value));
                } catch (Exception ignored) {
                }
            }
        }
        return 1;
    }
    
    private static String resolveFactionName(String factionId) {
        if (factionId == null || factionId.isBlank()) {
            return "";
        }

        try {
            String name = FactionLookupRepository.findNameById(factionId);
            return (name == null || name.isBlank()) ? factionId : name;
        } catch (Exception ignored) {
            return factionId;
        }
    }

    public record SavedArmyOption(int armyId, String name, String factionId, int points, boolean marked) {
    }

    public record ImportedArmyData(
            int armyId,
            String name,
            String factionId,
            String factionName,
            int points,
            List<GameArmyUnitVM> units
    ) {
    }
}