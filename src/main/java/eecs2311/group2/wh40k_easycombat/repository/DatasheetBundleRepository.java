package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.db.Tx;
import eecs2311.group2.wh40k_easycombat.model.Datasheets;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_abilities;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_detachment_abilities;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_enhancements;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_keywords;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_leader;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models_cost;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_options;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_stratagems;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_unit_composition;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_wargear;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class DatasheetBundleRepository {

    private static final Comparator<String> NULL_SAFE_TEXT_ORDER =
            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER);

    public record DatasheetRecordBundle(
            Datasheets datasheet,
            List<Datasheets_models> models,
            List<Datasheets_wargear> wargear,
            List<Datasheets_abilities> abilities,
            List<Datasheets_unit_composition> compositions,
            List<Datasheets_models_cost> costs,
            List<Datasheets_keywords> keywords,
            List<Datasheets_options> options,
            List<Datasheets_leader> leaders,
            List<Datasheets_stratagems> stratagems,
            List<Datasheets_enhancements> enhancements,
            List<Datasheets_detachment_abilities> detachmentAbilities
    ) {
        public DatasheetRecordBundle {
            Objects.requireNonNull(datasheet, "datasheet must not be null");
            models = immutableList(models);
            wargear = immutableList(wargear);
            abilities = immutableList(abilities);
            compositions = immutableList(compositions);
            costs = immutableList(costs);
            keywords = immutableList(keywords);
            options = immutableList(options);
            leaders = immutableList(leaders);
            stratagems = immutableList(stratagems);
            enhancements = immutableList(enhancements);
            detachmentAbilities = immutableList(detachmentAbilities);
        }
    }

    public static List<DatasheetRecordBundle> findAllBundles() throws SQLException {
        Map<String, DatasheetAccumulator> byId = new LinkedHashMap<>();

        for (Datasheets datasheet : DatasheetsRepository.getAllDatasheets()) {
            if (datasheet == null || isBlank(datasheet.id())) {
                continue;
            }
            byId.put(datasheet.id(), new DatasheetAccumulator(datasheet));
        }

        attach(byId, Datasheets_modelsRepository.getAllDatasheets_models(),
                Datasheets_models::datasheet_id, (acc, row) -> acc.models.add(row));
        attach(byId, Datasheets_wargearRepository.getAllDatasheets_wargear(),
                Datasheets_wargear::datasheet_id, (acc, row) -> acc.wargear.add(row));
        attach(byId, Datasheets_abilitiesRepository.getAllDatasheets_abilities(),
                Datasheets_abilities::datasheet_id, (acc, row) -> acc.abilities.add(row));
        attach(byId, Datasheets_unit_compositionRepository.getAllDatasheets_unit_composition(),
                Datasheets_unit_composition::datasheet_id, (acc, row) -> acc.compositions.add(row));
        attach(byId, Datasheets_models_costRepository.getAllDatasheets_models_cost(),
                Datasheets_models_cost::datasheet_id, (acc, row) -> acc.costs.add(row));
        attach(byId, Datasheets_keywordsRepository.getAllDatasheets_keywords(),
                Datasheets_keywords::datasheet_id, (acc, row) -> acc.keywords.add(row));
        attach(byId, Datasheets_optionsRepository.getAllDatasheets_options(),
                Datasheets_options::datasheet_id, (acc, row) -> acc.options.add(row));
        attach(byId, Datasheets_leaderRepository.getAllDatasheets_leader(),
                Datasheets_leader::attached_id, (acc, row) -> acc.leaders.add(row));
        attach(byId, Datasheets_stratagemsRepository.getAllDatasheets_stratagems(),
                Datasheets_stratagems::datasheet_id, (acc, row) -> acc.stratagems.add(row));
        attach(byId, Datasheets_enhancementsRepository.getAllDatasheets_enhancements(),
                Datasheets_enhancements::datasheet_id, (acc, row) -> acc.enhancements.add(row));
        attach(byId, Datasheets_detachment_abilitiesRepository.getAllDatasheets_detachment_abilities(),
                Datasheets_detachment_abilities::datasheet_id, (acc, row) -> acc.detachmentAbilities.add(row));

        List<DatasheetRecordBundle> result = new ArrayList<>(byId.size());
        for (DatasheetAccumulator accumulator : byId.values()) {
            result.add(accumulator.toBundle());
        }

        return List.copyOf(result);
    }

    public static void saveBundle(DatasheetRecordBundle bundle) throws SQLException {
        validate(bundle);

        runInTransaction(conn -> {
            updateDatasheet(conn, bundle.datasheet());
            deleteChildrenByDatasheetId(conn, bundle.datasheet().id());
            insertChildren(conn, bundle);
        });
    }

    private static void updateDatasheet(Connection conn, Datasheets datasheet) throws SQLException {
        Dao.update(conn,
                "UPDATE Datasheets SET " +
                        "name=?, faction_id=?, source_id=?, legend=?, role=?, loadout=?, transport=?, virtual=?, " +
                        "leader_head=?, leader_footer=?, damaged_w=?, damaged_description=?, link=? " +
                        "WHERE id=?",
                datasheet.name(),
                datasheet.faction_id(),
                datasheet.source_id(),
                datasheet.legend(),
                datasheet.role(),
                datasheet.loadout(),
                datasheet.transport(),
                datasheet.virtual(),
                datasheet.leader_head(),
                datasheet.leader_footer(),
                datasheet.damaged_w(),
                datasheet.damaged_description(),
                datasheet.link(),
                datasheet.id()
        );
    }

    private static void deleteChildrenByDatasheetId(Connection conn, String datasheetId) throws SQLException {
        Dao.update(conn, "DELETE FROM Datasheets_models WHERE datasheet_id = ?", datasheetId);
        Dao.update(conn, "DELETE FROM Datasheets_models_cost WHERE datasheet_id = ?", datasheetId);
        Dao.update(conn, "DELETE FROM Datasheets_unit_composition WHERE datasheet_id = ?", datasheetId);
        Dao.update(conn, "DELETE FROM Datasheets_wargear WHERE datasheet_id = ?", datasheetId);
        Dao.update(conn, "DELETE FROM Datasheets_abilities WHERE datasheet_id = ?", datasheetId);
        Dao.update(conn, "DELETE FROM Datasheets_keywords WHERE datasheet_id = ?", datasheetId);
        Dao.update(conn, "DELETE FROM Datasheets_options WHERE datasheet_id = ?", datasheetId);
        Dao.update(conn, "DELETE FROM Datasheets_stratagems WHERE datasheet_id = ?", datasheetId);
        Dao.update(conn, "DELETE FROM Datasheets_enhancements WHERE datasheet_id = ?", datasheetId);
        Dao.update(conn, "DELETE FROM Datasheets_detachment_abilities WHERE datasheet_id = ?", datasheetId);
        Dao.update(conn, "DELETE FROM Datasheets_leader WHERE attached_id = ?", datasheetId);
    }

    private static void insertChildren(Connection conn, DatasheetRecordBundle bundle) throws SQLException {
        for (Datasheets_models row : bundle.models()) {
            Dao.update(conn,
                    "INSERT INTO Datasheets_models " +
                            "(datasheet_id, line, name, M, T, Sv, inv_sv, inv_sv_descr, W, Ld, OC, base_size, base_size_descr) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    row.datasheet_id(), row.line(), row.name(), row.M(), row.T(),
                    row.Sv(), row.inv_sv(), row.inv_sv_descr(), row.W(),
                    row.Ld(), row.OC(), row.base_size(), row.base_size_descr()
            );
        }

        for (Datasheets_models_cost row : bundle.costs()) {
            Dao.update(conn,
                    "INSERT INTO Datasheets_models_cost (datasheet_id, line, description, cost) VALUES (?, ?, ?, ?)",
                    row.datasheet_id(), row.line(), row.description(), row.cost()
            );
        }

        for (Datasheets_unit_composition row : bundle.compositions()) {
            Dao.update(conn,
                    "INSERT INTO Datasheets_unit_composition (datasheet_id, line, description) VALUES (?, ?, ?)",
                    row.datasheet_id(), row.line(), row.description()
            );
        }

        for (Datasheets_wargear row : bundle.wargear()) {
            Dao.update(conn,
                    "INSERT INTO Datasheets_wargear " +
                            "(datasheet_id, line, line_in_wargear, dice, name, description, range, type, A, BS_WS, S, AP, D) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    row.datasheet_id(), row.line(), row.line_in_wargear(),
                    row.dice(), row.name(), row.description(),
                    row.range(), row.type(), row.A(),
                    row.BS_WS(), row.S(), row.AP(), row.D()
            );
        }

        for (Datasheets_abilities row : bundle.abilities()) {
            Dao.update(conn,
                    "INSERT INTO Datasheets_abilities " +
                            "(datasheet_id, line, ability_id, model, name, description, type, parameter) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    row.datasheet_id(), row.line(), row.ability_id(), row.model(),
                    row.name(), row.description(), row.type(), row.parameter()
            );
        }

        for (Datasheets_keywords row : bundle.keywords()) {
            Dao.update(conn,
                    "INSERT INTO Datasheets_keywords (datasheet_id, keyword, model, is_faction_keyword) VALUES (?, ?, ?, ?)",
                    row.datasheet_id(), row.keyword(), row.model(), row.is_faction_keyword()
            );
        }

        for (Datasheets_options row : bundle.options()) {
            Dao.update(conn,
                    "INSERT INTO Datasheets_options (datasheet_id, line, button, description) VALUES (?, ?, ?, ?)",
                    row.datasheet_id(), row.line(), row.button(), row.description()
            );
        }

        for (Datasheets_stratagems row : bundle.stratagems()) {
            Dao.update(conn,
                    "INSERT INTO Datasheets_stratagems (datasheet_id, stratagem_id) VALUES (?, ?)",
                    row.datasheet_id(), row.stratagem_id()
            );
        }

        for (Datasheets_enhancements row : bundle.enhancements()) {
            Dao.update(conn,
                    "INSERT INTO Datasheets_enhancements (datasheet_id, enhancement_id) VALUES (?, ?)",
                    row.datasheet_id(), row.enhancement_id()
            );
        }

        for (Datasheets_detachment_abilities row : bundle.detachmentAbilities()) {
            Dao.update(conn,
                    "INSERT INTO Datasheets_detachment_abilities (datasheet_id, detachment_ability_id) VALUES (?, ?)",
                    row.datasheet_id(), row.detachment_ability_id()
            );
        }

        for (Datasheets_leader row : bundle.leaders()) {
            Dao.update(conn,
                    "INSERT INTO Datasheets_leader (leader_id, attached_id) VALUES (?, ?)",
                    row.leader_id(), row.attached_id()
            );
        }
    }

    private static <T> void attach(
            Map<String, DatasheetAccumulator> byId,
            List<T> rows,
            Function<T, String> datasheetIdExtractor,
            BiConsumer<DatasheetAccumulator, T> rowConsumer
    ) {
        for (T row : rows) {
            if (row == null) {
                continue;
            }

            String datasheetId = datasheetIdExtractor.apply(row);
            if (isBlank(datasheetId)) {
                continue;
            }

            DatasheetAccumulator accumulator = byId.get(datasheetId);
            if (accumulator != null) {
                rowConsumer.accept(accumulator, row);
            }
        }
    }

    private static void validate(DatasheetRecordBundle bundle) {
        if (bundle == null || bundle.datasheet() == null || isBlank(bundle.datasheet().id())) {
            throw new IllegalArgumentException("bundle/datasheet/id must not be null");
        }
    }

    private static int safeLineInt(String value) {
        if (value == null) {
            return Integer.MAX_VALUE;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return Integer.MAX_VALUE;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static <T> List<T> immutableList(List<T> rows) {
        return rows == null ? List.of() : List.copyOf(rows);
    }

    private static void runInTransaction(SqlWork work) throws SQLException {
        try {
            Tx.run(conn -> {
                try {
                    work.run(conn);
                    return null;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException sqlException) {
                throw sqlException;
            }
            throw e;
        }
    }

    @FunctionalInterface
    private interface SqlWork {
        void run(Connection conn) throws SQLException;
    }

    private static final class DatasheetAccumulator {
        private final Datasheets datasheet;
        private final List<Datasheets_models> models = new ArrayList<>();
        private final List<Datasheets_wargear> wargear = new ArrayList<>();
        private final List<Datasheets_abilities> abilities = new ArrayList<>();
        private final List<Datasheets_unit_composition> compositions = new ArrayList<>();
        private final List<Datasheets_models_cost> costs = new ArrayList<>();
        private final List<Datasheets_keywords> keywords = new ArrayList<>();
        private final List<Datasheets_options> options = new ArrayList<>();
        private final List<Datasheets_leader> leaders = new ArrayList<>();
        private final List<Datasheets_stratagems> stratagems = new ArrayList<>();
        private final List<Datasheets_enhancements> enhancements = new ArrayList<>();
        private final List<Datasheets_detachment_abilities> detachmentAbilities = new ArrayList<>();

        private DatasheetAccumulator(Datasheets datasheet) {
            this.datasheet = datasheet;
        }

        private DatasheetRecordBundle toBundle() {
            models.sort(Comparator.comparingInt(row -> safeLineInt(row.line())));
            wargear.sort(Comparator
                    .comparingInt((Datasheets_wargear row) -> safeLineInt(row.line()))
                    .thenComparingInt(row -> safeLineInt(row.line_in_wargear())));
            abilities.sort(Comparator.comparingInt(row -> safeLineInt(row.line())));
            compositions.sort(Comparator.comparingInt(row -> safeLineInt(row.line())));
            costs.sort(Comparator.comparingInt(row -> safeLineInt(row.line())));
            keywords.sort(Comparator
                    .comparing(Datasheets_keywords::keyword, NULL_SAFE_TEXT_ORDER)
                    .thenComparing(Datasheets_keywords::model, NULL_SAFE_TEXT_ORDER));
            options.sort(Comparator.comparingInt(row -> safeLineInt(row.line())));
            leaders.sort(Comparator.comparing(Datasheets_leader::leader_id, NULL_SAFE_TEXT_ORDER));
            stratagems.sort(Comparator.comparing(Datasheets_stratagems::stratagem_id, NULL_SAFE_TEXT_ORDER));
            enhancements.sort(Comparator.comparing(Datasheets_enhancements::enhancement_id, NULL_SAFE_TEXT_ORDER));
            detachmentAbilities.sort(Comparator.comparing(
                    Datasheets_detachment_abilities::detachment_ability_id,
                    NULL_SAFE_TEXT_ORDER
            ));

            return new DatasheetRecordBundle(
                    datasheet,
                    models,
                    wargear,
                    abilities,
                    compositions,
                    costs,
                    keywords,
                    options,
                    leaders,
                    stratagems,
                    enhancements,
                    detachmentAbilities
            );
        }
    }

    private DatasheetBundleRepository() {
    }
}