package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.*;
import eecs2311.group2.wh40k_easycombat.repository.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class StaticDataService {
    private static StaticDataService instance;

    private final Map<Integer, Factions> factions = new ConcurrentHashMap<>();
    private final Map<Integer, Units> units = new ConcurrentHashMap<>();
    private final Map<Integer, Detachments> detachments = new ConcurrentHashMap<>();

    private final Map<Integer, RangeWeapons> rangedWeapons = new ConcurrentHashMap<>();
    private final Map<Integer, MeleeWeapons> meleeWeapons = new ConcurrentHashMap<>();

    private final Map<Integer, CoreAbilities> coreAbilities = new ConcurrentHashMap<>();
    private final Map<Integer, OtherAbilities> otherAbilities = new ConcurrentHashMap<>();
    private final Map<Integer, UnitKeywords> unitKeywords = new ConcurrentHashMap<>();
    private final Map<Integer, WeaponKeywords> weaponKeywords = new ConcurrentHashMap<>();

    public static synchronized StaticDataService getInstance() {
        if (instance == null) instance = new StaticDataService();
        return instance;
    }

    private StaticDataService() {
    }

    public void loadAll() {
        clearAllCaches();

        try {
            FactionRepository.getAllFactions().forEach(f -> factions.put(f.id(), f));
            RangeWeaponRepository.getAllRangeWeapons().forEach(w -> rangedWeapons.put(w.id(), w));
            MeleeWeaponRepository.getAllMeleeWeapons().forEach(w -> meleeWeapons.put(w.id(), w));
            CoreAbilityRepository.getAllCoreAbilities().forEach(a -> coreAbilities.put(a.id(), a));
            OtherAbilityRepository.getAllOtherAbilities().forEach(a -> otherAbilities.put(a.id(), a));
            UnitKeywordRepository.getAllUnitKeywords().forEach(k -> unitKeywords.put(k.id(), k));
            WeaponKeywordRepository.getAllWeaponKeywords().forEach(k -> weaponKeywords.put(k.id(), k));

            DetachmentRepository.getAllDetachments().forEach(d -> detachments.put(d.id(), d));
            UnitRepository.getAllUnits().forEach(u -> units.put(u.id(), u));

            System.out.println("StaticDataService: All tables synchronized successfully.");
        } catch (SQLException e) {
            System.err.println("StaticDataService: Failed to load data from database!");
            e.printStackTrace();
        }
    }

    private void clearAllCaches() {
        factions.clear();
        units.clear();
        detachments.clear();
        rangedWeapons.clear();
        meleeWeapons.clear();
        coreAbilities.clear();
        otherAbilities.clear();
        unitKeywords.clear();
        weaponKeywords.clear();
    }

    // --- Factions ---
    public List<Factions> getAllFactions() {
        return new ArrayList<>(factions.values());
    }

    public Optional<Factions> getFactionById(int id) {
        return Optional.ofNullable(factions.get(id));
    }

    public int addFaction(Factions f) throws SQLException {
        int id = FactionRepository.addNewFaction(f);
        factions.put(id, f);
        return id;
    }

    public void updateFaction(Factions f) throws SQLException {
        FactionRepository.updateFaction(f);
        factions.put(f.id(), f);
    }

    public void deleteFaction(Factions f) throws SQLException {
        FactionRepository.deleteFaction(f);
        factions.remove(f.id());
    }

    // --- Units ---
    public List<Units> getAllUnits() {
        return new ArrayList<>(units.values());
    }

    public Optional<Units> getUnitById(int id) {
        return Optional.ofNullable(units.get(id));
    }

    public int addUnit(Units u) throws SQLException {
        int id = UnitRepository.addNewUnit(u);
        units.put(id, u);
        return id;
    }

    public void updateUnit(Units u) throws SQLException {
        UnitRepository.updateUnit(u);
        units.put(u.id(), u);
    }

    public void deleteUnit(Units u) throws SQLException {
        UnitRepository.deleteUnit(u);
        units.remove(u.id());
    }

    // --- Detachments ---
    public List<Detachments> getAllDetachments() {
        return new ArrayList<>(detachments.values());
    }

    public Optional<Detachments> getDetachmentById(int id) {
        return Optional.ofNullable(detachments.get(id));
    }

    public int addDetachment(Detachments d) throws SQLException {
        int id = DetachmentRepository.addNewDetachment(d);
        detachments.put(id, d);
        return id;
    }

    public void updateDetachment(Detachments d) throws SQLException {
        DetachmentRepository.updateDetachment(d);
        detachments.put(d.id(), d);
    }

    public void deleteDetachment(Detachments d) throws SQLException {
        DetachmentRepository.deleteDetachment(d);
        detachments.remove(d.id());
    }

    // --- RangeWeapons ---
    public List<RangeWeapons> getAllRangeWeapons() {
        return new ArrayList<>(rangedWeapons.values());
    }

    public Optional<RangeWeapons> getRangeWeaponById(int id) {
        return Optional.ofNullable(rangedWeapons.get(id));
    }

    public int addRangeWeapon(RangeWeapons w) throws SQLException {
        int id = RangeWeaponRepository.addNewRangeWeapon(w);
        rangedWeapons.put(id, w);
        return id;
    }

    public void updateRangeWeapon(RangeWeapons w) throws SQLException {
        RangeWeaponRepository.updateRangeWeapon(w);
        rangedWeapons.put(w.id(), w);
    }

    public void deleteRangeWeapon(RangeWeapons w) throws SQLException {
        RangeWeaponRepository.deleteRangeWeapon(w);
        rangedWeapons.remove(w.id());
    }

    // --- MeleeWeapons ---
    public List<MeleeWeapons> getAllMeleeWeapons() {
        return new ArrayList<>(meleeWeapons.values());
    }

    public Optional<MeleeWeapons> getMeleeWeaponById(int id) {
        return Optional.ofNullable(meleeWeapons.get(id));
    }

    public int addMeleeWeapon(MeleeWeapons w) throws SQLException {
        int id = MeleeWeaponRepository.addNewMeleeWeapon(w);
        meleeWeapons.put(id, w);
        return id;
    }

    public void updateMeleeWeapon(MeleeWeapons w) throws SQLException {
        MeleeWeaponRepository.updateMeleeWeapon(w);
        meleeWeapons.put(w.id(), w);
    }

    public void deleteMeleeWeapon(MeleeWeapons w) throws SQLException {
        MeleeWeaponRepository.deleteMeleeWeapon(w);
        meleeWeapons.remove(w.id());
    }

    // --- CoreAbilities ---
    public List<CoreAbilities> getAllCoreAbilities() {
        return new ArrayList<>(coreAbilities.values());
    }

    public Optional<CoreAbilities> getCoreAbilityById(int id) {
        return Optional.ofNullable(coreAbilities.get(id));
    }

    public int addCoreAbility(CoreAbilities a) throws SQLException {
        int id = CoreAbilityRepository.addNewCoreAbility(a);
        coreAbilities.put(id, a);
        return id;
    }

    public void updateCoreAbility(CoreAbilities a) throws SQLException {
        CoreAbilityRepository.updateCoreAbility(a);
        coreAbilities.put(a.id(), a);
    }

    public void deleteCoreAbility(CoreAbilities a) throws SQLException {
        CoreAbilityRepository.deleteCoreAbility(a);
        coreAbilities.remove(a.id());
    }

    // --- OtherAbilities ---
    public List<OtherAbilities> getAllOtherAbilities() {
        return new ArrayList<>(otherAbilities.values());
    }

    public Optional<OtherAbilities> getOtherAbilityById(int id) {
        return Optional.ofNullable(otherAbilities.get(id));
    }

    public int addOtherAbility(OtherAbilities a) throws SQLException {
        int id = OtherAbilityRepository.addNewOtherAbility(a);
        otherAbilities.put(id, a);
        return id;
    }

    public void updateOtherAbility(OtherAbilities a) throws SQLException {
        OtherAbilityRepository.updateOtherAbility(a);
        otherAbilities.put(a.id(), a);
    }

    public void deleteOtherAbility(OtherAbilities a) throws SQLException {
        OtherAbilityRepository.deleteOtherAbility(a);
        otherAbilities.remove(a.id());
    }

    // --- UnitKeywords ---
    public List<UnitKeywords> getAllUnitKeywords() {
        return new ArrayList<>(unitKeywords.values());
    }

    public Optional<UnitKeywords> getUnitKeywordById(int id) {
        return Optional.ofNullable(unitKeywords.get(id));
    }

    public int addUnitKeyword(UnitKeywords k) throws SQLException {
        int id = UnitKeywordRepository.addNewUnitKeyword(k);
        unitKeywords.put(id, k);
        return id;
    }

    public void updateUnitKeyword(UnitKeywords k) throws SQLException {
        UnitKeywordRepository.updateUnitKeyword(k);
        unitKeywords.put(k.id(), k);
    }

    public void deleteUnitKeyword(UnitKeywords k) throws SQLException {
        UnitKeywordRepository.deleteUnitKeyword(k);
        unitKeywords.remove(k.id());
    }

    // --- WeaponKeywords ---
    public List<WeaponKeywords> getAllWeaponKeywords() {
        return new ArrayList<>(weaponKeywords.values());
    }

    public Optional<WeaponKeywords> getWeaponKeywordById(int id) {
        return Optional.ofNullable(weaponKeywords.get(id));
    }

    public int addWeaponKeyword(WeaponKeywords k) throws SQLException {
        int id = WeaponKeywordRepository.addNewWeaponKeyword(k);
        weaponKeywords.put(id, k);
        return id;
    }

    public void updateWeaponKeyword(WeaponKeywords k) throws SQLException {
        WeaponKeywordRepository.updateWeaponKeyword(k);
        weaponKeywords.put(k.id(), k);
    }

    public void deleteWeaponKeyword(WeaponKeywords k) throws SQLException {
        WeaponKeywordRepository.deleteWeaponKeyword(k);
        weaponKeywords.remove(k.id());
    }
}
