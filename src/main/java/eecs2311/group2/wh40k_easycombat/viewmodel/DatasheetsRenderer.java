package eecs2311.group2.wh40k_easycombat.viewmodel;

import eecs2311.group2.wh40k_easycombat.model.instance.WeaponRow;
import eecs2311.group2.wh40k_easycombat.aggregate.DatasheetAggregate;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

import static eecs2311.group2.wh40k_easycombat.util.FxReflectionHelper.getAny;
import static eecs2311.group2.wh40k_easycombat.util.FxReflectionHelper.s;
import static eecs2311.group2.wh40k_easycombat.viewmodel.DatasheetsTextFormatter.*;

public final class DatasheetsRenderer {

    private DatasheetsRenderer() {
    }

    public static void renderBundle(
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
            TableView<WeaponRow> rangedWeaponTable,
            TableView<WeaponRow> meleeWeaponTable
    ) {
        if (datasheetName != null) {
            datasheetName.setText(s(getAny(bundle.datasheet, "name", "datasheet_name", "title")));
        }

        Object model1 = (bundle.models != null && !bundle.models.isEmpty()) ? bundle.models.get(0) : null;
        Object model2 = (bundle.models != null && bundle.models.size() > 1) ? bundle.models.get(1) : null;

        boolean hasModel2 = (model2 != null);

        if (unitName1 != null) {
            String model1Name = model1 == null ? "" : s(getAny(model1, "name", "model", "unit_name"));
            unitName1.setText(hasModel2 ? model1Name : "");
            setVisibleManaged(unitName1, hasModel2);
        }

        fillStats(model1, unit1MLabel, unit1TLabel, unit1SvLabel, unit1WLabel, unit1LdLabel, unit1OcLabel);
        updateInvSv(model1, invsvLabel, insvTxtLabel);

        if (unitName2 != null) {
            unitName2.setText(hasModel2 ? s(getAny(model2, "name", "model", "unit_name")) : "");
        }
        fillStats(model2, unit2MLabel, unit2TLabel, unit2SvLabel, unit2WLabel, unit2LdLabel, unit2OcLabel);

        setVisibleManaged(unit2PropertyHBox, hasModel2);
        setVisibleManaged(unitName2, hasModel2);

        setFlow(unitCompositionTextFlow, joinLines(bundle.compositions,
                "text", "composition", "rule", "description", "line_text"));

        setFlow(costTextFlow, buildModelCost(bundle.costs));
        setFlow(keywordsTextFlow, joinKeywordsComma(bundle.keywords));

        String normalAbilities = formatNonFactionAbilities(bundle.abilities, state);
        setFlow(abilityTextFlow, normalAbilities);
        setVisibleManaged(abilityTextFlow, !normalAbilities.isBlank());

        String factionAbilities = formatFactionAbilityNames(bundle.abilities, bundle.detachmentAbilities);
        setFlow(factionAbilityTextFlow, factionAbilities);
        setVisibleManaged(factionAbilityTextFlow, !factionAbilities.isBlank());

        String other = buildOtherSection(bundle);
        setFlow(otherTextFlow, other);
        setVisibleManaged(otherTextFlow, !other.isBlank());

        List<WeaponRow> ranged = new ArrayList<>();
        List<WeaponRow> melee = new ArrayList<>();

        if (bundle.wargear != null) {
            for (Object w : bundle.wargear) {
                WeaponRow row = WeaponRow.fromWargear(w);
                if (row == null) continue;
                if (row.isMelee()) melee.add(row);
                else ranged.add(row);
            }
        }

        if (rangedWeaponTable != null) rangedWeaponTable.getItems().setAll(ranged);
        if (meleeWeaponTable != null) meleeWeaponTable.getItems().setAll(melee);
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
            TableView<WeaponRow> rangedWeaponTable,
            TableView<WeaponRow> meleeWeaponTable,
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

    public static void setFlow(TextFlow flow, String text) {
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

    private static void fillStats(Object model, Label m, Label t, Label sv, Label w, Label ld, Label oc) {
        if (model == null) {
            clearLabels(m, t, sv, w, ld, oc);
            return;
        }

        if (m != null) m.setText(s(getAny(model, "m", "move", "movement", "M")));
        if (t != null) t.setText(s(getAny(model, "t", "toughness", "T")));
        if (sv != null) sv.setText(s(getAny(model, "sv", "save", "Sv")));
        if (w != null) w.setText(s(getAny(model, "w", "wounds", "W")));
        if (ld != null) ld.setText(s(getAny(model, "ld", "leadership", "Ld")));
        if (oc != null) oc.setText(s(getAny(model, "oc", "objective_control", "OC")));
    }

    private static void updateInvSv(Object model, Label invsvLabel, Label insvTxtLabel) {
        String inv = model == null ? "" : s(getAny(model, "inv_sv", "inv", "invSave", "invulnerable_save"));
        boolean visible = !inv.isBlank() && !"-".equals(inv);

        if (invsvLabel != null) {
            invsvLabel.setText(visible ? inv : "");
        }

        setVisibleManaged(invsvLabel, visible);
        setVisibleManaged(insvTxtLabel, visible);
    }

    private static void setVisibleManaged(Node node, boolean visible) {
        if (node == null) return;
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private static void clearLabels(Label... labels) {
        for (Label l : labels) {
            if (l != null) l.setText("");
        }
    }
}