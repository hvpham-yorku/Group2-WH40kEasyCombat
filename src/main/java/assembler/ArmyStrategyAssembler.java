package assembler;

import eecs2311.group2.wh40k_easycombat.model.Army_detachment;
import eecs2311.group2.wh40k_easycombat.model.Stratagems;
import eecs2311.group2.wh40k_easycombat.repository.StratagemsRepository;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameStrategyVM;

import java.util.ArrayList;
import java.util.List;

public final class ArmyStrategyAssembler {

    private ArmyStrategyAssembler() {
    }

    public static List<GameStrategyVM> importStrategiesForArmy(int armyId) throws Exception {

        List<GameStrategyVM> detachmentStrategies = new ArrayList<>();
        List<GameStrategyVM> coreStrategies = new ArrayList<>();

        String detachmentId = getArmyDetachmentId(armyId);

        List<Stratagems> all = StratagemsRepository.getAllStratagems();

        for (Stratagems s : all) {

            if (s == null) continue;

            String stratDetachment = safe(s.detachment_id());
            String stratFaction = safe(s.faction_id());
            String stratType = safe(s.type());

            GameStrategyVM vm = new GameStrategyVM(
                    safe(s.name()),
                    safe(s.cp_cost()),
                    safe(s.turn()),
                    safe(s.phase()),
                    safe(s.description())
            );

            // 1️ Detachment stratagem
            if (!stratDetachment.isBlank()) {
                if (detachmentId != null && detachmentId.equalsIgnoreCase(stratDetachment)) {
                    detachmentStrategies.add(vm);
                }
                continue;
            }

            // 2️ Core stratagem
            if (stratFaction.isBlank()
                    && stratDetachment.isBlank()
                    && stratType.toLowerCase().contains("core")) {

                coreStrategies.add(vm);
            }
        }

        List<GameStrategyVM> result = new ArrayList<>();

        result.addAll(detachmentStrategies);
        result.addAll(coreStrategies);

        return result;
    }

    private static String getArmyDetachmentId(int armyId) throws Exception {

        List<Army_detachment> detachments = StaticDataService.getArmyDetachments(armyId);

        if (detachments == null || detachments.isEmpty()) {
            return null;
        }

        return detachments.get(0).detachment_id();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}