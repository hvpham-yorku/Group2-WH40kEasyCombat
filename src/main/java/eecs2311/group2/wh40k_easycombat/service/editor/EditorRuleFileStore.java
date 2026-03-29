package eecs2311.group2.wh40k_easycombat.service.editor;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleAttackType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDuration;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRulePhase;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleTargetRole;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class EditorRuleFileStore {
    private static final Path STORAGE_FOLDER = Path.of("data", "dsl");

    public List<EditorRuleDefinition> loadAll() {
        if (!Files.isDirectory(STORAGE_FOLDER)) {
            return new ArrayList<>();
        }

        List<EditorRuleDefinition> rules = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(STORAGE_FOLDER)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".rule"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()))
                    .forEach(path -> rules.add(readRule(path)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load VM rule files from " + STORAGE_FOLDER + ": " + e.getMessage(), e);
        }

        return rules;
    }

    public EditorRuleDefinition loadFromPath(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Rule file path must not be null.");
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Rule file was not found: " + path);
        }

        return readRule(path);
    }

    public void save(EditorRuleDefinition rule) {
        if (rule == null) {
            return;
        }

        try {
            Files.createDirectories(STORAGE_FOLDER);
            Files.writeString(resolvePath(rule.getId()), serialize(rule));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save VM rule file for " + rule.getName() + ": " + e.getMessage(), e);
        }
    }

    public void writeToPath(EditorRuleDefinition rule, Path path) {
        if (rule == null) {
            throw new IllegalArgumentException("Rule must not be null.");
        }
        if (path == null) {
            throw new IllegalArgumentException("Export path must not be null.");
        }

        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, serialize(rule));
        } catch (IOException e) {
            throw new RuntimeException("Failed to export VM rule file to " + path + ": " + e.getMessage(), e);
        }
    }

    public boolean delete(String ruleId) {
        if (ruleId == null || ruleId.isBlank()) {
            return false;
        }

        Path path = resolvePath(ruleId);
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete VM rule file " + path + ": " + e.getMessage(), e);
        }
    }

    private EditorRuleDefinition readRule(Path path) {
        try {
            List<String> lines = Files.readAllLines(path);
            EditorRuleDefinition rule = new EditorRuleDefinition();
            List<String> scriptLines = new ArrayList<>();
            boolean inMetadata = true;

            for (String line : lines) {
                String safeLine = line == null ? "" : line;
                if (inMetadata && safeLine.startsWith("# @")) {
                    applyMetadata(rule, safeLine.substring(3).trim());
                    continue;
                }
                if (inMetadata && safeLine.isBlank()) {
                    continue;
                }

                inMetadata = false;
                scriptLines.add(safeLine);
            }

            if (rule.getId() == null || rule.getId().isBlank()) {
                String fileName = path.getFileName().toString();
                int dot = fileName.lastIndexOf('.');
                rule.setId(dot > 0 ? fileName.substring(0, dot) : fileName);
            }
            if (rule.getName().isBlank()) {
                rule.setName(path.getFileName().toString());
            }
            rule.setDslScript(String.join("\n", scriptLines));
            return rule;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read VM rule file " + path + ": " + e.getMessage(), e);
        }
    }

    private String serialize(EditorRuleDefinition rule) {
        StringBuilder builder = new StringBuilder();
        appendMetadata(builder, "id", rule.getId());
        appendMetadata(builder, "name", rule.getName());
        appendMetadata(builder, "type", rule.getType().name());
        appendMetadata(builder, "phase", rule.getPhase().name());
        appendMetadata(builder, "attackType", rule.getAttackType().name());
        appendMetadata(builder, "enabled", Boolean.toString(rule.isEnabled()));
        appendMetadata(builder, "optionalActivation", Boolean.toString(rule.isOptionalActivation()));
        appendMetadata(builder, "attackerUnitNameContains", rule.getAttackerUnitNameContains());
        appendMetadata(builder, "defenderUnitNameContains", rule.getDefenderUnitNameContains());
        appendMetadata(builder, "weaponNameContains", rule.getWeaponNameContains());
        appendMetadata(builder, "attackerKeyword", rule.getAttackerKeyword());
        appendMetadata(builder, "defenderKeyword", rule.getDefenderKeyword());
        appendMetadata(builder, "attackerAbilityNameContains", rule.getAttackerAbilityNameContains());
        appendMetadata(builder, "defenderAbilityNameContains", rule.getDefenderAbilityNameContains());
        appendMetadata(builder, "attackerFactionAbilityNameContains", rule.getAttackerFactionAbilityNameContains());
        appendMetadata(builder, "defenderFactionAbilityNameContains", rule.getDefenderFactionAbilityNameContains());
        appendMetadata(builder, "attackerDetachmentAbilityNameContains", rule.getAttackerDetachmentAbilityNameContains());
        appendMetadata(builder, "defenderDetachmentAbilityNameContains", rule.getDefenderDetachmentAbilityNameContains());
        appendMetadata(builder, "attackerFactionNameContains", rule.getAttackerFactionNameContains());
        appendMetadata(builder, "defenderFactionNameContains", rule.getDefenderFactionNameContains());
        appendMetadata(builder, "attackerDetachmentNameContains", rule.getAttackerDetachmentNameContains());
        appendMetadata(builder, "defenderDetachmentNameContains", rule.getDefenderDetachmentNameContains());
        appendMetadata(builder, "attackerEnhancementNameContains", rule.getAttackerEnhancementNameContains());
        appendMetadata(builder, "defenderEnhancementNameContains", rule.getDefenderEnhancementNameContains());
        appendMetadata(builder, "triggeringStratagemNameContains", rule.getTriggeringStratagemNameContains());
        appendMetadata(builder, "duration", rule.getDuration().name());
        appendMetadata(builder, "targetRole", rule.getTargetRole().name());

        appendMetadata(builder, "visualWithinHalfRange", Boolean.toString(rule.isVisualWithinHalfRange()));
        appendMetadata(builder, "visualRemainedStationary", Boolean.toString(rule.isVisualRemainedStationary()));
        appendMetadata(builder, "visualAdvancedThisTurn", Boolean.toString(rule.isVisualAdvancedThisTurn()));
        appendMetadata(builder, "visualFellBackThisTurn", Boolean.toString(rule.isVisualFellBackThisTurn()));
        appendMetadata(builder, "visualChargedThisTurn", Boolean.toString(rule.isVisualChargedThisTurn()));
        appendMetadata(builder, "visualAttackerCanFight", Boolean.toString(rule.isVisualAttackerCanFight()));
        appendMetadata(builder, "visualTargetHasCover", Boolean.toString(rule.isVisualTargetHasCover()));
        appendMetadata(builder, "visualBlastIsLegal", Boolean.toString(rule.isVisualBlastIsLegal()));
        appendMetadata(builder, "visualTargetIsInfantry", Boolean.toString(rule.isVisualTargetIsInfantry()));
        appendMetadata(builder, "visualTargetIsVehicle", Boolean.toString(rule.isVisualTargetIsVehicle()));
        appendMetadata(builder, "visualTargetIsMonster", Boolean.toString(rule.isVisualTargetIsMonster()));
        appendMetadata(builder, "visualTargetIsCharacter", Boolean.toString(rule.isVisualTargetIsCharacter()));
        appendMetadata(builder, "visualTargetIsPsyker", Boolean.toString(rule.isVisualTargetIsPsyker()));
        appendMetadata(builder, "visualHitModifier", Integer.toString(rule.getVisualHitModifier()));
        appendMetadata(builder, "visualWoundModifier", Integer.toString(rule.getVisualWoundModifier()));
        appendMetadata(builder, "visualAttacksModifier", Integer.toString(rule.getVisualAttacksModifier()));
        appendMetadata(builder, "visualDamageModifier", Integer.toString(rule.getVisualDamageModifier()));
        appendMetadata(builder, "visualApModifier", Integer.toString(rule.getVisualApModifier()));
        appendMetadata(builder, "visualHitReroll", rule.getVisualHitReroll().name());
        appendMetadata(builder, "visualWoundReroll", rule.getVisualWoundReroll().name());
        appendMetadata(builder, "visualExtraWeaponKeywords", rule.getVisualExtraWeaponKeywords());

        builder.append('\n');
        builder.append(rule.getDslScript() == null ? "" : rule.getDslScript().replace("\r\n", "\n").replace('\r', '\n'));
        builder.append('\n');
        return builder.toString();
    }

    private void applyMetadata(EditorRuleDefinition rule, String metadataLine) {
        int separator = metadataLine.indexOf('=');
        if (separator <= 0) {
            return;
        }

        String key = metadataLine.substring(0, separator).trim();
        String value = metadataLine.substring(separator + 1).trim();

        switch (key) {
            case "id" -> rule.setId(value);
            case "name" -> rule.setName(value);
            case "type" -> rule.setType(parseEnum(EditorRuleType.class, value, EditorRuleType.ABILITY));
            case "phase" -> rule.setPhase(parseEnum(EditorRulePhase.class, value, EditorRulePhase.ANY));
            case "attackType" -> rule.setAttackType(parseEnum(EditorRuleAttackType.class, value, EditorRuleAttackType.ANY));
            case "enabled" -> rule.setEnabled(Boolean.parseBoolean(value));
            case "optionalActivation" -> rule.setOptionalActivation(Boolean.parseBoolean(value));
            case "attackerUnitNameContains" -> rule.setAttackerUnitNameContains(value);
            case "defenderUnitNameContains" -> rule.setDefenderUnitNameContains(value);
            case "weaponNameContains" -> rule.setWeaponNameContains(value);
            case "attackerKeyword" -> rule.setAttackerKeyword(value);
            case "defenderKeyword" -> rule.setDefenderKeyword(value);
            case "attackerAbilityNameContains" -> rule.setAttackerAbilityNameContains(value);
            case "defenderAbilityNameContains" -> rule.setDefenderAbilityNameContains(value);
            case "attackerFactionAbilityNameContains" -> rule.setAttackerFactionAbilityNameContains(value);
            case "defenderFactionAbilityNameContains" -> rule.setDefenderFactionAbilityNameContains(value);
            case "attackerDetachmentAbilityNameContains" -> rule.setAttackerDetachmentAbilityNameContains(value);
            case "defenderDetachmentAbilityNameContains" -> rule.setDefenderDetachmentAbilityNameContains(value);
            case "attackerFactionNameContains" -> rule.setAttackerFactionNameContains(value);
            case "defenderFactionNameContains" -> rule.setDefenderFactionNameContains(value);
            case "attackerDetachmentNameContains" -> rule.setAttackerDetachmentNameContains(value);
            case "defenderDetachmentNameContains" -> rule.setDefenderDetachmentNameContains(value);
            case "attackerEnhancementNameContains" -> rule.setAttackerEnhancementNameContains(value);
            case "defenderEnhancementNameContains" -> rule.setDefenderEnhancementNameContains(value);
            case "triggeringStratagemNameContains" -> rule.setTriggeringStratagemNameContains(value);
            case "duration" -> rule.setDuration(parseEnum(EditorRuleDuration.class, value, EditorRuleDuration.UNTIL_END_OF_PHASE));
            case "targetRole" -> rule.setTargetRole(parseEnum(EditorRuleTargetRole.class, value, EditorRuleTargetRole.ATTACKER));
            case "visualWithinHalfRange" -> rule.setVisualWithinHalfRange(Boolean.parseBoolean(value));
            case "visualRemainedStationary" -> rule.setVisualRemainedStationary(Boolean.parseBoolean(value));
            case "visualAdvancedThisTurn" -> rule.setVisualAdvancedThisTurn(Boolean.parseBoolean(value));
            case "visualFellBackThisTurn" -> rule.setVisualFellBackThisTurn(Boolean.parseBoolean(value));
            case "visualChargedThisTurn" -> rule.setVisualChargedThisTurn(Boolean.parseBoolean(value));
            case "visualAttackerCanFight" -> rule.setVisualAttackerCanFight(Boolean.parseBoolean(value));
            case "visualTargetHasCover" -> rule.setVisualTargetHasCover(Boolean.parseBoolean(value));
            case "visualBlastIsLegal" -> rule.setVisualBlastIsLegal(Boolean.parseBoolean(value));
            case "visualTargetIsInfantry" -> rule.setVisualTargetIsInfantry(Boolean.parseBoolean(value));
            case "visualTargetIsVehicle" -> rule.setVisualTargetIsVehicle(Boolean.parseBoolean(value));
            case "visualTargetIsMonster" -> rule.setVisualTargetIsMonster(Boolean.parseBoolean(value));
            case "visualTargetIsCharacter" -> rule.setVisualTargetIsCharacter(Boolean.parseBoolean(value));
            case "visualTargetIsPsyker" -> rule.setVisualTargetIsPsyker(Boolean.parseBoolean(value));
            case "visualHitModifier" -> rule.setVisualHitModifier(parseInt(value));
            case "visualWoundModifier" -> rule.setVisualWoundModifier(parseInt(value));
            case "visualAttacksModifier" -> rule.setVisualAttacksModifier(parseInt(value));
            case "visualDamageModifier" -> rule.setVisualDamageModifier(parseInt(value));
            case "visualApModifier" -> rule.setVisualApModifier(parseInt(value));
            case "visualHitReroll" -> rule.setVisualHitReroll(parseEnum(EditorRerollType.class, value, EditorRerollType.NONE));
            case "visualWoundReroll" -> rule.setVisualWoundReroll(parseEnum(EditorRerollType.class, value, EditorRerollType.NONE));
            case "visualExtraWeaponKeywords" -> rule.setVisualExtraWeaponKeywords(value);
            default -> {
            }
        }
    }

    private void appendMetadata(StringBuilder builder, String key, String value) {
        String safeValue = value == null ? "" : value.replace("\r\n", " ").replace('\n', ' ').replace('\r', ' ').trim();
        builder.append("# @").append(key).append('=').append(safeValue).append('\n');
    }

    private Path resolvePath(String ruleId) {
        return STORAGE_FOLDER.resolve(ruleId.trim() + ".rule");
    }

    private <T extends Enum<T>> T parseEnum(Class<T> type, String value, T fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.trim());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value == null ? "0" : value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
