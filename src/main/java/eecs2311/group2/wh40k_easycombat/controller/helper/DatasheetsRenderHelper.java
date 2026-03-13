package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.model.aggregate.DatasheetAggregate;
import eecs2311.group2.wh40k_easycombat.model.Abilities;
import eecs2311.group2.wh40k_easycombat.model.Datasheets;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_abilities;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_detachment_abilities;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_keywords;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_leader;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models_cost;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_options;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_unit_composition;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_wargear;
import eecs2311.group2.wh40k_easycombat.model.Detachment_abilities;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService;
import eecs2311.group2.wh40k_easycombat.viewmodel.DatasheetListItemVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.DatasheetsPageState;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class DatasheetsRenderHelper {

    private DatasheetsRenderHelper() {
    }

    public static boolean renderSelectedDatasheet(
            DatasheetListItemVM selected,
            DatasheetsPageState state,
            Label datasheetName,
            Label unitName1,
            Label unitName2,
            HBox unit2PropertyHBox,
            Label unit1MLabel,
            Label unit1TLabel,
            Label unit1SvLabel,
            Label unit1WLabel,
            Label unit1LdLabel,
            Label unit1OcLabel,
            Label invsvLabel,
            Label insvTxtLabel,
            Label unit2MLabel,
            Label unit2TLabel,
            Label unit2SvLabel,
            Label unit2WLabel,
            Label unit2LdLabel,
            Label unit2OcLabel,
            TextFlow costTextFlow,
            TextFlow unitCompositionTextFlow,
            TextFlow keywordsTextFlow,
            TextFlow abilityTextFlow,
            TextFlow factionAbilityTextFlow,
            TextFlow otherTextFlow,
            TableView<WeaponProfile> rangedWeaponTable,
            TableView<WeaponProfile> meleeWeaponTable
    ) throws SQLException {
        if (selected == null || selected.getDatasheetId().isBlank()) {
            return false;
        }

        DatasheetAggregate bundle = StaticDataService.getDatasheetBundle(selected.getDatasheetId());
        if (bundle == null) {
            return false;
        }

        renderBundle(
                bundle,
                state,
                datasheetName,
                unitName1,
                unitName2,
                unit2PropertyHBox,
                unit1MLabel,
                unit1TLabel,
                unit1SvLabel,
                unit1WLabel,
                unit1LdLabel,
                unit1OcLabel,
                invsvLabel,
                insvTxtLabel,
                unit2MLabel,
                unit2TLabel,
                unit2SvLabel,
                unit2WLabel,
                unit2LdLabel,
                unit2OcLabel,
                costTextFlow,
                unitCompositionTextFlow,
                keywordsTextFlow,
                abilityTextFlow,
                factionAbilityTextFlow,
                otherTextFlow,
                rangedWeaponTable,
                meleeWeaponTable
        );

        return true;
    }

    public static void clearRightPanel(
            Label datasheetName,
            Label unitName1,
            Label unitName2,
            Label unit1MLabel,
            Label unit1TLabel,
            Label unit1SvLabel,
            Label unit1WLabel,
            Label unit1LdLabel,
            Label unit1OcLabel,
            Label invsvLabel,
            Label unit2MLabel,
            Label unit2TLabel,
            Label unit2SvLabel,
            Label unit2WLabel,
            Label unit2LdLabel,
            Label unit2OcLabel,
            TextFlow costTextFlow,
            TextFlow unitCompositionTextFlow,
            TextFlow keywordsTextFlow,
            TextFlow abilityTextFlow,
            TextFlow factionAbilityTextFlow,
            TextFlow otherTextFlow,
            TableView<WeaponProfile> rangedWeaponTable,
            TableView<WeaponProfile> meleeWeaponTable,
            HBox unit2PropertyHBox,
            Label insvTxtLabel
    ) {
        if (datasheetName != null) datasheetName.setText("");
        if (unitName1 != null) unitName1.setText("");
        if (unitName2 != null) unitName2.setText("");

        clearLabels(unit1MLabel, unit1TLabel, unit1SvLabel, unit1WLabel, unit1LdLabel, unit1OcLabel, invsvLabel);
        clearLabels(unit2MLabel, unit2TLabel, unit2SvLabel, unit2WLabel, unit2LdLabel, unit2OcLabel);

        setFlow(unitCompositionTextFlow, "");
        setFlow(costTextFlow, "");
        setFlow(keywordsTextFlow, "");
        setFlow(abilityTextFlow, "");
        setFlow(factionAbilityTextFlow, "");
        setFlow(otherTextFlow, "");

        setVisibleManaged(abilityTextFlow, false);
        setVisibleManaged(factionAbilityTextFlow, false);
        setVisibleManaged(otherTextFlow, false);

        if (rangedWeaponTable != null) rangedWeaponTable.getItems().clear();
        if (meleeWeaponTable != null) meleeWeaponTable.getItems().clear();

        setVisibleManaged(unit2PropertyHBox, false);
        setVisibleManaged(unitName1, true);
        setVisibleManaged(unitName2, false);
        setVisibleManaged(invsvLabel, false);
        setVisibleManaged(insvTxtLabel, false);
    }

    private static void renderBundle(
            DatasheetAggregate bundle,
            DatasheetsPageState state,
            Label datasheetName,
            Label unitName1,
            Label unitName2,
            HBox unit2PropertyHBox,
            Label unit1MLabel,
            Label unit1TLabel,
            Label unit1SvLabel,
            Label unit1WLabel,
            Label unit1LdLabel,
            Label unit1OcLabel,
            Label invsvLabel,
            Label insvTxtLabel,
            Label unit2MLabel,
            Label unit2TLabel,
            Label unit2SvLabel,
            Label unit2WLabel,
            Label unit2LdLabel,
            Label unit2OcLabel,
            TextFlow costTextFlow,
            TextFlow unitCompositionTextFlow,
            TextFlow keywordsTextFlow,
            TextFlow abilityTextFlow,
            TextFlow factionAbilityTextFlow,
            TextFlow otherTextFlow,
            TableView<WeaponProfile> rangedWeaponTable,
            TableView<WeaponProfile> meleeWeaponTable
    ) {
        if (datasheetName != null) {
            datasheetName.setText(safe(bundle.datasheet.name(), bundle.datasheet.id()));
        }

        Datasheets_models model1 = bundle.models.isEmpty() ? null : bundle.models.get(0);
        Datasheets_models model2 = bundle.models.size() > 1 ? nullSafeSecondModel(bundle.models) : null;
        boolean hasModel2 = model2 != null;

        if (unitName1 != null) {
            unitName1.setText(hasModel2 && model1 != null ? safe(model1.name()) : "");
            setVisibleManaged(unitName1, hasModel2);
        }

        fillStats(model1, unit1MLabel, unit1TLabel, unit1SvLabel, unit1WLabel, unit1LdLabel, unit1OcLabel);
        updateInvSv(model1, invsvLabel, insvTxtLabel);

        if (unitName2 != null) {
            unitName2.setText(hasModel2 ? safe(model2.name()) : "");
        }
        fillStats(model2, unit2MLabel, unit2TLabel, unit2SvLabel, unit2WLabel, unit2LdLabel, unit2OcLabel);

        setVisibleManaged(unit2PropertyHBox, hasModel2);
        setVisibleManaged(unitName2, hasModel2);

        setFlow(unitCompositionTextFlow, joinUnitComposition(bundle));
        setFlow(costTextFlow, buildModelCost(bundle.costs));
        setFlow(keywordsTextFlow, joinKeywordsComma(bundle.keywords));

        String normalAbilities = formatNonFactionAbilities(bundle.abilities, state);
        setFlow(abilityTextFlow, normalAbilities);
        setVisibleManaged(abilityTextFlow, !normalAbilities.isBlank());

        String factionAbilities = formatFactionAbilityNames(bundle.abilities, bundle.detachmentAbilities, state);
        setFlow(factionAbilityTextFlow, factionAbilities);
        setVisibleManaged(factionAbilityTextFlow, !factionAbilities.isBlank());

        String other = buildOtherSection(bundle, state);
        setFlow(otherTextFlow, other);
        setVisibleManaged(otherTextFlow, !other.isBlank());

        List<WeaponProfile> ranged = new ArrayList<>();
        List<WeaponProfile> melee = new ArrayList<>();

        for (Datasheets_wargear wargear : bundle.wargear) {
            WeaponProfile weapon = WeaponProfile.fromDatasheetWargear(wargear);
            if (weapon == null) continue;

            if (weapon.melee()) {
                melee.add(weapon);
            } else {
                ranged.add(weapon);
            }
        }

        if (rangedWeaponTable != null) {
            rangedWeaponTable.getItems().setAll(ranged);
        }
        if (meleeWeaponTable != null) {
            meleeWeaponTable.getItems().setAll(melee);
        }
    }

    private static Datasheets_models nullSafeSecondModel(List<Datasheets_models> models) {
        return models.size() > 1 ? models.get(1) : null;
    }

    private static void setFlow(TextFlow flow, String text) {
        if (flow == null) return;

        flow.getChildren().clear();
        if (text == null || text.isBlank()) return;

        String[] lines = text.split("\\n", -1);
        for (int i = 0; i < lines.length; i++) {
            addFormattedLine(flow, lines[i]);
            if (i < lines.length - 1) {
                flow.getChildren().add(new Text("\n"));
            }
        }
    }

    private static void addFormattedLine(TextFlow flow, String line) {
        if (line == null) return;

        String working = line;

        while (!working.isEmpty()) {
            int bStart = working.toLowerCase().indexOf("<b>");
            if (bStart < 0) {
                String plain = htmlToPlainText(working);
                if (!plain.isEmpty()) {
                    Text text = new Text(plain);
                    text.setStyle("-fx-font-size: 14px;");
                    flow.getChildren().add(text);
                }
                break;
            }

            if (bStart > 0) {
                String plainBefore = htmlToPlainText(working.substring(0, bStart));
                if (!plainBefore.isEmpty()) {
                    Text text = new Text(plainBefore);
                    text.setStyle("-fx-font-size: 14px;");
                    flow.getChildren().add(text);
                }
            }

            int bEnd = working.toLowerCase().indexOf("</b>", bStart);
            if (bEnd < 0) {
                String plain = htmlToPlainText(working);
                if (!plain.isEmpty()) {
                    Text text = new Text(plain);
                    text.setStyle("-fx-font-size: 14px;");
                    flow.getChildren().add(text);
                }
                break;
            }

            String boldContent = working.substring(bStart + 3, bEnd);
            String cleanedBold = htmlToPlainText(boldContent);

            if (!cleanedBold.isEmpty()) {
                Text boldText = new Text(cleanedBold);
                boldText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                flow.getChildren().add(boldText);
            }

            working = working.substring(bEnd + 4);
        }
    }

    private static void fillStats(Datasheets_models model, Label m, Label t, Label sv, Label w, Label ld, Label oc) {
        if (model == null) {
            clearLabels(m, t, sv, w, ld, oc);
            return;
        }

        if (m != null) m.setText(safe(model.M()));
        if (t != null) t.setText(safe(model.T()));
        if (sv != null) sv.setText(safe(model.Sv()));
        if (w != null) w.setText(safe(model.W()));
        if (ld != null) ld.setText(safe(model.Ld()));
        if (oc != null) oc.setText(safe(model.OC()));
    }

    private static void updateInvSv(Datasheets_models model, Label invsvLabel, Label insvTxtLabel) {
        String inv = model == null ? "" : safe(model.inv_sv());
        boolean visible = !inv.isBlank() && !"-".equals(inv);

        if (invsvLabel != null) {
            invsvLabel.setText(visible ? inv : "");
        }

        setVisibleManaged(invsvLabel, visible);
        setVisibleManaged(insvTxtLabel, visible);
    }

    private static String joinUnitComposition(DatasheetAggregate bundle) {
        return bundle.compositions.stream()
                .map(DatasheetsRenderHelper::compositionLine)
                .filter(line -> !line.isBlank())
                .collect(Collectors.joining("\n"));
    }

    private static String compositionLine(Datasheets_unit_composition composition) {
        return safe(composition.description(), composition.line());
    }

    private static String joinKeywordsComma(List<Datasheets_keywords> keywords) {
        return keywords.stream()
                .map(Datasheets_keywords::keyword)
                .filter(x -> x != null && !x.isBlank())
                .collect(Collectors.joining(", "));
    }

    private static String formatNonFactionAbilities(List<Datasheets_abilities> list, DatasheetsPageState state) {
        if (list == null || list.isEmpty()) return "";

        List<String> coreNames = new ArrayList<>();
        List<String> otherAbilities = new ArrayList<>();

        Set<String> seenCore = new LinkedHashSet<>();
        Set<String> seenOther = new LinkedHashSet<>();

        for (Datasheets_abilities ability : list) {
            String type = safe(ability.type()).toLowerCase();

            if (type.contains("faction")) {
                continue;
            }

            if (type.contains("core")) {
                String name = resolveAbilityName(ability, state);
                if (!name.isBlank() && seenCore.add(name)) {
                    coreNames.add(name);
                }
                continue;
            }

            String line = resolveAbilityFullTextForDisplay(ability, state);
            if (!line.isBlank() && seenOther.add(line)) {
                otherAbilities.add(line);
            }
        }

        List<String> blocks = new ArrayList<>();

        if (!coreNames.isEmpty()) {
            blocks.add("<b>Core:</b> " + String.join(", ", coreNames));
        }

        blocks.addAll(otherAbilities);

        return String.join("\n", blocks);
    }

    private static String formatFactionAbilityNames(
            List<Datasheets_abilities> abilities,
            List<Datasheets_detachment_abilities> detachmentAbilities,
            DatasheetsPageState state
    ) {
        List<String> out = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (Datasheets_abilities ability : abilities) {
            if (!safe(ability.type()).toLowerCase().contains("faction")) {
                continue;
            }

            String name = resolveAbilityName(ability, state);
            if (!name.isBlank() && seen.add(name)) {
                out.add("<b>" + name + "</b>");
            }
        }

        for (Datasheets_detachment_abilities detachmentAbility : detachmentAbilities) {
            String id = safe(detachmentAbility.detachment_ability_id());
            if (id.isBlank()) continue;

            Detachment_abilities master = state.getDetachmentAbilitiesById().get(id);
            String name = master == null ? id : safe(master.name(), id);

            if (!name.isBlank() && seen.add(name)) {
                out.add("<b>" + name + "</b>");
            }
        }

        return String.join("\n", out);
    }

    private static String buildModelCost(List<Datasheets_models_cost> costs) {
        List<String> lines = new ArrayList<>();

        for (Datasheets_models_cost cost : costs) {
            String desc = safe(cost.description());
            String points = safe(cost.cost());

            if (desc.isBlank() && points.isBlank()) continue;

            if (!desc.isBlank() && !points.isBlank()) {
                lines.add(desc + " " + points);
            } else if (!desc.isBlank()) {
                lines.add(desc);
            } else {
                lines.add(points);
            }
        }

        return String.join("\n", lines);
    }

    private static String buildOtherSection(DatasheetAggregate bundle, DatasheetsPageState state) {
        List<String> sections = new ArrayList<>();

        String leadersText = buildLeaderSection(bundle, state);
        if (!leadersText.isBlank()) {
            sections.add("ATTACHED UNIT\n" + leadersText);
        }

        String optionsText = bundle.options.stream()
                .map(DatasheetsRenderHelper::optionLine)
                .filter(line -> !line.isBlank())
                .collect(Collectors.joining("\n"));
        if (!optionsText.isBlank()) {
            sections.add("OPTIONS\n" + optionsText);
        }

        return String.join("\n\n", sections).trim();
    }

    private static String buildLeaderSection(DatasheetAggregate bundle, DatasheetsPageState state) {
        List<String> lines = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        String head = safe(bundle.datasheet.leader_head());
        if (!head.isBlank()) {
            lines.add(head);
        }

        for (Datasheets_leader leader : bundle.leaders) {
            String leaderId = safe(leader.leader_id());
            if (leaderId.isBlank()) {
                continue;
            }

            Datasheets leaderDatasheet = state.getDatasheetsById().get(leaderId);
            String leaderName = leaderDatasheet == null
                    ? leaderId
                    : safe(leaderDatasheet.name(), leaderId);

            if (seen.add(leaderName)) {
                lines.add("<b>" + leaderName + "</b>");
            }
        }

        String footer = safe(bundle.datasheet.leader_footer());
        if (!footer.isBlank()) {
            lines.add(footer);
        }

        return String.join("\n", lines).trim();
    }

    private static String optionLine(Datasheets_options option) {
        String button = safe(option.button());
        String description = safe(option.description());

        if (!button.isBlank() && !description.isBlank()) {
            return button + ": " + description;
        }
        if (!description.isBlank()) {
            return description;
        }
        return button;
    }

    private static String resolveAbilityFullTextForDisplay(Datasheets_abilities datasheetAbility, DatasheetsPageState state) {
        if (datasheetAbility == null) return "";

        String localName = safe(datasheetAbility.name());
        String localDesc = safe(datasheetAbility.description());
        String abilityId = safe(datasheetAbility.ability_id());

        Abilities master = abilityId.isBlank() ? null : state.getAbilitiesById().get(abilityId);
        String masterName = master == null ? "" : safe(master.name());
        String masterDesc = master == null ? "" : safe(master.description());

        String name = !localName.isBlank() ? localName : masterName;
        String desc = !localDesc.isBlank() ? localDesc : masterDesc;

        if (!name.isBlank() && !desc.isBlank()) return "<b>" + name + ":</b> " + desc;
        if (!name.isBlank()) return "<b>" + name + "</b>";
        return desc;
    }

    private static String resolveAbilityName(Datasheets_abilities datasheetAbility, DatasheetsPageState state) {
        if (datasheetAbility == null) return "";

        String localName = safe(datasheetAbility.name());
        if (!localName.isBlank()) {
            return localName;
        }

        String abilityId = safe(datasheetAbility.ability_id());
        if (abilityId.isBlank()) {
            return "";
        }

        Abilities master = state.getAbilitiesById().get(abilityId);
        return master == null ? "" : safe(master.name());
    }

    private static String htmlToPlainText(String html) {
        if (html == null || html.isBlank()) return "";

        String value = html;
        value = value.replaceAll("(?i)<br\\s*/?>", "\n");
        value = value.replaceAll("(?i)<li[^>]*>", "- ");
        value = value.replaceAll("(?i)</li>", "\n");
        value = value.replaceAll("(?i)<a[^>]*>", "");
        value = value.replaceAll("(?i)</a>", "");
        value = value.replaceAll("(?is)<(?!/?b\\b)[^>]+>", "");
        value = value.replace("&nbsp;", " ");
        value = value.replace("&lt;", "<");
        value = value.replace("&gt;", ">");
        value = value.replace("&amp;", "&");
        value = value.replace("&quot;", "\"");
        value = value.replace("&#39;", "'");
        value = value.replace("\r", "");
        value = value.replaceAll("[ \\t]+", " ");

        return value.trim();
    }

    private static void setVisibleManaged(Node node, boolean visible) {
        if (node == null) return;
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private static void clearLabels(Label... labels) {
        for (Label label : labels) {
            if (label != null) {
                label.setText("");
            }
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String safe(String value, String fallback) {
        String normalized = safe(value);
        return normalized.isBlank() ? safe(fallback) : normalized;
    }
}
