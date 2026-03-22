package eecs2311.group2.wh40k_easycombat.service.game;

import eecs2311.group2.wh40k_easycombat.model.aggregate.ArmyAggregate;
import eecs2311.group2.wh40k_easycombat.model.aggregate.DatasheetAggregate;
import eecs2311.group2.wh40k_easycombat.model.Army_detachment;
import eecs2311.group2.wh40k_easycombat.model.Army_units;
import eecs2311.group2.wh40k_easycombat.model.Army_wargear;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_unit_composition;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_wargear;
import eecs2311.group2.wh40k_easycombat.model.Stratagems;
import eecs2311.group2.wh40k_easycombat.model.instance.StratagemInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_abilities;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_keywords;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitAbilityProfile;
import eecs2311.group2.wh40k_easycombat.repository.FactionLookupRepository;
import eecs2311.group2.wh40k_easycombat.repository.StratagemsRepository;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyImportVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameStrategyVM;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class GameArmyImportService {

    private GameArmyImportService() {
    }

    public static GameArmyImportVM importArmy(int armyId) throws Exception {
        ArmyAggregate bundle = StaticDataService.getArmyBundle(armyId);
        if (bundle == null || bundle.army == null) {
            return null;
        }

        String factionId = safe(bundle.army.faction_id());
        String factionName = resolveFactionName(factionId);

        return new GameArmyImportVM(
                bundle.army.auto_id(),
                safe(bundle.army.name()),
                factionId,
                factionName,
                bundle.army.total_points(),
                buildImportedUnits(bundle),
                loadStrategiesForArmy(armyId)
        );
    }

    private static List<GameArmyUnitVM> buildImportedUnits(ArmyAggregate bundle) throws Exception {
        List<GameArmyUnitVM> importedUnits = new ArrayList<>();

        for (Army_units savedUnit : bundle.units) {
            DatasheetAggregate datasheetBundle =
                    StaticDataService.getDatasheetBundle(savedUnit.datasheet_id());

            if (datasheetBundle == null || datasheetBundle.datasheet == null) {
                continue;
            }

            String unitName = safe(datasheetBundle.datasheet.name(), savedUnit.datasheet_id());
            UnitInstance unit = new UnitInstance(savedUnit.datasheet_id(), unitName);
            importUnitRules(unit, datasheetBundle);
            List<Army_wargear> equippedWeapons = StaticDataService.getArmyWargearByUnitId(savedUnit.auto_id());

            buildSubUnits(unit, datasheetBundle, savedUnit.model_count());
            buildWeapons(unit, datasheetBundle, equippedWeapons);

            importedUnits.add(new GameArmyUnitVM(unit));
        }

        return importedUnits;
    }

    private static List<GameStrategyVM> loadStrategiesForArmy(int armyId) throws Exception {
        List<GameStrategyVM> detachmentStrategies = new ArrayList<>();
        List<GameStrategyVM> coreStrategies = new ArrayList<>();

        String detachmentId = loadPrimaryDetachmentId(armyId);
        List<Stratagems> all = StratagemsRepository.getAllStratagems();

        for (Stratagems stratagem : all) {
            if (stratagem == null) continue;

            String stratDetachment = safe(stratagem.detachment_id());
            String stratFaction = safe(stratagem.faction_id());
            String stratType = safe(stratagem.type()).toLowerCase(Locale.ROOT);

            GameStrategyVM vm = new GameStrategyVM(new StratagemInstance(
                    safe(stratagem.name()),
                    safe(stratagem.cp_cost()),
                    safe(stratagem.turn()),
                    safe(stratagem.phase()),
                    safe(stratagem.description())
            ));

            if (!stratDetachment.isBlank()) {
                if (detachmentId != null && detachmentId.equalsIgnoreCase(stratDetachment)) {
                    detachmentStrategies.add(vm);
                }
                continue;
            }

            if (stratFaction.isBlank() && stratType.contains("core")) {
                coreStrategies.add(vm);
            }
        }

        List<GameStrategyVM> result = new ArrayList<>();
        result.addAll(detachmentStrategies);
        result.addAll(coreStrategies);
        return result;
    }

    private static void buildSubUnits(
            UnitInstance unit,
            DatasheetAggregate bundle,
            int totalModelCount
    ) {
        if (bundle.models == null || bundle.models.isEmpty()) {
            return;
        }

        Map<String, Integer> countsByName = parseCountsFromComposition(
                bundle.models,
                bundle.compositions,
                totalModelCount
        );

        if (countsByName.isEmpty()) {
            if (bundle.models.size() == 1) {
                String modelName = resolveModelName(bundle.models.get(0));
                countsByName.put(modelName, totalModelCount);
            } else {
                int remaining = totalModelCount;
                for (int i = 0; i < bundle.models.size(); i++) {
                    String modelName = resolveModelName(bundle.models.get(i));
                    int count = (i == bundle.models.size() - 1) ? remaining : Math.min(1, remaining);
                    countsByName.put(modelName, count);
                    remaining -= count;
                }
            }
        }

        for (Datasheets_models model : bundle.models) {
            String modelName = resolveModelName(model);
            int count = countsByName.getOrDefault(modelName, 0);

            for (int i = 0; i < count; i++) {
                unit.addModel(new UnitModelInstance(
                        modelName,
                        safe(model.M()),
                        safe(model.T()),
                        safe(model.Sv()),
                        safe(model.W()),
                        safe(model.Ld()),
                        safe(model.OC()),
                        safe(model.inv_sv())
                ));
            }
        }
    }

    private static Map<String, Integer> parseCountsFromComposition(
            List<Datasheets_models> models,
            List<Datasheets_unit_composition> compositions,
            int totalCount
    ) {
        Map<String, Integer> result = new LinkedHashMap<>();

        if (compositions == null || compositions.isEmpty()) {
            return result;
        }

        for (Datasheets_models model : models) {
            String modelName = resolveModelName(model);
            if (modelName.isBlank()) continue;

            for (Datasheets_unit_composition composition : compositions) {
                String text = safe(composition.description(), composition.line());
                if (text.isBlank()) continue;

                String lowerText = text.toLowerCase(Locale.ROOT);
                String lowerName = modelName.toLowerCase(Locale.ROOT);

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
            String modelName = resolveModelName(models.get(0));
            result.put(modelName, totalCount);
        }

        return result;
    }

    private static void buildWeapons(
            UnitInstance unit,
            DatasheetAggregate bundle,
            List<Army_wargear> equippedWeapons
    ) {
        if (bundle.wargear == null) {
            return;
        }

        if (equippedWeapons == null || equippedWeapons.isEmpty()) {
            for (Datasheets_wargear wargear : bundle.wargear) {
                WeaponProfile weapon = WeaponProfile.fromDatasheetWargear(wargear);
                addWeapon(unit, weapon);
            }
            return;
        }

        Map<Integer, Integer> weaponCountByID = new LinkedHashMap<>();
        for (Army_wargear equipped : equippedWeapons) {
            weaponCountByID.merge(
                    equipped.wargear_id(),
                    Math.max(1, equipped.wargear_count()),
                    Integer::sum
            );
        }

        Map<Integer, Datasheets_wargear> datasheetWargearByID = new LinkedHashMap<>();
        for (Datasheets_wargear wargear : bundle.wargear) {
            datasheetWargearByID.put(wargear.auto_id(), wargear);
        }

        for (Map.Entry<Integer, Integer> entry : weaponCountByID.entrySet()) {
            Datasheets_wargear wargear = datasheetWargearByID.get(entry.getKey());
            if (wargear == null) {
                continue;
            }

            WeaponProfile weapon = WeaponProfile.fromDatasheetWargear(wargear, entry.getValue());
            addWeapon(unit, weapon);
        }
    }

    private static void addWeapon(UnitInstance unit, WeaponProfile weapon) {
        if (weapon == null) {
            return;
        }

        if (weapon.melee()) {
            unit.addMeleeWeapon(weapon);
        } else {
            unit.addRangedWeapon(weapon);
        }
    }

    private static String loadPrimaryDetachmentId(int armyId) throws Exception {
        List<Army_detachment> detachments = StaticDataService.getArmyDetachments(armyId);
        if (detachments == null || detachments.isEmpty()) {
            return null;
        }
        return detachments.get(0).detachment_id();
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

    private static String resolveModelName(Datasheets_models model) {
        if (model == null) {
            return "";
        }
        return safe(model.name(), model.line());
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String safe(String value, String fallback) {
        String normalized = safe(value);
        return normalized.isBlank() ? safe(fallback) : normalized;
    }
    
    private static void importUnitRules(UnitInstance unit, DatasheetAggregate bundle) {
        importKeywords(unit, bundle == null ? List.of() : bundle.keywords);
        importAbilities(unit, bundle == null ? List.of() : bundle.abilities);
    }

    private static void importKeywords(UnitInstance unit, List<Datasheets_keywords> keywords) {
        if (unit == null || keywords == null) {
            return;
        }

        for (Datasheets_keywords keyword : keywords) {
            String value = safe(keyword.keyword());
            if (!value.isBlank()) {
                unit.addKeyword(value);
            }
        }
    }

    private static void importAbilities(UnitInstance unit, List<Datasheets_abilities> abilities) {
        if (unit == null || abilities == null) {
            return;
        }

        for (Datasheets_abilities ability : abilities) {
            String name = safe(ability.name(), ability.line());
            String description = stripHtml(safe(ability.description()));
            String type = safe(ability.type());

            if (name.isBlank() && description.isBlank()) {
                continue;
            }

            unit.addAbility(new UnitAbilityProfile(name, description, type));
        }
    }

    private static String stripHtml(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return text
                .replace("<br>", " ")
                .replace("<br/>", " ")
                .replace("<br />", " ")
                .replace("&nbsp;", " ")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
