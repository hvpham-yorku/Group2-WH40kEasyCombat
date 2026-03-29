package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

import java.util.List;
import java.util.Locale;

public final class ArmyImportApplyHelper {

    private ArmyImportApplyHelper() {
    }

    public static boolean applyImportedFaction(
            ComboBox<String> factionComboBox,
            Runnable refreshDetachmentOptions,
            Runnable rebuildUnitTree,
            String importedFactionName
    ) {
        String matchingDisplay = findMatchingDisplay(factionComboBox.getItems(), importedFactionName);
        if (matchingDisplay == null) {
            return false;
        }

        factionComboBox.setValue(matchingDisplay);
        refreshDetachmentOptions.run();
        rebuildUnitTree.run();
        return true;
    }

    public static boolean applyImportedDetachment(
            ComboBox<String> detachmentComboBox,
            String importedDetachmentName
    ) {
        String matchingDisplay = findMatchingDisplay(detachmentComboBox.getItems(), importedDetachmentName);
        if (matchingDisplay == null) {
            return false;
        }

        detachmentComboBox.setValue(matchingDisplay);
        return true;
    }

    public static void keepOnlyFirstImportedWarlord(ObservableList<ArmyUnitVM> currentArmy) {
        ArmyUnitVM firstWarlord = null;

        for (ArmyUnitVM unit : currentArmy) {
            if (!unit.warlordProperty().get()) {
                continue;
            }

            if (firstWarlord == null) {
                firstWarlord = unit;
            } else {
                unit.warlordProperty().set(false);
            }
        }
    }

    private static String findMatchingDisplay(List<String> displays, String importedValue) {
        String expected = normalizeImportText(importedValue);
        if (expected.isBlank()) {
            return null;
        }

        for (String display : displays) {
            if (normalizeImportText(display).equals(expected)) {
                return display;
            }
        }

        for (String display : displays) {
            String candidate = normalizeImportText(display);
            if (candidate.contains(expected) || expected.contains(candidate)) {
                return display;
            }
        }

        return null;
    }

    private static String normalizeImportText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return text.toLowerCase(Locale.ROOT)
                .replace('&', ' ')
                .replace('-', ' ')
                .replace('_', ' ')
                .replace('/', ' ')
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
