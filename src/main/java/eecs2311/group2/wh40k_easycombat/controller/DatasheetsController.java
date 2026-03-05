package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.service.StaticDataService;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService.DatasheetBundle;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
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
    @FXML private ChoiceBox<String> factionChoiceBox;

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
    @FXML private Label unit2TLabel;
    @FXML private Label unit2MLabel;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
        loadDatasheetsAndFactions();
        wireEvents();
    }

    // ---------- 1) 表格列绑定 + 武器名自动换行 ----------
    private void setupTables() {
        // ranged
        rangedWeaponName.setCellValueFactory(c -> c.getValue().displayName);
        rangedWeaponRange.setCellValueFactory(c -> c.getValue().range);
        rangedWeaponA.setCellValueFactory(c -> c.getValue().a);
        rangedWeaponBS.setCellValueFactory(c -> c.getValue().skill);
        rangedWeaponAP.setCellValueFactory(c -> c.getValue().ap);
        rangedWeaponD.setCellValueFactory(c -> c.getValue().d);

        // melee
        meleeWeaponName.setCellValueFactory(c -> c.getValue().displayName);
        meleeWeaponRange.setCellValueFactory(c -> c.getValue().range);
        meleeWeaponA.setCellValueFactory(c -> c.getValue().a);
        meleeWeaponWS.setCellValueFactory(c -> c.getValue().skill);
        meleeWeaponAP.setCellValueFactory(c -> c.getValue().ap);
        meleeWeaponD.setCellValueFactory(c -> c.getValue().d);

        // 让武器名（含description）可以换行显示
        applyWrapCellFactory(rangedWeaponName);
        applyWrapCellFactory(meleeWeaponName);

        rangedWeaponTable.setItems(FXCollections.observableArrayList());
        meleeWeaponTable.setItems(FXCollections.observableArrayList());
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

    // ---------- 2) 从 sqlite 读取所有 datasheets + faction ----------
    private void loadDatasheetsAndFactions() {
        try {
            StaticDataService.loadAllFromSqlite();

            // 这里用 Repository 拿列表（如果你后面愿意，我也可以教你改成 Service 暴露 getAllDatasheets）
            List<?> list = eecs2311.group2.wh40k_easycombat.repository.DatasheetsRepository.getAllDatasheets();

            allDatasheets.setAll((Collection<? extends Object>) list);
            filteredDatasheets.setAll(allDatasheets);

            datasheetsList.setItems(filteredDatasheets);
            datasheetsList.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); return; }
                    String name = s(getAny(item, "name", "datasheet_name", "title"));
                    if (name.isBlank()) name = s(getAny(item, "id", "datasheet_id"));
                    setText(name);
                }
            });

            // factions
            Set<String> factions = allDatasheets.stream()
                    .map(d -> s(getAny(d, "faction", "faction_name", "army")))
                    .filter(x -> !x.isBlank())
                    .collect(Collectors.toCollection(TreeSet::new));

            factionChoiceBox.getItems().clear();
            factionChoiceBox.getItems().add("ALL");
            factionChoiceBox.getItems().addAll(factions);
            factionChoiceBox.setValue("ALL");

            if (!filteredDatasheets.isEmpty()) {
                datasheetsList.getSelectionModel().select(0);
                showSelectedDatasheet();
            }
        } catch (Exception e) {
            showError("Load data failed", e);
        }
    }

    // ---------- 3) 事件绑定 ----------
    private void wireEvents() {
        datasheetsList.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldV, newV) -> showSelectedDatasheet());

        factionChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldV, newV) -> applyFilters());

        searchButton.setOnMouseClicked(e -> applyFilters());
    }

    private void applyFilters() {
        String faction = factionChoiceBox.getValue() == null ? "ALL" : factionChoiceBox.getValue();
        String keyword = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();

        filteredDatasheets.setAll(allDatasheets.stream()
                .filter(d -> {
                    if (!"ALL".equalsIgnoreCase(faction)) {
                        String f = s(getAny(d, "faction", "faction_name", "army"));
                        if (!f.equalsIgnoreCase(faction)) return false;
                    }
                    if (!keyword.isBlank()) {
                        String name = s(getAny(d, "name", "datasheet_name", "title")).toLowerCase();
                        String id = s(getAny(d, "id", "datasheet_id")).toLowerCase();
                        return name.contains(keyword) || id.contains(keyword);
                    }
                    return true;
                })
                .toList());

        if (!filteredDatasheets.isEmpty()) datasheetsList.getSelectionModel().select(0);
        else clearRightPanel();
    }

    // ---------- 4) 显示选中的 datasheet ----------
    private void showSelectedDatasheet() {
        Object selected = datasheetsList.getSelectionModel().getSelectedItem();
        if (selected == null) { clearRightPanel(); return; }

        String datasheetId = s(getAny(selected, "id", "datasheet_id"));
        if (datasheetId.isBlank()) { clearRightPanel(); return; }

        try {
            DatasheetBundle bundle = StaticDataService.getDatasheetBundle(datasheetId);
            if (bundle == null) { clearRightPanel(); return; }
            renderBundle(bundle);
        } catch (SQLException e) {
            showError("Read datasheet failed: " + datasheetId, e);
        }
    }

    // ======================= 你要的渲染规则在这里 =======================
    private void renderBundle(DatasheetBundle bundle) {

        // Datasheet Name
        datasheetName.setText(s(getAny(bundle.datasheet, "name", "datasheet_name", "title")));

        // ---- Models：如果只有一个 unit，隐藏 unit2PropertyHBox ----
        Object model1 = bundle.models.size() > 0 ? bundle.models.get(0) : null;
        Object model2 = bundle.models.size() > 1 ? bundle.models.get(1) : null;

        unitName1.setText(model1 == null ? "" : s(getAny(model1, "name", "model", "unit_name")));
        fillStats(model1, unit1MLabel, unit1TLabel, unit1SvLabel, unit1WLabel, unit1LdLabel, unit1OcLabel);

        boolean hasModel2 = (model2 != null);

        unitName2.setText(hasModel2 ? s(getAny(model2, "name", "model", "unit_name")) : "");
        fillStats(model2, unit2MLabel, unit2TLabel, unit2SvLabel, unit2WLabel, unit2LdLabel, unit2OcLabel);

        setVisibleManaged(unit2PropertyHBox, hasModel2);
        setVisibleManaged(unitName2, hasModel2);

        // ---- Text blocks ----
        setFlow(unitCompositionTextFlow, joinLines(bundle.compositions,
                "text", "composition", "rule", "description", "line_text"));

        setFlow(costTextFlow, buildModelCost(bundle.costs));

        setFlow(keywordsTextFlow, joinKeywords(bundle.keywords));

        setFlow(abilityTextFlow, joinLines(bundle.abilities,
                "ability", "name", "text", "description", "line_text"));

        setFlow(factionAbilityTextFlow, joinLines(bundle.detachmentAbilities,
                "ability", "name", "text", "description", "line_text"));

        // ---- Other：你要显示 Attached Unit / 其它信息 ----
        setFlow(otherTextFlow, buildOtherSection(bundle));

        // ---- Weapons：melee / ranged 分开，melee range 固定显示 “Melee”，武器名后带 description ----
        List<WeaponRow> ranged = new ArrayList<>();
        List<WeaponRow> melee = new ArrayList<>();

        for (Object w : bundle.wargear) {
            WeaponRow row = WeaponRow.fromWargear(w);
            if (row == null) continue;

            if (row.isMelee) melee.add(row);
            else ranged.add(row);
        }

        rangedWeaponTable.getItems().setAll(ranged);
        meleeWeaponTable.getItems().setAll(melee);
    }
    
    private String buildModelCost(List<?> costs) {
        if (costs == null || costs.isEmpty()) return "";

        List<String> lines = new ArrayList<>();
        for (Object c : costs) {
            // “描述”优先：model / description / text ...
            String desc = s(getAny(c, "model", "description", "text", "line_text", "name"));
            // “cost”优先：cost / points ...
            String cost = s(getAny(c, "cost", "points", "value"));

            if (desc.isBlank() && cost.isBlank()) continue;

            // ✅ 描述 + cost
            if (!desc.isBlank() && !cost.isBlank()) lines.add(desc + " " + cost);
            else if (!desc.isBlank()) lines.add(desc);
            else lines.add(cost);
        }
        return String.join("\n", lines);
    }

    private String buildOtherSection(DatasheetBundle bundle) {
        List<String> sections = new ArrayList<>();

        // Attached Unit (leaders)
        String leadersText = joinLines(bundle.leaders, "text", "rule", "description", "line_text", "name");
        if (!leadersText.isBlank()) {
            sections.add("ATTACHED UNIT\n" + leadersText);
        }

        // Options
        String optionsText = joinLines(bundle.options, "text", "option", "description", "line_text", "name");
        if (!optionsText.isBlank()) {
            sections.add("OPTIONS\n" + optionsText);
        }

        // Stratagems
        String stratsText = joinLines(bundle.stratagems, "text", "name", "description", "line_text");
        if (!stratsText.isBlank()) {
            sections.add("STRATAGEMS\n" + stratsText);
        }

        // Enhancements
        String enhText = joinLines(bundle.enhancements, "text", "name", "description", "line_text");
        if (!enhText.isBlank()) {
            sections.add("ENHANCEMENTS\n" + enhText);
        }

        return String.join("\n\n", sections).trim();
    }

    private void setVisibleManaged(Node node, boolean visible) {
        if (node == null) return;
        node.setVisible(visible);
        node.setManaged(visible);
    }

    // ======================= Back Button ======================
    @FXML
    void clickBackButton(MouseEvent event) throws IOException {
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/MainUI.fxml",
                1200.0, 800.0);
    }
    
    @FXML
    private void clickSearchButton(MouseEvent event) {
        applyFilters();
    }
    
    @FXML
    private void clickAddButton(MouseEvent event) {

    }

    @FXML
    private void clickEditButton(MouseEvent event) {

    }

    // ======================= Clear UI =========================
    private void clearRightPanel() {
        datasheetName.setText("");
        unitName1.setText("");
        unitName2.setText("");

        clearLabels(unit1MLabel, unit1TLabel, unit1SvLabel, unit1WLabel, unit1LdLabel, unit1OcLabel);
        clearLabels(unit2MLabel, unit2TLabel, unit2SvLabel, unit2WLabel, unit2LdLabel, unit2OcLabel);

        setFlow(unitCompositionTextFlow, "");
        setFlow(costTextFlow, "");
        setFlow(keywordsTextFlow, "");
        setFlow(abilityTextFlow, "");
        setFlow(factionAbilityTextFlow, "");
        setFlow(otherTextFlow, "");

        rangedWeaponTable.getItems().clear();
        meleeWeaponTable.getItems().clear();

        setVisibleManaged(unit2PropertyHBox, false);
        setVisibleManaged(unitName2, false);
    }

    private void clearLabels(Label... labels) {
        for (Label l : labels) if (l != null) l.setText("");
    }

    private void fillStats(Object model, Label m, Label t, Label sv, Label w, Label ld, Label oc) {
        if (model == null) {
            clearLabels(m, t, sv, w, ld, oc);
            return;
        }
        m.setText(s(getAny(model, "m", "move", "movement", "M")));
        t.setText(s(getAny(model, "t", "toughness", "T")));
        sv.setText(s(getAny(model, "sv", "save", "Sv")));
        w.setText(s(getAny(model, "w", "wounds", "W")));
        ld.setText(s(getAny(model, "ld", "leadership", "Ld")));
        oc.setText(s(getAny(model, "oc", "objective_control", "OC")));
    }

    private String joinKeywords(List<?> keywords) {
        if (keywords == null || keywords.isEmpty()) return "";

        List<String> ks = new ArrayList<>();
        for (Object k : keywords) {
            String kw = s(getAny(k, "keyword", "name", "text"));
            if (!kw.isBlank()) ks.add(kw);
        }
        
        return String.join(", ", ks);
    }

    private String joinLines(List<?> items, String... fields) {
        if (items == null || items.isEmpty()) return "";
        List<String> out = new ArrayList<>();
        for (Object x : items) {
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
     * Record/accessor reflection (tolerant to different model field naming)
     */
    private static Object getAny(Object obj, String... methodNames) {
        if (obj == null) return null;
        for (String name : methodNames) {
            try {
                Method m = obj.getClass().getMethod(name);
                return m.invoke(obj);
            } catch (Exception ignored) {}
        }
        return null;
    }

    // ======================= Weapon Row =======================
    public static final class WeaponRow {
        final SimpleStringProperty displayName = new SimpleStringProperty("");
        final SimpleStringProperty range = new SimpleStringProperty("");
        final SimpleStringProperty a = new SimpleStringProperty("");
        final SimpleStringProperty skill = new SimpleStringProperty(""); // BS or WS
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

            // ✅ 你要的：武器名字后面显示 description（建议换行更像 GW）
            String combinedName = name;
            if (!desc.isBlank()) combinedName = name + "\n[" + desc + "]";
            row.displayName.set(combinedName);

            // ✅ 你要的：melee 的 range 列固定显示 “Melee”
            row.range.set(melee ? "Melee" : range);

            row.a.set(attacks);
            row.skill.set(melee ? ws : bs);
            row.ap.set(ap);
            row.d.set(dmg);

            return row;
        }
    }
}