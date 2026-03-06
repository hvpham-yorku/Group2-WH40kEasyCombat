package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.service.StaticDataService;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService.DatasheetBundle;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class DatasheetsController implements Initializable {

    // ======================= Buttons ==========================
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button searchButton;
    @FXML private Button backToMainButton;

    // ======================= Inputs ===========================
    @FXML private TextField searchTextField;
    @FXML private ComboBox<String> factionComboBox;

    // ======================= Lists ============================
    @FXML private ListView<Object> datasheetsList;

    // ======================= Labels - Datasheet / Unit Names ===
    @FXML private Label datasheetName;
    @FXML private Label unitName1;
    @FXML private Label unitName2;

    // ======================= Unit 1 - Properties ==============
    @FXML private HBox unit1PropertyHBox;
    @FXML private Label unit1MLabel;
    @FXML private Label unit1TLabel;
    @FXML private Label unit1SvLabel;
    @FXML private Label unit1WLabel;
    @FXML private Label unit1OcLabel;
    @FXML private Label unit1LdLabel;

    // ======================= Unit 2 - Properties ==============
    @FXML private HBox unit2PropertyHBox;
    @FXML private Label unit2MLabel;
    @FXML private Label unit2TLabel;
    @FXML private Label unit2SvLabel;
    @FXML private Label unit2WLabel;
    @FXML private Label unit2OcLabel;
    @FXML private Label unit2LdLabel;

    // ======================= TextFlows - Descriptions =========
    @FXML private TextFlow costTextFlow;
    @FXML private TextFlow unitCompositionTextFlow;
    @FXML private TextFlow keywordsTextFlow;
    @FXML private TextFlow abilityTextFlow;
    @FXML private TextFlow factionAbilityTextFlow;
    @FXML private TextFlow otherTextFlow;

    // ======================= Tables - Melee Weapons ===========
    @FXML private TableView<WeaponRow> meleeWeaponTable;
    @FXML private TableColumn<WeaponRow, String> meleeWeaponName;
    @FXML private TableColumn<WeaponRow, String> meleeWeaponRange;
    @FXML private TableColumn<WeaponRow, String> meleeWeaponA;
    @FXML private TableColumn<WeaponRow, String> meleeWeaponWS;
    @FXML private TableColumn<WeaponRow, String> meleeWeaponAP;
    @FXML private TableColumn<WeaponRow, String> meleeWeaponD;

    // ======================= Tables - Ranged Weapons ==========
    @FXML private TableView<WeaponRow> rangedWeaponTable;
    @FXML private TableColumn<WeaponRow, String> rangedWeaponName;
    @FXML private TableColumn<WeaponRow, String> rangedWeaponRange;
    @FXML private TableColumn<WeaponRow, String> rangedWeaponA;
    @FXML private TableColumn<WeaponRow, String> rangedWeaponBS;
    @FXML private TableColumn<WeaponRow, String> rangedWeaponAP;
    @FXML private TableColumn<WeaponRow, String> rangedWeaponD;

    // ======================= In-memory ========================
    private final ObservableList<Object> allDatasheets = FXCollections.observableArrayList();
    private final ObservableList<Object> filteredDatasheets = FXCollections.observableArrayList();

    // Optional mapping if datasheets store faction_id but UI shows faction name
    private final Map<String, String> factionNameToId = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
        loadDatasheetsAndFactions();
        wireEvents();
    }

    // -------------------- Table setup --------------------

    private void setupTables() {
        if (rangedWeaponName != null) {
            rangedWeaponName.setCellValueFactory(c -> c.getValue().displayName);
            rangedWeaponRange.setCellValueFactory(c -> c.getValue().range);
            rangedWeaponA.setCellValueFactory(c -> c.getValue().a);
            rangedWeaponBS.setCellValueFactory(c -> c.getValue().skill);
            rangedWeaponAP.setCellValueFactory(c -> c.getValue().ap);
            rangedWeaponD.setCellValueFactory(c -> c.getValue().d);
            applyWrapCellFactory(rangedWeaponName);
        }

        if (meleeWeaponName != null) {
            meleeWeaponName.setCellValueFactory(c -> c.getValue().displayName);
            meleeWeaponRange.setCellValueFactory(c -> c.getValue().range);
            meleeWeaponA.setCellValueFactory(c -> c.getValue().a);
            meleeWeaponWS.setCellValueFactory(c -> c.getValue().skill);
            meleeWeaponAP.setCellValueFactory(c -> c.getValue().ap);
            meleeWeaponD.setCellValueFactory(c -> c.getValue().d);
            applyWrapCellFactory(meleeWeaponName);
        }

        if (rangedWeaponTable != null) rangedWeaponTable.setItems(FXCollections.observableArrayList());
        if (meleeWeaponTable != null) meleeWeaponTable.setItems(FXCollections.observableArrayList());
    }

    private void applyWrapCellFactory(TableColumn<WeaponRow, String> col) {
        col.setCellFactory(tc -> new TableCell<>() {
            private final Label label = new Label();
            {
                label.setWrapText(true);
                label.setMaxWidth(Double.MAX_VALUE);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item);
                    setGraphic(label);
                }
            }
        });
    }

    // -------------------- Data loading --------------------
    private void loadDatasheetsAndFactions() {
        try {
            // Load caches
            StaticDataService.loadAllFromSqlite();

            // Load datasheets list
            List<?> list = eecs2311.group2.wh40k_easycombat.repository.DatasheetsRepository.getAllDatasheets();
            allDatasheets.setAll((Collection<? extends Object>) list);
            filteredDatasheets.setAll(allDatasheets);

            datasheetsList.setItems(filteredDatasheets);
            datasheetsList.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        return;
                    }
                    String name = s(getAny(item, "name", "datasheet_name", "title"));
                    if (name.isBlank()) name = s(getAny(item, "id", "datasheet_id"));
                    setText(name);
                }
            });

            loadFactionsIntoComboBox();

            // Default selection
            if (!filteredDatasheets.isEmpty()) {
                datasheetsList.getSelectionModel().select(0);
                showSelectedDatasheet();
            } else {
                clearRightPanel();
            }
        } catch (Exception e) {
            showError("Load data failed", e);
        }
    }

    private void loadFactionsIntoComboBox() {
        if (factionComboBox == null) return;

        factionComboBox.getItems().clear();
        factionComboBox.getItems().add("ALL");
        factionNameToId.clear();

        // Preferred: read factions from FactionsRepository if it exists
        boolean loadedFromFactionRepo = false;
        try {
            Class<?> repo = Class.forName("eecs2311.group2.wh40k_easycombat.repository.FactionsRepository");
            Method m = repo.getMethod("getAllFactions");
            Object result = m.invoke(null);
            if (result instanceof List<?> factions) {
                for (Object f : factions) {
                    String id = s(getAny(f, "id", "faction_id"));
                    String name = s(getAny(f, "name", "faction_name"));
                    if (!id.isBlank() && !name.isBlank()) {
                        factionNameToId.put(name, id);
                    }
                }
                loadedFromFactionRepo = !factionNameToId.isEmpty();
            }
        } catch (Exception ignored) {
            // Fall back below
        }

        if (loadedFromFactionRepo) {
            List<String> names = new ArrayList<>(factionNameToId.keySet());
            Collections.sort(names);
            factionComboBox.getItems().addAll(names);
            factionComboBox.setValue("ALL");
            return;
        }

        // Fallback: derive from datasheets object fields (may already be faction_name)
        Set<String> factions = allDatasheets.stream()
                .map(d -> s(getAny(d, "faction_name", "faction", "army")))
                .filter(x -> !x.isBlank())
                .collect(Collectors.toCollection(TreeSet::new));

        factionComboBox.getItems().addAll(factions);
        factionComboBox.setValue("ALL");
    }

    // -------------------- Events wiring --------------------

    private void wireEvents() {
        if (datasheetsList != null) {
            datasheetsList.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldV, newV) -> showSelectedDatasheet());
        }

        // Faction selection should refresh list immediately
        if (factionComboBox != null) {
            factionComboBox.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldV, newV) -> applyFilters());
        }

        // Search button triggers filters
        if (searchButton != null) {
            searchButton.setOnMouseClicked(e -> applyFilters());
        }
    }

    // FXML may have onAction bound to this
    @FXML
    private void selectFaction(ActionEvent event) {
        applyFilters();
    }

    // FXML legacy handler (if still wired somewhere)
    @FXML
    void changeFaction(InputMethodEvent event) {
        // InputMethodEvent is NOT ideal for ComboBox, but keep it so FXML won't crash if still wired.
        applyFilters();
    }

    @FXML
    void clickSearchButton(MouseEvent event) {
        applyFilters();
    }

    @FXML
    void clickAddButton(MouseEvent event) {
        // TODO
    }

    @FXML
    void clickEditButton(MouseEvent event) {
        // TODO
    }

    @FXML
    void clickBackButton(MouseEvent event) throws IOException {
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/MainUI.fxml",
                1200.0, 800.0);
    }

    // -------------------- Filtering --------------------

    private void applyFilters() {
        String keyword = searchTextField == null || searchTextField.getText() == null
                ? ""
                : searchTextField.getText().trim().toLowerCase();

        String selectedFaction = (factionComboBox == null || factionComboBox.getValue() == null)
                ? "ALL"
                : factionComboBox.getValue();

        // If mapping exists (name -> id), use id too
        String selectedFactionId = factionNameToId.getOrDefault(selectedFaction, selectedFaction);

        filteredDatasheets.setAll(allDatasheets.stream()
                .filter(d -> {
                    if (!"ALL".equalsIgnoreCase(selectedFaction)) {
                        String fName = s(getAny(d, "faction_name", "faction", "army"));
                        String fId = s(getAny(d, "faction_id"));

                        boolean match = false;
                        if (!fId.isBlank()) match = fId.equalsIgnoreCase(selectedFactionId);
                        if (!match && !fName.isBlank()) match = fName.equalsIgnoreCase(selectedFaction);

                        if (!match) return false;
                    }

                    if (!keyword.isBlank()) {
                        String name = s(getAny(d, "name", "datasheet_name", "title")).toLowerCase();
                        String id = s(getAny(d, "id", "datasheet_id")).toLowerCase();
                        return name.contains(keyword) || id.contains(keyword);
                    }
                    return true;
                })
                .toList());

        if (!filteredDatasheets.isEmpty()) {
            datasheetsList.getSelectionModel().select(0);
            showSelectedDatasheet();
        } else {
            clearRightPanel();
        }
    }

    // -------------------- Selection rendering --------------------

    private void showSelectedDatasheet() {
        Object selected = datasheetsList == null ? null : datasheetsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            clearRightPanel();
            return;
        }

        String datasheetId = s(getAny(selected, "id", "datasheet_id"));
        if (datasheetId.isBlank()) {
            clearRightPanel();
            return;
        }

        try {
            DatasheetBundle bundle = StaticDataService.getDatasheetBundle(datasheetId);
            if (bundle == null) {
                clearRightPanel();
                return;
            }
            renderBundle(bundle);
        } catch (SQLException e) {
            showError("Read datasheet failed: " + datasheetId, e);
        }
    }

    private void renderBundle(DatasheetBundle bundle) {
        // Datasheet Name
        if (datasheetName != null) {
            datasheetName.setText(s(getAny(bundle.datasheet, "name", "datasheet_name", "title")));
        }

        // Models & stats
        Object model1 = (bundle.models != null && bundle.models.size() > 0) ? bundle.models.get(0) : null;
        Object model2 = (bundle.models != null && bundle.models.size() > 1) ? bundle.models.get(1) : null;

        boolean hasModel2 = (model2 != null);

        if (unitName1 != null) unitName1.setText(model1 == null ? "" : s(getAny(model1, "name", "model", "unit_name")));
        fillStats(model1, unit1MLabel, unit1TLabel, unit1SvLabel, unit1WLabel, unit1LdLabel, unit1OcLabel);

        if (unitName2 != null) unitName2.setText(hasModel2 ? s(getAny(model2, "name", "model", "unit_name")) : "");
        fillStats(model2, unit2MLabel, unit2TLabel, unit2SvLabel, unit2WLabel, unit2LdLabel, unit2OcLabel);

        // Hide Unit2 panel if only one unit
        setVisibleManaged(unit2PropertyHBox, hasModel2);
        setVisibleManaged(unitName2, hasModel2);

        // Text sections
        setFlow(unitCompositionTextFlow, joinLines(bundle.compositions,
                "text", "composition", "rule", "description", "line_text"));

        // Model cost: description + cost
        setFlow(costTextFlow, buildModelCost(bundle.costs));

        // Keywords: comma-separated
        setFlow(keywordsTextFlow, joinKeywordsComma(bundle.keywords));

        // Abilities: show name + description
        setFlow(abilityTextFlow, formatAbilities(bundle.abilities));

        // Faction abilities (detachment abilities): show name + description
        setFlow(factionAbilityTextFlow, formatAbilities(bundle.detachmentAbilities));

        // Other: only show existing parts; if empty, hide TextFlow
        String other = buildOtherSection(bundle);
        setFlow(otherTextFlow, other);
        setVisibleManaged(otherTextFlow, !other.isBlank());

        // Weapons: split melee/ranged, melee range forced to "Melee", name includes description
        List<WeaponRow> ranged = new ArrayList<>();
        List<WeaponRow> melee = new ArrayList<>();

        if (bundle.wargear != null) {
            for (Object w : bundle.wargear) {
                WeaponRow row = WeaponRow.fromWargear(w);
                if (row == null) continue;
                if (row.isMelee) melee.add(row);
                else ranged.add(row);
            }
        }

        if (rangedWeaponTable != null) rangedWeaponTable.getItems().setAll(ranged);
        if (meleeWeaponTable != null) meleeWeaponTable.getItems().setAll(melee);
    }

    private void clearRightPanel() {
        if (datasheetName != null) datasheetName.setText("");
        if (unitName1 != null) unitName1.setText("");
        if (unitName2 != null) unitName2.setText("");

        clearLabels(unit1MLabel, unit1TLabel, unit1SvLabel, unit1WLabel, unit1LdLabel, unit1OcLabel);
        clearLabels(unit2MLabel, unit2TLabel, unit2SvLabel, unit2WLabel, unit2LdLabel, unit2OcLabel);

        setFlow(unitCompositionTextFlow, "");
        setFlow(costTextFlow, "");
        setFlow(keywordsTextFlow, "");
        setFlow(abilityTextFlow, "");
        setFlow(factionAbilityTextFlow, "");
        setFlow(otherTextFlow, "");
        setVisibleManaged(otherTextFlow, false);

        if (rangedWeaponTable != null) rangedWeaponTable.getItems().clear();
        if (meleeWeaponTable != null) meleeWeaponTable.getItems().clear();

        setVisibleManaged(unit2PropertyHBox, false);
        setVisibleManaged(unitName2, false);
    }

    // -------------------- Formatting helpers --------------------

    private String joinKeywordsComma(List<?> keywords) {
        if (keywords == null || keywords.isEmpty()) return "";
        List<String> ks = new ArrayList<>();
        for (Object k : keywords) {
            String kw = s(getAny(k, "keyword", "name", "text"));
            if (!kw.isBlank()) ks.add(kw);
        }
        return String.join(", ", ks);
    }

    private String formatAbilities(List<?> list) {
        if (list == null || list.isEmpty()) return "";

        List<String> out = new ArrayList<>();
        for (Object a : list) {
            String name = s(getAny(a, "name", "ability"));
            String text = s(getAny(a, "text", "description", "line_text"));

            if (!name.isBlank() && !text.isBlank()) out.add(name + ": " + text);
            else if (!name.isBlank()) out.add(name);
            else if (!text.isBlank()) out.add(text);
        }
        return String.join("\n", out);
    }

    private String buildModelCost(List<?> costs) {
        if (costs == null || costs.isEmpty()) return "";

        List<String> lines = new ArrayList<>();
        for (Object c : costs) {
            String desc = s(getAny(c, "model", "description", "text", "line_text", "name"));
            String cost = s(getAny(c, "cost", "points", "value"));

            if (desc.isBlank() && cost.isBlank()) continue;

            if (!desc.isBlank() && !cost.isBlank()) lines.add(desc + " " + cost);
            else if (!desc.isBlank()) lines.add(desc);
            else lines.add(cost);
        }
        return String.join("\n", lines);
    }

    private String buildOtherSection(DatasheetBundle bundle) {
        List<String> sections = new ArrayList<>();

        String leadersText = joinLines(bundle.leaders, "text", "rule", "description", "line_text", "name");
        if (!leadersText.isBlank()) sections.add("ATTACHED UNIT\n" + leadersText);

        String optionsText = joinLines(bundle.options, "text", "option", "description", "line_text", "name");
        if (!optionsText.isBlank()) sections.add("OPTIONS\n" + optionsText);

        String stratsText = joinLines(bundle.stratagems, "text", "name", "description", "line_text");
        if (!stratsText.isBlank()) sections.add("STRATAGEMS\n" + stratsText);

        String enhText = joinLines(bundle.enhancements, "text", "name", "description", "line_text");
        if (!enhText.isBlank()) sections.add("ENHANCEMENTS\n" + enhText);

        return String.join("\n\n", sections).trim();
    }

    private String joinLines(List<?> items, String... fields) {
        if (items == null || items.isEmpty()) return "";
        List<String> out = new ArrayList<>();
        for (Object x : items) {
            // Prefer combining name + text if both exist
            String name = s(getAny(x, "name", "ability", "title"));
            String text = s(getAny(x, "text", "description", "line_text"));
            if (!name.isBlank() && !text.isBlank()) {
                out.add(name + ": " + text);
                continue;
            }

            String line = "";
            for (String f : fields) {
                String v = s(getAny(x, f));
                if (!v.isBlank()) { line = v; break; }
            }
            if (!line.isBlank()) out.add(line);
        }
        return String.join("\n", out);
    }

    private void setFlow(TextFlow flow, String text) {
        if (flow == null) return;
        flow.getChildren().clear();
        flow.getChildren().add(new Text(text == null ? "" : text));
    }

    private void setVisibleManaged(Node node, boolean visible) {
        if (node == null) return;
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void clearLabels(Label... labels) {
        for (Label l : labels) if (l != null) l.setText("");
    }

    private void fillStats(Object model, Label m, Label t, Label sv, Label w, Label ld, Label oc) {
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

    private void showError(String title, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    private static String s(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    /**
     * Read value from record/accessor via reflection.
     */
    private static Object getAny(Object obj, String... methodNames) {
        if (obj == null) return null;
        for (String name : methodNames) {
            try {
                Method m = obj.getClass().getMethod(name);
                return m.invoke(obj);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    // -------------------- Weapon row --------------------

    public static final class WeaponRow {
        final SimpleStringProperty displayName = new SimpleStringProperty("");
        final SimpleStringProperty range = new SimpleStringProperty("");
        final SimpleStringProperty a = new SimpleStringProperty("");
        final SimpleStringProperty skill = new SimpleStringProperty("");
        final SimpleStringProperty ap = new SimpleStringProperty("");
        final SimpleStringProperty d = new SimpleStringProperty("");
        final boolean isMelee;

        private WeaponRow(boolean isMelee) {
            this.isMelee = isMelee;
        }

        static WeaponRow fromWargear(Object w) {
            if (w == null) return null;

            String name = s(getAny(w, "wargear", "weapon", "name"));
            String desc = s(getAny(w, "description", "desc", "text", "line_text", "special_rules"));

            String range = s(getAny(w, "range", "weapon_range"));
            String attacks = s(getAny(w, "a", "attacks", "A"));
            String bs = s(getAny(w, "bs", "BS"));
            String ws = s(getAny(w, "ws", "WS"));
            String ap = s(getAny(w, "ap", "AP"));
            String dmg = s(getAny(w, "d", "damage", "D"));

            String type = s(getAny(w, "type", "category", "weapon_type", "profile_type")).toLowerCase();

            boolean melee = type.contains("melee")
                    || range.toLowerCase().contains("melee")
                    || (range.isBlank() && !ws.isBlank());

            WeaponRow row = new WeaponRow(melee);

            // Weapon name + description
            String combined = name;
            if (!desc.isBlank()) combined = name + "\n[" + desc + "]";
            row.displayName.set(combined);

            // Melee range always "Melee"
            row.range.set(melee ? "Melee" : range);

            row.a.set(attacks);
            row.skill.set(melee ? ws : bs);
            row.ap.set(ap);
            row.d.set(dmg);

            return row;
        }
    }
}
