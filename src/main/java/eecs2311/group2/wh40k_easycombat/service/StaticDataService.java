package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.Factions;
import eecs2311.group2.wh40k_easycombat.model.Units;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class StaticDataService {
    private static StaticDataService instance;

    private final Map<Integer, Factions> factions = new ConcurrentHashMap<>();
    private final Map<Integer, Units> units = new ConcurrentHashMap<>();

    public static synchronized StaticDataService getInstance() {
        if (instance == null) instance = new StaticDataService();
        return instance;
    }

    private StaticDataService() {
    }

    public void loadAll() {
        
    }
    
    public List<Factions> getAllFactions(){}
    public Factions addNewFaction(Factions newFaction){}
    public boolean updateFaction(int factionId, Factions newFaction){}

    public List<Units> getUnitsByFaction(int factionId){}
    public List<Units> getUnitsByKeyword(String keyword){}
    public Optional<Units> getUnitById(int unitId){}
    public Optional<Units> getUnitByName(String name){}
    public Units addNewUnit(Units unit){}
    public boolean updateUnit(int unitId, Units units){}
}
