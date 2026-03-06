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
    @FXML private Label InvsvLabel;
    @FXML private Label insvTxtLabel;

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

    // Optional master abilities lookup if datasheets_abilities only stores ability_id
    private final Map<String, Object> abilitiesById = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
        ensurePropertyLabelsWhiteBackground();
        loadAbilitiesMaster();
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

    private void ensurePropertyLabelsWhiteBackground() {
        applyWhiteBackground(unit1MLabel);
        applyWhiteBackground(unit1TLabel);
        applyWhiteBackground(unit1SvLabel);
        applyWhiteBackground(unit1WLabel);
        applyWhiteBackground(unit1OcLabel);
        applyWhiteBackground(unit1LdLabel);
        applyWhiteBackground(InvsvLabel);

        applyWhiteBackground(unit2MLabel);
        applyWhiteBackground(unit2TLabel);
        applyWhiteBackground(unit2SvLabel);
        applyWhiteBackground(unit2WLabel);
        applyWhiteBackground(unit2OcLabel);
        applyWhiteBackground(unit2LdLabel);

        applyWhiteBackground(insvTxtLabel);
    }

    private void applyWhiteBackground(Label label) {
        if (label == null) return;

        String style = label.getStyle();
        if (style == null) style = "";

        style = style.replaceAll("-fx-background-color\\s*:[^;]*;?", "").trim();

        if (!style.isBlank() && !style.endsWith(";")) {
            style += ";";
        }
        style += "-fx-background-color: white;";

        label.setStyle(style);
    }

    // -------------------- Data loading --------------------

    private void loadAbilitiesMaster() {
        abilitiesById.clear();

        try {
            Class<?> repo = Class.forName("eecs2311.group2.wh40k_easycombat.repository.AbilitiesRepository");
            Method m = repo.getMethod("getAllAbilities");
            Object result = m.invoke(null);

            if (result instanceof List<?> list) {
                for (Object a : list) {
                    String id = s(getAny(a, "id"));
                    if (!id.isBlank()) {
                        abilitiesById.put(id, a);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void loadDatasheetsAndFactions() {
        try {
            StaticDataService.loadAllFromSqlite();

            List<?> list = eecs2311.group2.wh40k_easycombat.repository.DatasheetsRepository.getAllDatasheets();
            allDatasheets.setAll((Collection<? extends Object>) list);
            filteredDatasheets.setAll(allDatasheets);

            if (datasheetsList != null) {
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
            }

            loadFactionsIntoComboBox();

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
        }

        if (loadedFromFactionRepo) {
            List<String> names = new ArrayList<>(factionNameToId.keySet());
            Collections.sort(names);
            factionComboBox.getItems().addAll(names);
            factionComboBox.setValue("ALL");
            return;
        }

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

        if (factionComboBox != null) {
            factionComboBox.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldV, newV) -> applyFilters());
        }

        if (searchButton != null) {
            searchButton.setOnMouseClicked(e -> applyFilters());
        }
    }

    @FXML
    private void selectFaction(ActionEvent event) {
        applyFilters();
    }

    @FXML
    void changeFaction(InputMethodEvent event) {
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
        if (datasheetName != null) {
            datasheetName.setText(s(getAny(bundle.datasheet, "name", "datasheet_name", "title")));
        }

        Object model1 = (bundle.models != null && bundle.models.size() > 0) ? bundle.models.get(0) : null;
        Object model2 = (bundle.models != null && bundle.models.size() > 1) ? bundle.models.get(1) : null;

        boolean hasModel2 = (model2 != null);

        if (unitName1 != null) {
            String model1Name = model1 == null ? "" : s(getAny(model1, "name", "model", "unit_name"));
            unitName1.setText(hasModel2 ? model1Name : "");
            setVisibleManaged(unitName1, hasModel2);
        }

        fillStats(model1, unit1MLabel, unit1TLabel, unit1SvLabel, unit1WLabel, unit1LdLabel, unit1OcLabel);
        updateInvSv(model1);

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

        String normalAbilities = formatNonFactionAbilities(bundle.abilities);
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

        clearLabels(unit1MLabel, unit1TLabel, unit1SvLabel, unit1WLabel, unit1LdLabel, unit1OcLabel, InvsvLabel);
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
        setVisibleManaged(InvsvLabel, false);
        setVisibleManaged(insvTxtLabel, false);
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

    private String formatNonFactionAbilities(List<?> list) {
        if (list == null || list.isEmpty()) return "";

        List<String> coreNames = new ArrayList<>();
        List<String> otherAbilities = new ArrayList<>();

        Set<String> seenCore = new LinkedHashSet<>();
        Set<String> seenOther = new LinkedHashSet<>();

        for (Object a : list) {
            String type = s(getAny(a, "type")).trim().toLowerCase();

            if (type.contains("faction")) continue;

            // core: same line, comma separated
            if (type.contains("core")) {
                String name = resolveAbilityName(a);
                if (!name.isBlank() && seenCore.add(name)) {
                    coreNames.add(name);
                }
                continue;
            }

            String line = resolveAbilityFullTextForDisplay(a);
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

    private String formatFactionAbilityNames(List<?> abilities, List<?> detachmentAbilities) {
        List<String> out = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        if (abilities != null) {
            for (Object a : abilities) {
                String type = s(getAny(a, "type")).trim().toLowerCase();
                if (!type.contains("faction")) continue;

                String name = resolveAbilityName(a);
                if (!name.isBlank() && seen.add(name)) {
                    out.add("<b>" + name + "</b>");
                }
            }
        }

        if (detachmentAbilities != null) {
            for (Object a : detachmentAbilities) {
                String name = s(getAny(a,
                        "name",
                        "ability",
                        "detachment_ability",
                        "title",
                        "rule"));

                if (!name.isBlank() && seen.add(name)) {
                    out.add("<b>" + name + "</b>");
                }
            }
        }

        return String.join("\n", out);
    }

    private String resolveAbilityFullTextForDisplay(Object datasheetAbility) {
        if (datasheetAbility == null) return "";

        String localName = s(getAny(datasheetAbility, "name"));
        String localDesc = s(getAny(datasheetAbility, "description"));
        String abilityId = s(getAny(datasheetAbility, "ability_id"));

        Object master = abilityId.isBlank() ? null : abilitiesById.get(abilityId);
        String masterName = s(getAny(master, "name"));
        String masterDesc = s(getAny(master, "description"));

        String name = !localName.isBlank() ? localName : masterName;
        String desc = !localDesc.isBlank() ? localDesc : masterDesc;

        if (!name.isBlank() && !desc.isBlank()) return "<b>" + name + ":</b> " + desc;
        if (!name.isBlank()) return "<b>" + name + "</b>";
        if (!desc.isBlank()) return desc;

        return "";
    }
    
    @SuppressWarnings("unused")
	private String resolveAbilityFullText(Object datasheetAbility) {
        if (datasheetAbility == null) return "";

        String localName = s(getAny(datasheetAbility, "name"));
        String localDesc = s(getAny(datasheetAbility, "description"));
        String abilityId = s(getAny(datasheetAbility, "ability_id"));

        Object master = abilityId.isBlank() ? null : abilitiesById.get(abilityId);
        String masterName = s(getAny(master, "name"));
        String masterDesc = s(getAny(master, "description"));

        String name = !localName.isBlank() ? localName : masterName;
        String desc = !localDesc.isBlank() ? localDesc : masterDesc;

        if (!name.isBlank() && !desc.isBlank()) return name + ": " + desc;
        if (!name.isBlank()) return name;
        if (!desc.isBlank()) return desc;

        return "";
    }

    private String resolveAbilityName(Object datasheetAbility) {
        if (datasheetAbility == null) return "";

        String localName = s(getAny(datasheetAbility, "name"));
        if (!localName.isBlank()) return localName;

        String abilityId = s(getAny(datasheetAbility, "ability_id"));
        if (abilityId.isBlank()) return "";

        Object master = abilitiesById.get(abilityId);
        return s(getAny(master, "name"));
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
            String name = s(getAny(x, "name", "ability", "title"));
            String text = s(getAny(x, "text", "description", "line_text"));

            if (!name.isBlank() && !text.isBlank()) {
                out.add(name + ": " + text);
                continue;
            }

            String line = "";
            for (String f : fields) {
                String v = s(getAny(x, f));
                if (!v.isBlank()) {
                    line = v;
                    break;
                }
            }

            if (!line.isBlank()) out.add(line);
        }

        return String.join("\n", out);
    }

    private void setFlow(TextFlow flow, String text) {
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

    private void addFormattedLine(TextFlow flow, String line) {
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

    private String htmlToPlainText(String html) {
        if (html == null || html.isBlank()) return "";

        String s = html;

        // remove list markers
        s = s.replaceAll("(?i)<li[^>]*>", "• ");
        s = s.replaceAll("(?i)</li>", " ");

        // remove links but keep link text
        s = s.replaceAll("(?i)<a[^>]*>", "");
        s = s.replaceAll("(?i)</a>", "");

        // keep <b> but remove other tags
        s = s.replaceAll("(?is)<(?!/?b\\b)[^>]+>", "");

        // decode html entities
        s = s.replace("&nbsp;", " ");
        s = s.replace("&lt;", "<");
        s = s.replace("&gt;", ">");
        s = s.replace("&amp;", "&");
        s = s.replace("&quot;", "\"");
        s = s.replace("&#39;", "'");

        // normalize whitespace
        s = s.replace("\r", "");
        s = s.replaceAll("\\s+", " ");

        return s.trim();
    }

    private void setVisibleManaged(Node node, boolean visible) {
        if (node == null) return;
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void clearLabels(Label... labels) {
        for (Label l : labels) {
            if (l != null) l.setText("");
        }
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

    private void updateInvSv(Object model) {
        String inv = model == null ? "" : s(getAny(model, "inv_sv", "inv", "invSave", "invulnerable_save"));

        boolean visible = !inv.isBlank() && !"-".equals(inv);

        if (InvsvLabel != null) {
            InvsvLabel.setText(visible ? inv : "");
        }

        setVisibleManaged(InvsvLabel, visible);
        setVisibleManaged(insvTxtLabel, visible);
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

            String combined = name;
            if (!desc.isBlank()) combined = name + "\n[" + desc + "]";
            row.displayName.set(combined);

            row.range.set(melee ? "Melee" : range);

            row.a.set(attacks);
            row.skill.set(melee ? ws : bs);
            row.ap.set(ap);
            row.d.set(dmg);

            return row;
        }
    }
}